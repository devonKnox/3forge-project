import com.f1.ami.client.*;
import com.f1.ami.amicommon.*;
import com.f1.utils.CH;
import com.f1.ami.amicommon.centerclient.*;
import com.f1.bootstrap.ContainerBootstrap;
import com.f1.utils.OH;
import matchingEngine.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class genSendOrders implements AmiClientListener, AmiCenterClientListener {
    public static final byte OPTION_AUTO_PROCESS_INCOMING = 2;
    private final AmiClient amiClient;
    private final List<Order> receivedOrders = new ArrayList<>();
    private static final Map<String, ConcurrentLinkedQueue<Order>> externalOrderQueues = new ConcurrentHashMap<>();
    public int orderId = 0;

    public genSendOrders(AmiClient client) {
        this.amiClient = client;
    }

    public void run(String configFile, String username, int clientPort, int centerPort, String assetClass, double volatility, int simSpeed) throws Exception {
        System.setProperty("f1.appname", "sim_" + assetClass); // Was throwing error if asset classes had same "f1.appname"
        new ContainerBootstrap(genSendOrders.class, new String[]{configFile});

        amiClient.addListener(this);
        amiClient.start("localhost", clientPort, username, OPTION_AUTO_PROCESS_INCOMING);

        AmiCenterClient centerClient = new AmiCenterClient(username);

        // Make IDs unique per asset class
        String subName = "sub_" + assetClass;

        centerClient.connect(subName, "localhost", centerPort, this);
        centerClient.subscribe(subName, CH.s("client_orders"));

        List<StockEntryMD1> allStocks = StockConfigLoader.loadGroupFromJSON(configFile, assetClass);
        ConcurrentLinkedQueue<Order> updateQueue = new ConcurrentLinkedQueue<>(); // Gets sent to AMI
        Random rand = new Random();
        System.out.println("Loaded stock list for " + assetClass + ": " + allStocks.size() + " symbols");

        for (StockEntryMD1 stock : allStocks) {
            String symbol = stock.getSymbol();
            externalOrderQueues.put(symbol, new ConcurrentLinkedQueue<>()); // Each order queue FROM AMI is specific to the stock symbol
            ConcurrentLinkedQueue<Order> queue = externalOrderQueues.get(symbol);

            new Thread(() -> {
                while (true) {
                    try {

                        if (!queue.isEmpty()) { // Add outside of loop?
                            Order externalOrder = queue.poll();
                        }

                        Order.Type type = rand.nextDouble() < 0.7 ? Order.Type.BUY : Order.Type.SELL;
                        Order.Kind kind = rand.nextDouble() < 0.8 ? Order.Kind.LIMIT : Order.Kind.MARKET;
                        double price = stock.getMidPrice();

                        if (kind == Order.Kind.LIMIT) { // For limit orders, add some randomness to the price
                            double spread = 5; //engine.getSpread();  // Should add some memory/feedback so price can actually be driven by the market
                            double offset = rand.nextGaussian() * Math.max(0.05, spread * (5 + rand.nextDouble() * 10)) / 4 * volatility;
                            price += offset;
                        }

                        int qty = Math.max(1, (int) Math.round(Math.exp(2.5 + 1.0 * rand.nextGaussian()))); // Random qty for order 
                        Order simOrder = new Order(symbol, type, kind, qty, price, System.currentTimeMillis());
                        
                        String orderType = kind == Order.Kind.MARKET ? "Market" : "Limit";
                        updateQueue.add(simOrder); // Send to update queue (full of multiple stocks of the asset), which then goes to AMI
                        
                        OH.sleep(simSpeed);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        new Thread(() -> {
            while (true) {
                try {
                    Order update = updateQueue.poll();
                    if (update != null) {
                        synchronized (amiClient) {
                                String id = update.getSymbol() + "_" + System.currentTimeMillis(); // Unique ID for the order
                                String random_account_char = new Random().ints(6, 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length()).mapToObj(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(i)).collect(Collectors.collectingAndThen(Collectors.toList(),list -> list.stream().map(Object::toString).collect(Collectors.joining()))); // Change for later, just to show who is actualy sending in orders
                                System.out.println("Adding to order feed: " + update.getSymbol() + " | Price: " + update.getPrice() + " | Qty: " + update.getQuantity());
                                amiClient.startObjectMessage("orderFeed", id); // make it orderID to ensure uniqueness?
                                amiClient.addMessageParamString("symbol", update.getSymbol());
                                amiClient.addMessageParamInt("orderId", orderId);
                                amiClient.addMessageParamString("account", random_account_char);
                                amiClient.addMessageParamString("direction", update.getType().toString());
                                amiClient.addMessageParamString("kind", update.getKind().toString());
                                amiClient.addMessageParamDouble("price", update.getPrice());
                                amiClient.addMessageParamInt("Qty", update.getQuantity());
                                amiClient.addMessageParamInt("OpenQty", update.getQuantity()); // This will later be decremented so that orders can filled until its 0
                                amiClient.addMessageParamDouble("timestamp", System.currentTimeMillis());
                                amiClient.sendMessageAndFlush();
                                orderId += rand.nextInt(1, 1000); // Increment orderId for next order, to ensure uniqueness
                        }
                    } else {
                        OH.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true); // keep simulation alive
    }

    @Override
    public void onCenterMessage(AmiCenterDefinition center, AmiCenterClientObjectMessage m) {
        System.out.println("Received AMI message: " + m);
        try {
            String raw = m.toString();
            int start = raw.indexOf("{");
            int end = raw.lastIndexOf("}");
            if (start >= 0 && end > start) {
                raw = raw.substring(start + 1, end);
            }

            String[] parts = raw.split(",\\s*");

            String symbol = null, typeStr = null, kindStr = null;
            double price = 0;
            int qty = 0;
            long timestamp = 0;

            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length != 2) continue;
                String key = kv[0].trim().toLowerCase();
                String val = kv[1].trim();

                switch (key) {
                    case "symbol": symbol = val.toUpperCase(); break;
                    case "type": typeStr = val.toUpperCase(); break;
                    case "kind": kindStr = val.toUpperCase(); break;
                    case "price": price = Double.parseDouble(val.replaceAll("[^0-9.\\-Ee]", "")); break;
                    case "qty": qty = Integer.parseInt(val.replaceAll("[^0-9]", "")); break;
                    case "timestamp": timestamp = (long) Double.parseDouble(val.replaceAll("[^0-9.\\-Ee]", "")); break;
                    default: System.err.println("Ignoring unknown key: " + key);
                }
            }

            if (symbol == null || typeStr == null || kindStr == null) {
                System.err.println(" Incomplete order data. Symbol/type/kind missing.");
                return;
            }

            if (!externalOrderQueues.containsKey(symbol)) {
                System.err.println(" No order queue found for symbol: " + symbol);
                return;
            }

            Order.Type type = Order.Type.valueOf(typeStr);
            Order.Kind kind = Order.Kind.valueOf(kindStr);
            Order order = new Order(symbol, type, kind, qty, price, timestamp);
            receivedOrders.add(order);
            externalOrderQueues.get(symbol).add(order);
            // System.out.println(" AMI ORDER RECEIVED — Symbol: " + symbol +
            //     " | Type: " + type + " | Kind: " + kind +
            //     " | Qty: " + qty + " | Price: " + price +
            //     " | Timestamp: " + timestamp);

        } catch (Exception e) {
            System.err.println(" Failed to parse AMI message: " + m);
            e.printStackTrace();
        }
    }

    @Override public void onLoggedIn(AmiClient c) {}
    @Override public void onConnect(AmiClient c) {}
    @Override public void onDisconnect(AmiClient c) {}
    @Override public void onMessageSent(AmiClient c, CharSequence msg) {}
    @Override public void onMessageReceived(AmiClient c, long now, long seqnum, int status, CharSequence msg) {}
    @Override public void onCommand(AmiClient c, String requestId, String cmd, String user, String type, String id, Map<String, Object> params) {}
    @Override public void onCenterConnect(AmiCenterDefinition center) {}
    @Override public void onCenterDisconnect(AmiCenterDefinition center) {}
    @Override public void onCenterMessageBatchDone(AmiCenterDefinition center) {}
}

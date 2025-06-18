import com.f1.ami.client.*;
import com.f1.ami.amicommon.*;
import com.f1.utils.CH;
import com.f1.ami.amicommon.centerclient.*;
import com.f1.bootstrap.ContainerBootstrap;
import com.f1.utils.OH;
import matchingEngine.*;

import org.json.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class CenterClient implements AmiClientListener, AmiCenterClientListener {
    public static final byte OPTION_AUTO_PROCESS_INCOMING = 2;
    private final AmiClient amiClient;
    private final List<Order> receivedOrders = new ArrayList<>();

    private static final Map<String, MatchingEngine> engines = new ConcurrentHashMap<>(); // Maps each stock to its MatchingEngine
    private static final Map<String, ConcurrentLinkedQueue<Order>> externalOrderQueues = new ConcurrentHashMap<>(); // Maps each stock symbol to its external order queue

    public CenterClient(AmiClient client) {
        this.amiClient = client;
    }

    public static void main(String[] args) throws Exception {
        new ContainerBootstrap(CenterClient.class, args); //?

        if (args.length != 1) {
            System.out.println("Usage: java com.f1.CenterClient <stocks_asset_class.json>");
            return;
        }

        // For sending orders to AMI
        AmiClient baseClient = new AmiClient();
        CenterClient listener = new CenterClient(baseClient);
        baseClient.addListener(listener);
        baseClient.start("localhost", 3289, "demo", OPTION_AUTO_PROCESS_INCOMING);

        // For receiving orders from AMI
        AmiCenterClient centerClient = new AmiCenterClient("demo");
        centerClient.connect("subscription1", "localhost", 3270, listener);
        centerClient.subscribe("subscription1", CH.s("client_orders"));

        List<StockEntryMD1> stockList = StockConfigLoader.loadGroupFromJSON(args[0], "Auto");
        ConcurrentLinkedQueue<StockUpdate> updateQueue = new ConcurrentLinkedQueue<>();
        Random rand = new Random();

        for (StockEntryMD1 stock : stockList) {
            String symbol = stock.getSymbol();
            MatchingEngine engine = new MatchingEngine(symbol, stock.getMidPrice());
            engines.put(symbol, engine);
            externalOrderQueues.put(symbol, new ConcurrentLinkedQueue<>());

           new Thread(() -> {
    while (true) {
        ConcurrentLinkedQueue<Order> queue = externalOrderQueues.get(symbol);

        if (!queue.isEmpty()) {
            Order externalOrder = queue.poll();
            engine.addOrder(externalOrder);

            double prevPrice = engine.getLastTradePrice();
            engine.processAllTrades();
            double newPrice = engine.getLastTradePrice();

            if (prevPrice != newPrice) {
                String orderType = externalOrder.getKind() == Order.Kind.MARKET ? "Market" : "Limit";
                updateQueue.add(new StockUpdate(symbol, System.currentTimeMillis(), newPrice, orderType));
                System.out.println("AMI Order executed and update queued: " + newPrice);
            }

            continue;
        }

        Order.Type type = rand.nextDouble() < 0.7 ? Order.Type.BUY : Order.Type.SELL;
        Order.Kind kind = rand.nextDouble() < 0.8 ? Order.Kind.LIMIT : Order.Kind.MARKET;
        double price = stock.getMidPrice();
        if (kind == Order.Kind.LIMIT) {
            double spread = engine.getSpread();
            double offset = rand.nextGaussian() * Math.max(0.05, spread * (5 + rand.nextDouble() * 10)) / 4;
            price += offset;
        }

        int qty = Math.max(1, (int) Math.round(Math.exp(2.5 + 1.0 * rand.nextGaussian())));
        Order simOrder = new Order(type, kind, qty, price, System.currentTimeMillis());

        engine.addOrder(simOrder);

        double prevPrice = engine.getLastTradePrice();
        engine.processAllTrades();
        double newPrice = engine.getLastTradePrice();

        if (prevPrice != newPrice) {
            String orderType = kind == Order.Kind.MARKET ? "Market" : "Limit";
            updateQueue.add(new StockUpdate(symbol, System.currentTimeMillis(), newPrice, orderType));
        }

        OH.sleep(1000);
    }
}).start();

        }

        new Thread(() -> {
            while (true) {
                StockUpdate update = updateQueue.poll();
                if (update != null) {
                    try {
                        String id = update.symbol + " " + update.timestamp;
                        synchronized (baseClient) {
                            baseClient.startObjectMessage("simpleMD", id);
                            baseClient.addMessageParamString("Symbol", update.symbol);
                            baseClient.addMessageParamLong("LastTradeTimestamp", update.timestamp);
                            baseClient.addMessageParamDouble("Price", update.price);
                            baseClient.addMessageParamString("orderType", update.order_type);
                            baseClient.sendMessageAndFlush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    OH.sleep(1000);
                }
            }
        }).start(); 
        
        while(true);
    }

    @Override
    public void onCenterMessage(AmiCenterDefinition center, AmiCenterClientObjectMessage m) {
        System.out.println("Received AMI message: " + m);
        try {
            String raw = m.toString();
            String[] parts = raw.split(",\\s*");

            String symbol = null, typeStr = null, kindStr = null;
            double price = 0;
            int qty = 0;
            long timestamp = 0;

            for (String part : parts) {
                String[] kv = part.split("=");
                if (kv.length != 2) continue;
                String key = kv[0].trim().toLowerCase();
                String val = kv[1].replaceAll("[^0-9A-Za-z.\\-]", "");


                switch (key) {
                    case "symbol": symbol = val; break;
                    case "type": typeStr = val.toUpperCase(); break;
                    case "kind": kindStr = val.toUpperCase(); break;
                    case "price": price = Double.parseDouble(val); break;
                    case "qty": qty = Integer.parseInt(val); break;
                    case "timestamp": timestamp = (long) Double.parseDouble(val); break;
                }
            }

            if (typeStr != null && kindStr != null && symbol != null) {
                Order.Type type = Order.Type.valueOf(typeStr);
                Order.Kind kind = Order.Kind.valueOf(kindStr);
                Order order = new Order(type, kind, qty, price, timestamp);
                receivedOrders.add(order);
                externalOrderQueues.get(symbol).add(order);
                System.out.println("AMI ORDER RECEIVED — Symbol: " + symbol +
    " | Type: " + type + " | Kind: " + kind + 
    " | Qty: " + qty + " | Price: " + price +
    " | Timestamp: " + timestamp);

            }

        } catch (Exception e) {
            System.err.println("Failed to parse AMI message: " + m);
            e.printStackTrace();
        }
    }

    @Override public void onLoggedIn(AmiClient c) {}
    @Override public void onConnect(AmiClient c) {}
    @Override public void onDisconnect(AmiClient c) {}
    @Override public void onMessageSent(AmiClient c, CharSequence msg) {}
    @Override public void onMessageReceived(AmiClient c, long now, long seqnum, int status, CharSequence msg) {
        System.out.println("Message received: " + msg);
    }
    @Override public void onCommand(AmiClient c, String requestId, String cmd, String user, String type, String id, Map<String, Object> params) {}
    @Override public void onCenterConnect(AmiCenterDefinition center) {}
    @Override public void onCenterDisconnect(AmiCenterDefinition center) {}
    @Override public void onCenterMessageBatchDone(AmiCenterDefinition center) {}
}
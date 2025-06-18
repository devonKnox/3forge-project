package matchingEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MatchingEngine{
    /*
     * This class implements the logic for a matching engine for an individual stock trade, handling limit and market orders, and will output updates in stock prices.
     * Classes with their own AMI client will use this class to push data to the AMI server.
     */
    private static final double MAX_SPREAD = 0.3; // Controls how aggresively we want orders to match. If the difference between the best bid and best ask is greater than this, no match will occur.
    private String stockSymbol;
    private double lastTradePrice;
    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;

    public MatchingEngine(String stockSymbol, double LastTradePrice) {
        this.stockSymbol = stockSymbol;
        this.lastTradePrice = LastTradePrice;
        this.buyOrders = new PriorityQueue<>(new BuyOrderComparator());
        this.sellOrders = new PriorityQueue<>(new SellOrderComparator());

    }

    public void addOrder(Order order) {
        if (order.getType() == Order.Type.BUY) {
            buyOrders.add(order);
        } else if (order.getType() == Order.Type.SELL) {
            sellOrders.add(order);
        }
    }

    public void getQueue() { // For testing purposes, to see the order book
        System.out.println("\n=== Buy Order Execution Priority ===");
        PriorityQueue<Order> buyCopy = new PriorityQueue<>(buyOrders);
        while (!buyCopy.isEmpty()) {
            System.out.println(buyCopy.poll());
        }

        System.out.println("\n=== Sell Order Execution Priority ===");
        PriorityQueue<Order> sellCopy = new PriorityQueue<>(sellOrders);
        while (!sellCopy.isEmpty()) {
            System.out.println(sellCopy.poll());
        }

        System.out.println("====================================\n");
    }

    public void processAllTrades() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

        if (!isMatch(buyOrder, sellOrder)) {
            return;
        }
        processTrade(buyOrder, sellOrder);
        }
    }

    public void processTrade(Order buyOrder, Order sellOrder) {

        int tradeQty = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
        double tradePrice = (buyOrder.getKind() == Order.Kind.MARKET) ? sellOrder.getPrice()
                            : (sellOrder.getKind() == Order.Kind.MARKET) ? buyOrder.getPrice()
                            : (buyOrder.getPrice() + sellOrder.getPrice()) / 2;

        lastTradePrice = tradePrice;

      //  System.out.println("Trade executed: " + tradeQty + "shares of " + stockSymbol  + " @ " + "$" + tradePrice + " |" + buyOrder + "| " + sellOrder);

        // Order at top of ladder continues to get filled until it is exhausted
        buyOrder.setQuantity(buyOrder.getQuantity() - tradeQty);
        sellOrder.setQuantity(sellOrder.getQuantity() - tradeQty);

        if (buyOrder.getQuantity() == 0) buyOrders.poll(); // Remove the order if quantity is zero
        if (sellOrder.getQuantity() == 0) sellOrders.poll();
    }

    private boolean isMatch(Order buy, Order sell) {
        return (buy.getKind() == Order.Kind.MARKET || sell.getKind() == Order.Kind.MARKET)
            || (buy.getPrice() + MAX_SPREAD) >= sell.getPrice();
    }

    // For visualizing order ladder
    public List<Order> getTopBuyOrders(int n) {
        PriorityQueue<Order> copy = new PriorityQueue<>(buyOrders);
        List<Order> topOrders = new ArrayList<>();
        for (int i = 0; i < n && !copy.isEmpty(); i++) {
            topOrders.add(copy.poll());
        }
        return topOrders;
    }

    public List<Order> getTopSellOrders(int n) {
        PriorityQueue<Order> copy = new PriorityQueue<>(sellOrders);
        List<Order> topOrders = new ArrayList<>();
        for (int i = 0; i < n && !copy.isEmpty(); i++) {
            topOrders.add(copy.poll());
        }
        return topOrders;
    }


    public Order getBestBid() {
        return buyOrders.peek(); // Returns the best buy order (highest price)
    }

    public Order getBestAsk() {
        return sellOrders.peek(); // Returns the best sell order (lowest price)
    }

    public double getSpread() { // Used in main class to 
        if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
            return 0.0; // No orders to calculate spread
        }
        return Math.max(0, getBestAsk().getPrice() - getBestBid().getPrice()); // Spread is the difference between best ask and best bid
    }

    public double getLastTradePrice() {
        return this.lastTradePrice;
    }
    
}
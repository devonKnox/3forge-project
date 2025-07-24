package com.threeforge.simulation;

import com.threeforge.model.EquityOrder;
import com.threeforge.model.Order;

import orders.LoadFromJSON;
//import matchingEngine.LoadFromJSON;
import orders.StockEntryMD1;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * The "Producer" class.
 * It generates simulated orders and places them into a shared queue for processing.
 */
public class OrderGenerator implements Runnable {

    private final BlockingQueue<Order> orderQueue;
    private final List<StockEntryMD1> allStocks;
    private final List<LoadFromJSON.Account> accounts;
    private final Random rand = new Random();
    private static final int ORDERS_PER_STOCK = 5; // Generate 5 orders for each stock for this MVP

    public OrderGenerator(BlockingQueue<Order> orderQueue, List<StockEntryMD1> allStocks, List<LoadFromJSON.Account> accounts) {
        this.orderQueue = orderQueue;
        this.allStocks = allStocks;
        this.accounts = accounts;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Generator] Starting order generation...");

            for (StockEntryMD1 stock : allStocks) {
                for (int i = 0; i < ORDERS_PER_STOCK; i++) {
                    EquityOrder newOrder = generateSingleOrder(stock);
                    orderQueue.put(newOrder); // Place the new order in the queue
                    Thread.sleep(50); // Small delay to simulate a real-world feed
                }
            }

            // --- IMPORTANT: Add a "poison pill" to signal the end of work ---
            System.out.println("[Generator] All orders generated. Sending shutdown signal.");
            orderQueue.put(new EquityOrder("POISON_PILL", null, null, 0, 0, 0, "SYSTEM"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Generator] Order generator was interrupted.");
        }
    }

    private EquityOrder generateSingleOrder(StockEntryMD1 stock) {
        // This logic is copied from your original genSendOrders class
        orders.Order.Type type = rand.nextDouble() < 0.7 ? orders.Order.Type.BUY : orders.Order.Type.SELL;
        orders.Order.Kind kind = rand.nextDouble() < 0.8 ? orders.Order.Kind.LIMIT : orders.Order.Kind.MARKET;
        double lastPrice = stock.getMidPrice();
        double baseVol = 0.01 * lastPrice;
        double drift = type == orders.Order.Type.BUY ? 0.002 : -0.002;
        double noise = rand.nextGaussian() * baseVol;
        double trend = drift * lastPrice + noise;
        double price = lastPrice + trend;
        if (kind == orders.Order.Kind.LIMIT) {
            double limitSpread = rand.nextDouble() * 0.005 * lastPrice;
            price += (type == orders.Order.Type.BUY ? -limitSpread : limitSpread);
        }
        price = Math.max(0.01, Math.round(price * 100.0) / 100.0);
        int qty = Math.max(1, (int) Math.round(Math.exp(2.5 + 1.0 * rand.nextGaussian())));
        LoadFromJSON.Account placingAccount = weightedRandomAccount(accounts, rand);

        return new EquityOrder(stock.getSymbol(), type, kind, qty, price, System.currentTimeMillis(), placingAccount.name);
    }

    private LoadFromJSON.Account weightedRandomAccount(List<LoadFromJSON.Account> accounts, Random rand) {
        double total = 0;
        for (LoadFromJSON.Account acc : accounts) total += acc.activityLevel;
        double r = rand.nextDouble() * total;
        double sum = 0;
        for (LoadFromJSON.Account acc : accounts) {
            sum += acc.activityLevel;
            if (r <= sum) return acc;
        }
        return accounts.get(accounts.size() - 1);
    }
}

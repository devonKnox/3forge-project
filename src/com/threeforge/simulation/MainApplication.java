package com.threeforge.simulation;

import com.threeforge.ami.AmiSenderService;
import com.threeforge.model.Order;
import orders.LoadFromJSON;
import orders.StockEntryMD1;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The main entry point for the simulation application
 */
public class MainApplication {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: Please provide the path to the configuration file.");
            System.out.println("Usage: java com.threeforge.simulation.MainApplication <path/to/config.json>");
            return;
        }
        String configFile = args[0];
        System.out.println("--- Starting Simulation ---");
        System.out.println("Loading configuration from: " + configFile);

        try {
            // 1. Load data from JSON config file
            List<StockEntryMD1> allStocks = LoadFromJSON.loadStocksFromJSON(configFile);
            List<LoadFromJSON.Account> accounts = LoadFromJSON.loadAccountsFromJSON(configFile);

            // 2. Create the shared queue
            BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>(1000);

            // 3. Create the producer and the NEW consumer tasks
            Runnable orderGenerator = new OrderGenerator(orderQueue, allStocks, accounts);
            // --- UPDATED: Instantiate the AmiSenderService ---
            // You would pass real connection details from a config file here
            Runnable amiSender = new AmiSenderService(orderQueue, "localhost", 3289, "simUser");

            // 4. Create and start the threads
            Thread generatorThread = new Thread(orderGenerator);
            Thread consumerThread = new Thread(amiSender); // <-- UPDATED

            System.out.println("Starting generator and AMI sender threads...");
            generatorThread.start();
            consumerThread.start();

            // 5. Wait for both threads to complete
            generatorThread.join();
            consumerThread.join();

            System.out.println("--- Simulation Finished ---");

        } catch (Exception e) {
            System.err.println("A fatal error occurred in the main application thread.");
            e.printStackTrace();
        }
    }
}

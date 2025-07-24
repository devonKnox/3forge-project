package com.threeforge.ami;

import com.threeforge.model.Order;

import java.util.concurrent.BlockingQueue;

/**
 * The "Consumer" class. For this MVP, it simulates the AMI sender
 * by taking orders from the queue and printing them to the console.
 */
public class OrderConsumer implements Runnable {

    private final BlockingQueue<Order> orderQueue;

    public OrderConsumer(BlockingQueue<Order> orderQueue) {
        this.orderQueue = orderQueue;
    }

    @Override
    public void run() {
        System.out.println("[Consumer] Started. Waiting for orders...");
        try {
            while (true) {
                Order order = orderQueue.take(); // take() waits if the queue is empty

                // Check for the "poison pill" to know when to shut down
                if ("POISON_PILL".equals(order.getSymbol())) {
                    System.out.println("[Consumer] Shutdown signal received. Exiting.");
                    break; // Exit the loop
                }

                // In a real application, this is where you would call the AmiClient
                // For now, we just print the order
                System.out.println("[Consumer] Processed: " + order.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Consumer] Consumer thread was interrupted.");
        }
    }
}

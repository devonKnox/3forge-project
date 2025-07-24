package com.threeforge.ami;

import com.threeforge.ami.builders.AmiMessageBuilder;
import com.threeforge.ami.builders.EquityOrderBuilder;
import com.threeforge.model.EquityOrder;
import com.threeforge.model.Order;
import com.f1.ami.client.AmiClient;
import com.f1.ami.client.AmiClientListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch; // <-- FIXED: Added for synchronization

/**
 * The "Consumer" that connects to the AMI client and sends orders.
 */
public class AmiSenderService implements Runnable, AmiClientListener {

    public static final byte OPTION_AUTO_PROCESS_INCOMING = 2;

    private final BlockingQueue<Order> orderQueue;
    private final AmiClient amiClient;
    private final Map<Class<? extends Order>, AmiMessageBuilder> builderMap;
    private final String host;
    private final int port;
    private final String username;

    // --- FIXED: Latch to wait for connection before sending messages ---
    private final CountDownLatch connectionLatch = new CountDownLatch(1);

    public AmiSenderService(BlockingQueue<Order> orderQueue, String host, int port, String username) {
        this.orderQueue = orderQueue;
        this.host = host;
        this.port = port;
        this.username = username;
        this.amiClient = new AmiClient();
        this.builderMap = new HashMap<>();
        this.builderMap.put(EquityOrder.class, new EquityOrderBuilder());
    }

    @Override
    public void run() {
        try {
            System.out.println("[AmiSender] Connecting to AMI client at " + host + ":" + port);
            amiClient.addListener(this);
            amiClient.start(host, port, username, OPTION_AUTO_PROCESS_INCOMING);

            // --- FIXED: Wait for the onConnect method to fire ---
            System.out.println("[AmiSender] Waiting for successful connection...");
            connectionLatch.await(); // This will pause the thread until onConnect calls countDown()

            System.out.println("[AmiSender] Connection established. Now processing orders...");

            while (true) {
                Order order = orderQueue.take();

                if ("POISON_PILL".equals(order.getSymbol())) {
                    System.out.println("[AmiSender] Shutdown signal received. Exiting.");
                    break;
                }

                AmiMessageBuilder builder = builderMap.get(order.getClass());
                if (builder != null) {
                    builder.buildAndSendMessage(order, amiClient);
                } else {
                    System.err.println("[AmiSender] No builder found for order type: " + order.getClass().getName());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[AmiSender] Service was interrupted.");
        } catch (Exception e) {
            System.err.println("[AmiSender] An error occurred during execution.");
            e.printStackTrace();
        }
    }

    // --- Required methods for AmiClientListener ---

    @Override
    public void onConnect(AmiClient c) {
        System.out.println("[AmiSender] Successfully connected.");
        // --- FIXED: Release the latch to allow the run() method to proceed ---
        connectionLatch.countDown();
    }

    @Override public void onLoggedIn(AmiClient c) { System.out.println("[AmiSender] Successfully logged in."); }
    @Override public void onDisconnect(AmiClient c) { System.out.println("[AmiSender] Disconnected."); }
    @Override public void onMessageSent(AmiClient c, CharSequence msg) {}
    @Override public void onMessageReceived(AmiClient c, long now, long seqnum, int status, CharSequence msg) {}
    @Override public void onCommand(AmiClient c, String requestId, String cmd, String user, String type, String id, Map<String, Object> params) {}
}

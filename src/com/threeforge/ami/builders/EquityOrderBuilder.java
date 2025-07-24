package com.threeforge.ami.builders;

import com.threeforge.model.EquityOrder;
import com.threeforge.model.Order;
import com.f1.ami.client.AmiClient; // FIXED: Corrected the import path

/**
 * The specific strategy for building an AMI message from an EquityOrder.
 */
public class EquityOrderBuilder implements AmiMessageBuilder {

    @Override
    public void buildAndSendMessage(Order order, AmiClient client) {
        // Cast the generic Order to the specific EquityOrder
        EquityOrder eqOrder = (EquityOrder) order;

        // Use a unique ID for the message, e.g., combining symbol and timestamp
        String messageId = eqOrder.getSymbol() + "_" + System.currentTimeMillis();

        System.out.println("[AmiSender] Sending Order: " + eqOrder.getSymbol());
        int i = (int) System.currentTimeMillis() % 1000000; // Generate a unique order ID based on current time

        // This logic is adapted from your original genSendOrders class
        synchronized (client) {
            client.startObjectMessage("orderFeed", messageId);
            client.addMessageParamString("symbol", eqOrder.getSymbol());
            client.addMessageParamString("account", eqOrder.getAccountName());
            client.addMessageParamString("direction", eqOrder.getType().toString());
            client.addMessageParamString("kind", eqOrder.getKind().toString());
            client.addMessageParamDouble("price", eqOrder.getPrice());
            client.addMessageParamInt("Qty", eqOrder.getQuantity());
            client.addMessageParamInt("orderId", i);
            client.addMessageParamInt("OpenQty", eqOrder.getQuantity()); // Initially, OpenQty is the full quantity
            client.addMessageParamDouble("timestamp", System.currentTimeMillis());
            client.sendMessageAndFlush();
        }
    }
}

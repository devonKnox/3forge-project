package com.threeforge.ami.builders;

import com.threeforge.model.Order;
import com.f1.ami.client.AmiClient; 

/**
 * An interface for any class that can build and send a specific
 * type of message using the AmiClient.
 */
public interface AmiMessageBuilder {
    void buildAndSendMessage(Order order, AmiClient client);
}

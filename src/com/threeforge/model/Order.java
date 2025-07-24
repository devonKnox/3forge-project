package com.threeforge.model;

/**
 * Interface representing a generic order or message.
 * This allows the system to handle different types of messages in the future.
 */
public interface Order {
    String getSymbol();
}

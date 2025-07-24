package com.threeforge.model;

import orders.Order.Kind;
import orders.Order.Type;

/**
 * A concrete class representing a standard equity order.
 * It holds all the specific details for a stock trade.
 */
public class EquityOrder implements Order {

    private final String symbol;
    private final Type type;
    private final Kind kind;
    private final int quantity;
    private final double price;
    private final long timestamp;
    private final String accountName;

    public EquityOrder(String symbol, Type type, Kind kind, int quantity, double price, long timestamp, String accountName) {
        this.symbol = symbol;
        this.type = type;
        this.kind = kind;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.accountName = accountName;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    // Add getters for all other fields so the consumer can access them
    public Type getType() { return type; }
    public Kind getKind() { return kind; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getAccountName() { return accountName; }

    @Override
    public String toString() {
        return String.format("Account: %-28s | Order: %-5s %-7s %-5s @ %-8.2f | Qty: %d",
                accountName, symbol, type, kind, price, quantity);
    }
}

package matchingEngine;

public class Order {
    public enum Type {
        BUY, SELL
    }

    public enum Kind {
        LIMIT, MARKET
    }

    private String symbol;
    private Type type;
    private Kind kind;
    private int quantity;
    private double price;
    private long timestamp;

    public Order(String symbol, Type type, Kind kind, int quantity, double price, long timestamp) {
        this.symbol = symbol;
        this.type = type;
        this.kind = kind;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }
    
    public Type getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

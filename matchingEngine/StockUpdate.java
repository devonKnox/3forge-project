package matchingEngine;
/*
 * This class represents a stock update in the matching engine
 */

public class StockUpdate {
    public final String symbol;
    public final long timestamp;
    public final double price;
    public final String order_type; // Market or Limit

    public StockUpdate(String symbol, long timestamp, double price, String order_type) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.price = price;
        this.order_type = order_type;
    }
}

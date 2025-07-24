package orders;

/**
 * Data container for a stock entry loaded from the JSON config.
 */
public class StockEntryMD1 {
    private String symbol;
    private double midPrice;
    // private int numEntries;

    public StockEntryMD1(String symbol, double midPrice) {
        this.symbol = symbol;
        this.midPrice = midPrice;
        // this.numEntries = numEntries;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public double getMidPrice() {
        return this.midPrice;
    }

    public void setMidPrice(double midPrice) {
        this.midPrice = midPrice;
    }
}

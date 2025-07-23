package matchingEngine;
/*
 * This class is used to parse stock entries from a JSON file. Will be changed to account for volatility and other factors
 */


public class StockEntryMD1 {
    private String symbol;
    private double midPrice;
    private int numEntries;

    public StockEntryMD1(String symbol, double midPrice, int numEntries) {
        this.symbol = symbol;
        this.midPrice = midPrice;
        this.numEntries = numEntries;
    }

    public String getSymbol() { return symbol; }
    public double getMidPrice() { return midPrice; }
    public int getNumEntries() { return numEntries; }
    public void setMidPrice(double midPrice) {
        this.midPrice = midPrice;
    }

}

package matchingEngine;
import java.util.Comparator;

public class BuyOrderComparator implements Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        // Market orders first
        if (o1.getKind() == Order.Kind.MARKET && o2.getKind() != Order.Kind.MARKET) return -1;
        if (o2.getKind() == Order.Kind.MARKET && o1.getKind() != Order.Kind.MARKET) return 1;

        // Higher price is better for buy orders
        int priceCmp = Double.compare(o2.getPrice(), o1.getPrice());
        if (priceCmp != 0) return priceCmp;

        // Earlier time gets priority
        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
    }
}

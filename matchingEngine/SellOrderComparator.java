package matchingEngine;
import java.util.Comparator;

public class SellOrderComparator implements Comparator<Order> {
    @Override
    public int compare(Order o1, Order o2) {
        // Market orders first
        if (o1.getKind() == Order.Kind.MARKET && o2.getKind() != Order.Kind.MARKET) return -1;
        if (o2.getKind() == Order.Kind.MARKET && o1.getKind() != Order.Kind.MARKET) return 1;

        // Lower price is better for sell orders
        int priceCmp = Double.compare(o1.getPrice(), o2.getPrice());
        if (priceCmp != 0) return priceCmp;

        // Earlier time gets priority
        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
    }
}

// // Add indexes later for fast SFW commands (definitely needed for HDB)
//Add tables with "I" flag later for correct startup

DROP TRIGGER IF EXISTS executeTrade;
DROP TIMER IF EXISTS orderFeedTimer;
DROP METHOD IF EXISTS simpleMatch(String symbol);
DROP METHOD IF EXISTS getSymbols()
DROP METHOD IF EXISTS processAllTrades();
DROP METHOD IF EXISTS getSpread(Double bid, Double ask);

CREATE METHOD Double getSpread(Double bid, Double ask)
{ // Make complex later
    return ask - bid;
};

CREATE METHOD Int simpleMatch(String symbol)
 {
    // Prioritize market orders first
    
    //Get one BUY order
    Table buyTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "BUY" AND OpenQty > 0 AND kind == "MARKET" ORDER BY price DESC, timestamp ASC;
  
    // Get one SELL order
    Table sellTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "SELL" AND OpenQty > 0 AND kind == "MARKET" ORDER BY price ASC, timestamp ASC;
  
  
    // Get rows
    List buys = buyTable.getRows();
    List sells = sellTable.getRows();
  
    if (buys.size() == 0 || sells.size() == 0) { // No more market orders, now do limit orders
      buyTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "BUY" AND OpenQty > 0 AND kind == "LIMIT" ORDER BY price DESC, timestamp ASC;
      sellTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "SELL" AND OpenQty > 0 AND kind == "LIMIT" ORDER BY price ASC, timestamp ASC;
      List buys = buyTable.getRows();
      List sells = sellTable.getRows();
    
      if (buys.size() == 0 || sells.size() == 0) { // Check if there's no limit orders either
        return 0; // This means either there are no buy or sell orders at the moment
      }
    }
    
    else {
    
      // Retrieve best buy and best sell
      Map buy = buys.get(0);
      Map sell = sells.get(0);
    
      Double buyPrice = buy.get("price");
      Double sellPrice = sell.get("price");
      
      Double spread = getSpread(buyPrice, sellPrice);
    
      if (spread >= 10) { // Spread logic
        return 0; // If spread isn't crossed (work on later), then don't trade!
      }
      
      else {
    
        Int buyQty = buy.get("OpenQty");
        String buyAcc = buy.get("account");
        String buyType = buy.get("kind");
        
        Int sellQty = sell.get("OpenQty");
        String sellAcc = sell.get("account");
        
        
        
        Int matchQty = minimum(buyQty, sellQty);
        Int newOpenQty_buy = buyQty - matchQty;
        Int newOpenQty_sell = sellQty - matchQty;
        Double tradePrice = (buyPrice + sellPrice) / 2;
        USE ds="AMI" EXECUTE INSERT INTO trades VALUES ("${symbol}", ${tradePrice}, ${matchQty}, "${buyAcc}", "${sellAcc}", ${timestamp()} );
      
        // Need to update openQty in order book for both buy and sells
        USE ds="AMI" EXECUTE UPDATE orderFeed SET OpenQty = ${newOpenQty_buy} WHERE account == "${buyAcc}";
        USE ds="AMI" EXECUTE UPDATE orderFeed SET OpenQty = ${newOpenQty_sell} WHERE account == "${sellAcc}";
      
        return 1;
      }
    }
  };

CREATE METHOD List getSymbols() {
        Table distinctSymbols = USE ds="AMI" EXECUTE SELECT symbol FROM orderFeed GROUP BY symbol;
    List l = distinctSymbols.getRows();
    int size = l.size();
    List syms = new List();
    for (int i = 0; i < size; i++) {
      Map m = l.get(i);
      syms.add(m.get("symbol"));
    }
    return syms;
};

CREATE METHOD Int processAllTrades() {
    list symbolsToTrade = getSymbols();
    Iterator i = symbolsToTrade.iterator();
    int count = 0;
    while(i.hasNext()) {
      String sym = symbolsToTrade.get(count);
      count += 1;
      i.next();
      simpleMatch(sym);
    }
};

//String t = "TSLA";
//CREATE TRIGGER executeTrade OFTYPE AMISCRIPT ON orderFeed USE onInsertedScript="int ret = simpleMatch(t);";
CREATE TIMER orderFeedTimer OFTYPE AMISCRIPT ON "500" PRIORITY 0 USE script="int rett = processAllTrades();";
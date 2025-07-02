// // Add indexes later for fast SFW commands (definitely needed for HDB)
//Add tables with "I" flag later for correct startup

DROP TRIGGER IF EXISTS executeTrade;
DROP METHOD IF EXISTS simpleMatch(String symbol);

CREATE METHOD Int simpleMatch(String symbol)
{  
  //Get one BUY order
  Table buyTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "BUY" AND OpenQty > 0 ORDER BY price DESC, timestamp ASC; // works

  // Get one SELL order
  Table sellTable = USE ds="AMI" EXECUTE SELECT * FROM orderFeed WHERE direction == "SELL" AND OpenQty > 0 ORDER BY price ASC, timestamp ASC;
  // Get rows
  List buys = buyTable.getRows();
  List sells = sellTable.getRows();

  if (buys.size() == 0 && sells.size() == 0) return 0; // Don't think this is needed?

  // Retrieve best buy and best sell
  Map buy = buys.get(0);
  Map sell = sells.get(0);

  Double buyPrice = buy.get("price");
  Double sellPrice = sell.get("price");

  if (sellPrice - buyPrice >= 20) {
    return 0;
  }

  Int buyQty = buy.get("OpenQty");
  String buyAcc = buy.get("Account");
  Int sellQty = sell.get("OpenQty");
  String sellAcc = sell.get("Account");
  
  
  Int matchQty = minimum(buyQty, sellQty);
  Int newOpenQty_buy = buyQty - matchQty;
  Int newOpenQty_sell = sellQty - matchQty;
  Double tradePrice = (buyPrice + sellPrice) / 2;
  String stri = "acc";
  Double dub = 5;
  Int qt = 5;

  USE ds="AMI" EXECUTE INSERT INTO trades(symbol, quantity, price, sellAccount, buyAccount, timestamp) VALUES ("${symbol}", ${matchQty}, ${tradePrice}, "${buyAcc}", "${sellAcc}", ${timestamp()} );

  // Need to update openQty in order book for both buy and sells
  USE ds="AMI" EXECUTE UPDATE orderFeed SET OpenQty == newOpenQty_buy WHERE account == "${buyAcc}"";
  USE ds="AMI" EXECUTE UPDATE orderFeed SET OpenQty == newOpenQty_sell WHERE account == "${sellAcc}"";

  
  return 1;
};
String t = "TSLA";
CREATE TRIGGER executeTrade OFTYPE AMISCRIPT ON orderFeed USE onInsertedScript="int ret = simpleMatch(t);";

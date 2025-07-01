// setting up center for matching engine and trade generation
// // Add indexes later for fast SFW commands (definitely needed for HDB)

// DROP TRIGGER IF EXISTS addOrdertoBook(String symbol, String direction, )
//Add tables with "I" flag later for correct startup
//CREATE TRIGGER TRIGGER_PROCESS_ORDERS OFTYPE AMISCRIPT ON MD1Snaps USE canMutateRow="false" onInsertedScript="int ret = processOrders(Symbol);";


DROP TRIGGER IF EXISTS executeTrade;
DROP TABLE IF EXISTS trades;
DROP METHOD IF EXISTS matchOrder(String symbol, String direction, double price, Int qty, Int openQty, Int orderId, account String, kind String);

CREATE PUBLIC TABLE trades(
    symbol String,
    quantity Int,
    price double,
    sellAccount String,
    buyAccount String,
    timestamp double
);

CREATE TRIGGER executeTrade OFTYPE AMISCRIPT ON orderFeed USE onInsertedScript="int ret = matchOrder(symbol, orderId, account, direction, kind);";

//CREATE TRIGGER executeTrade OFTYPE AMISCRIPT ON orderFeed USE
//rowVar = "_row"
//onInsertedScript="insert into trades values(\"test\", 1, 1, \"test\", \"test\", 1)";


// For later
//if(${symbol} == "TSLA" && ${direction} == "BUY") {
//insert into tslaBuyLadder(quantity, price, kind, timestamp) values(1, 0, "TS", \"${timestamp()}\");
//}""";


//CREATE TRIGGER ordertoBook_tslaSellLadder OFTYPE AMISCRIPT ON orderFeed USE
//onInsertedScript="insert into test_one(symbol, price) values("test", 100)";
//insert into tslaBuyLadder(symbol, price) values("test", 100);

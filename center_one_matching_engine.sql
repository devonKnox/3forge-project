-- setting up center for matching engine and trade generation
-- index?
-- drop table, triggers, methods if exists ...
// Add indexes later for fast SFW commands (definitely needed for HDB)

DROP TRIGGER IF EXISTS addOrdertoBook(String symbol, String direction, )

DROP TABLE IF EXISTS buyOrders;
DROP TABLE IF EXISTS sellOrders;
DROP TABLE IF EXISTS mdLevel1;

DROP METHOD IF EXISTS compareBuyOrders(String )
DROP METHOD IF EXISTS sortBuyOrders()

CREATE PUBLIC TABLE buyOrders(String symbol, String direction, String orderId, String price, String quantity, String timestamp) USE RefreshPeriodMs="100" OnUndefColumn="ADD" InitialCapacity="1000"; // Look into params for referesh, etc. later

CREATE PUBLIC TABLE sellOrders()

CREATE TRIGGER TRIGGER_PROCESS_ORDERS OFTYPE AMISCRIPT ON MD1Snaps USE canMutateRow="false" onInsertedScript="int ret = processOrders(Symbol);";

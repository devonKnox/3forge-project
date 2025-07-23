package matchingEngine;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;


public class StockConfigLoader {
    // For placing orders from specific accounts
    public static class Account {
        public String name;
        public double activityLevel;
        public double capital;

        @Override
        public String toString() {
        return name + " [activity=" + activityLevel + ", capital=" + capital + "]";
        }
    }

    public static List<Account> loadAccountsFromJSON(String filePath) {
        List<Account> accounts = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject root = new JSONObject(content);

            JSONArray accountArray = root.getJSONArray("accounts");
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject obj = accountArray.getJSONObject(i);
                Account acc = new Account();
                acc.name = obj.getString("name");
                acc.activityLevel = obj.getDouble("activityLevel");
                acc.capital = obj.getDouble("capital");
                accounts.add(acc);
            }
        } catch (Exception e) {
            System.err.println("Failed to load accounts from JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }

    // main script calls this method for when volatility is not a concern
    public static List<StockEntryMD1> loadStocksFromJSON(String filename) throws IOException, JSONException {
    String content = new String(Files.readAllBytes(Paths.get(filename)));
    JSONObject root = new JSONObject(content);

    int numEntries = root.getInt("numEntries");
    JSONArray groupsArray = root.getJSONArray("groups");
    List<StockEntryMD1> list = new ArrayList<>();

    for (int i = 0; i < groupsArray.length(); i++) {
        JSONObject group = groupsArray.getJSONObject(i);
        JSONArray stocksArray = group.getJSONArray("stocks");

        for (int j = 0; j < stocksArray.length(); j++) {
            JSONObject stockObj = stocksArray.getJSONObject(j);
            String symbol = stockObj.getString("symbol");
            double midPrice = stockObj.getDouble("midPrice");
            list.add(new StockEntryMD1(symbol, midPrice, numEntries));
        }
    }

    return list;
    }

}
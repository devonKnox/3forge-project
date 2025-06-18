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
        


    // main script calls this method for when volatility is not a concern
    public static List<StockEntryMD1> loadGroupFromJSON(String filename, String groupName) throws IOException, JSONException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        JSONObject root = new JSONObject(content);
        int numEntries = root.getInt("numEntries");
        JSONArray groupsArray = root.getJSONArray("groups");
        for (int i = 0; i < groupsArray.length(); i++) {
            JSONObject grp = groupsArray.getJSONObject(i);
            if (grp.getString("name").equalsIgnoreCase(groupName)) {
                JSONArray stocksArray = grp.getJSONArray("stocks");
                List<StockEntryMD1> list = new ArrayList<>();
                for (int j = 0; j < stocksArray.length(); j++) {
                    JSONObject stockObj = stocksArray.getJSONObject(j);
                    String symbol = stockObj.getString("symbol");
                    double midPrice = stockObj.getDouble("midPrice");
                    list.add(new StockEntryMD1(symbol, midPrice, numEntries));
                }
                return list;
            }
        }
        throw new IllegalArgumentException("Group '" + groupName + "' not found in " + filename);
    }
}
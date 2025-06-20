
import com.f1.ami.client.AmiClient;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class SimulationRunner {
    public static void main(String[] args) throws Exception {
        // User settings — change as desired
        int simSpeed = 1000; // Time in ms between simulated orders

        // Map of asset classes to their volatility levels
        Map<String, Double> defaultVolatility = Map.of(
            "Auto", 0.5
            // "Tech", 0.7,
            // "Finance",0.6
        );

        // Choose which asset classes to simulate — comment/uncomment as needed
        List<String> assetClassesToSimulate = Arrays.asList(
            "Auto"
            //"Tech"
            // "Finance",
        );

        if (args.length != 4) {
            System.out.println("Usage: java SimulationRunner <config.json> <username> <clientPort> <centerPort>");
            return;
        }

        String configFile = args[0];
        String username = args[1];
        int clientPort = Integer.parseInt(args[2]);
        int centerPort = Integer.parseInt(args[3]);

        for (String assetClass : assetClassesToSimulate) {
            double volatility = defaultVolatility.getOrDefault(assetClass, 1.0);
            System.out.println("Starting simulation for asset class: " + assetClass + " with volatility " + volatility);
            AmiClient baseClient = new AmiClient();
            CenterClient runner = new CenterClient(baseClient);
            runner.run(configFile, username, clientPort, centerPort, assetClass, volatility, simSpeed);
        }
    }
}

import com.f1.ami.client.AmiClient;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class SimulationRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage: java SimulationRunner <config.json> <username> <clientPort> <centerPort> <assetClass>");
            return;
        }

        String configFile = args[0];
        String username = args[1];
        int clientPort = Integer.parseInt(args[2]);
        int centerPort = Integer.parseInt(args[3]);
        int simSpeed = 1000;
        AmiClient baseClient = new AmiClient();
        genSendOrders runner = new genSendOrders(baseClient);
        runner.run(configFile, username, clientPort, centerPort, simSpeed);
    }
}

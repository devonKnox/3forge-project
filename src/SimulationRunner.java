import com.f1.ami.client.AmiClient;

public class SimulationRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage: java SimulationRunner <config.json> <username> <clientPort> <centerPort>");
            return;
        }

        String configFile = args[0];
        String username = args[1];
        int clientPort = Integer.parseInt(args[2]);
        int centerPort = Integer.parseInt(args[3]);

        AmiClient baseClient = new AmiClient();
        CenterClient runner = new CenterClient(baseClient);
        runner.run(configFile, username, clientPort, centerPort);
    }
}

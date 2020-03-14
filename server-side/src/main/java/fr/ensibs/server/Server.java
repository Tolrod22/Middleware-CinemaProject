package fr.ensibs.server;

import fr.ensibs.joramAdmin.JoramAdmin;
import fr.ensibs.joramServer.Joram;
import fr.ensibs.river.River;

import java.util.Scanner;

public class Server {

    /**
     * Default host for River host
     */
    private static final String DEFAULT_RIVER_HOST = "localhost";

    /**
     * Default port for River host
     */
    private static final String DEFAULT_JORAM_HOST = "localhost";

    /**
     * Default host for Joram host
     */
    private static final int DEFAULT_RIVER_PORT = 8065;

    /**
     * Default port for Joram host
     */
    private static final int DEFAULT_JORAM_PORT = 8066;

    /**
     * Main methods launching the Server app.
     *
     * @param args Nothing or -h (for help).
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("-h")) {
            usage();
        }

        String riverHost = DEFAULT_RIVER_HOST;
        int riverPort = DEFAULT_RIVER_PORT;
        String joramHost = DEFAULT_JORAM_HOST;
        int joramPort = DEFAULT_JORAM_PORT;

        try {
            System.out.print("River host:port (press Enter for default : " + DEFAULT_RIVER_HOST + ":" + DEFAULT_RIVER_PORT + ") : ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();

            if (!line.equals("")) {
                String[] command = line.split(":");
                riverHost = command[0];
                riverPort = Integer.parseInt(command[1]);
            }

            System.out.print("Joram host:port (press Enter for default : " + DEFAULT_JORAM_HOST + ":" + DEFAULT_JORAM_PORT + ") : ");
            line = scanner.nextLine();

            if (!line.equals("")) {
                String[] command = line.split(":");
                joramHost = command[0];
                joramPort = Integer.parseInt(command[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (joramPort == riverPort || joramPort + 1 == riverPort) {
            usage();
        }

        try {
            River river = new River(riverHost, riverPort);
            river.run();
            Joram joram = new Joram(joramHost, joramPort);
            joram.run();
        } catch (NumberFormatException e) {
            usage();
        }

        JoramAdmin admin = new JoramAdmin(joramHost, joramPort);
        admin.createTopic("Cinema");
    }

    /**
     * Print a usage message and exit
     */
    private static void usage() {
        System.out.println("Usage : Server : runs a Joram Server and a River Server");
        System.out.println("Settings for servers will be asked after launching");
        System.out.println("riverPort must be diffrent than joramPort and joramPort+1");
        System.exit(-1);
    }
}

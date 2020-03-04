package fr.ensibs.server;

import fr.ensibs.joramAdmin.JoramAdmin;
import fr.ensibs.joramServer.Joram;
import fr.ensibs.river.River;

public class Server {

    private static final String DEFAULT_RIVER_HOST = "localhost"; // default server host
    private static final String DEFAULT_JORAM_HOST = "localhost"; // default server host
    private static final int DEFAULT_RIVER_PORT = 8065; // default server port
    private static final int DEFAULT_JORAM_PORT = 8066; // default server port

    public static void main(String[] args) throws Exception {
        if (args.length % 4 != 0) {
            usage();
        }

        String riverHost = DEFAULT_RIVER_HOST;
        int riverPort = DEFAULT_RIVER_PORT;
        String joramHost = DEFAULT_JORAM_HOST;
        int joramPort = DEFAULT_JORAM_PORT;

        int idx = 0;
        while (idx < args.length - 1) {
            switch (args[idx]) {
                case "-rh":
                    riverHost = args[idx + 1];
                    break;
                case "-rp":
                    try {
                        riverPort = Integer.parseInt(args[idx + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        usage();
                    }
                    break;
                case "-jh":
                    joramHost = args[idx + 1];
                    break;
                case "-jp":
                    try {
                        joramPort = Integer.parseInt(args[idx + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        usage();
                    }
                    break;
                default:
                    usage();
            }
            idx += 2;
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
        System.out.println("Usage : Server [options]");
        System.out.println("with options :");
        System.out.println("  -rh riverHost   the local host name for River (default : " + DEFAULT_RIVER_HOST + ")");
        System.out.println("  -rp riverPort   the server's port number for River (default : " + DEFAULT_RIVER_PORT + ")");
        System.out.println("  -jh joramHost   the local host name for Joram (default : " + DEFAULT_JORAM_HOST + ")");
        System.out.println("  -jp joramPort   the server's port number for Joram (default : " + DEFAULT_JORAM_PORT + ")");
        System.out.println("  riverPort must be diffrent than joramPort and joramPort+1");
        System.exit(-1);
    }
}

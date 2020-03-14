package fr.ensibs.cinema;

import fr.ensibs.RiverLookup;
import net.jini.space.JavaSpace;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

public class CinemaApp {

    /**
     * Context for the app
     */
    private static Context context = null;

    /**
     * Connection for the app
     */
    private static Connection connection = null;

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
     * Main methods launching the Cinema app.
     *
     * @param args Nothing or -h (for help).
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-h")) {
            usage();
        }

        String cinema_name = "";
        String riverHost = DEFAULT_RIVER_HOST;
        int riverPort = DEFAULT_RIVER_PORT;
        String joramHost = DEFAULT_JORAM_HOST;
        int joramPort = DEFAULT_JORAM_PORT;

        try {
            System.out.print("Cinema name : ");
            Scanner scanner = new Scanner(System.in);
            cinema_name = scanner.nextLine();
            while (cinema_name.equals("")) {
                System.out.print("Cinema name : ");
                cinema_name = scanner.nextLine();
            }

            System.out.print("River host:port (press Enter for default : " + DEFAULT_RIVER_HOST + ":" + DEFAULT_RIVER_PORT + ") : ");
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

        try {
            String dest = "Cinema";

            System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
            System.setProperty("java.naming.factory.host", joramHost);
            System.setProperty("java.naming.factory.port", Integer.toString(joramPort));
            System.out.println("Error stream assigned to \"logs/cinema.err\"");
            File dir = new File("logs");
            dir.mkdirs();
            System.setErr(new PrintStream("logs/cinema.err"));
            context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            Destination destination = (Destination) context.lookup(dest);

            connection = factory.createConnection();
            Session sessionProducer = connection.createSession();
            MessageProducer producer = sessionProducer.createProducer(destination);
            connection.start();

            JavaSpace space = new RiverLookup().lookup(riverHost, riverPort, JavaSpace.class);

            Cinema instance = new Cinema(cinema_name, sessionProducer, producer, space);
            instance.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Print a usage message and exit
     */
    private static void usage() {
        System.out.println("Usage: java Cinema <cinema_name> <cinema_city> <riverHost> <riverPort> <joramHost> <joramPort>");
        System.out.println("Launch the cinema application");
        System.out.println("with:");
        System.out.println("<cinema_name>   the name of your cinema");
        System.out.println("<cinema_city>   the city of your cinema");
        System.out.println("<riverHost>     the name or IP address of the river server");
        System.out.println("<riverPort>     the port numer of the river server");
        System.out.println("<joramHost>     the name or IP address of the joram server");
        System.out.println("<joramPort>     the port numer of the joram server");
        System.exit(0);
    }
}

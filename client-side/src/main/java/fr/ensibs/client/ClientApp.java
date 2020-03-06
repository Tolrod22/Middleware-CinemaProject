package fr.ensibs.client;

import fr.ensibs.RiverLookup;
import net.jini.space.JavaSpace;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class ClientApp {

    private static Context context = null;
    private static Connection connection = null;

    /**
     * Main methods launching the Client app.
     *
     * @param args The cinema name and the city name, The host name and the port for River server and JMS Server.
     */
    public static void main(String[] args) {
        if (args.length != 5) {
            usage();
        }

        String client_name = args[0];
        String riverHost = args[1];
        String riverPort = args[2];
        String joramHost = args[3];
        String joramPort = args[4];

        try {
            String dest = "Cinema";

            System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
            System.setProperty("java.naming.factory.host", joramHost);
            System.setProperty("java.naming.factory.port", joramPort);
            context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            Destination destination = (Destination) context.lookup(dest);

            connection = factory.createConnection();
            Session sessionConsumer = connection.createSession();
            MessageConsumer consumer = sessionConsumer.createConsumer(destination);
            connection.start();

            JavaSpace space = new RiverLookup().lookup(riverHost, Integer.parseInt(riverPort), JavaSpace.class);

            Client instance = new Client(client_name, sessionConsumer, consumer, destination, space);
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
        System.out.println("Usage: java Client <client_name> <riverHost> <riverPort> <joramHost> <joramPort>");
        System.out.println("Launch the client application");
        System.out.println("with:");
        System.out.println("<client_name>   your name");
        System.out.println("<riverHost>     the name or IP address of the river server");
        System.out.println("<riverPort>     the port numer of the river server");
        System.out.println("<joramHost>     the name or IP address of the joram server");
        System.out.println("<joramPort>     the port numer of the joram server");
        System.exit(0);
    }
}

package fr.ensibs.cinema;

import fr.ensibs.RiverLookup;
import net.jini.space.JavaSpace;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.PrintStream;

public class CinemaApp {

    private static Context context = null;
    private static Connection connection = null;

    /**
     * Main methods launching the Cinema app.
     *
     * @param args The cinema name and the city name, The host name and the port for River server and JMS Server.
     */
    public static void main(String[] args) {
        if (args.length != 6) {
            usage();
        }

        String cinema_name = args[0];
        String cinema_city = args[1];
        String riverHost = args[2];
        String riverPort = args[3];
        String joramHost = args[4];
        String joramPort = args[5];

        try {
            String dest = "Cinema";

            System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
            System.setProperty("java.naming.factory.host", joramHost);
            System.setProperty("java.naming.factory.port", joramPort);
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

            JavaSpace space = new RiverLookup().lookup(riverHost, Integer.parseInt(riverPort), JavaSpace.class);

            Cinema instance = new Cinema(cinema_name, cinema_city, sessionProducer, producer, space);
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

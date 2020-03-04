package fr.ensibs.joramServer;

import fr.dyade.aaa.agent.AgentServer;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.*;

/**
 * Class that allows to start a JORAM server. Can be used as a standalone
 * application or by creating an instance and invoking its start or run method
 * to start the server asynchronousely or synchronousely
 */
public class Joram extends Thread implements Closeable {

    /**
     * The JORAM server id
     */
    private static final int SERVER_ID = 0;

    /**
     * The name of the ConnectionFactory object in the JNDI repository
     */
    private static final String FACTORY_NAME = "ConnectionFactory";

    /**
     * the local host name
     */
    private String host;

    /**
     * the jndi port number. The server port number is port+1
     */
    private int port;

    /**
     * Constructor
     *
     * @param host the JNDI server host address or name
     * @param port the JNDI server port number
     */
    public Joram(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        makeConfig();
    }

    /**
     * Start the JORAM server
     */
    @Override
    public void run() {
        try {
            File directory = new File(System.getProperty("user.home"), ".joram");
            String[] serverArgs = {Integer.toString(SERVER_ID), directory.getAbsolutePath()};

            AgentServer.main(serverArgs);
            Thread.sleep(1000);
            makeConnectionFactory();

            System.out.println("JORAM server running on " + host + ":" + port);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Stop the JORAM server
     */
    @Override
    public void close() {
        AgentServer.stop();
    }

    /**
     * Create a ConnectionFactory instance and register it to the JNDI
     * repository using the {@link #SERVER_ID} and {@link #port}
     * properties
     */
    private void makeConnectionFactory() throws Exception {
        Context context = null;
        try {
            // connect to the admin module and create an anonymous user
            AdminModule.connect("localhost", port + 1, "root", "root");
            User.create("anonymous", "anonymous", SERVER_ID);

            // create a ConnectionFactory object and register it
            ConnectionFactory factory = TcpConnectionFactory.create(host, port + 1);
            System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
            System.setProperty("java.naming.factory.host", "localhost");
            System.setProperty("java.naming.factory.port", Integer.toString(port));
            context = new InitialContext();
            context.rebind(FACTORY_NAME, factory);

        } finally {
            // close all resources
            if (context != null) {
                try {
                    context.close();
                } catch (Exception ignored) {
                }
            }
            AdminModule.disconnect();
        }
    }

    /**
     * Make the config file in the user working directory using the
     * {@link #SERVER_ID} and {@link #port} properties
     */
    private void makeConfig() throws IOException {
        File dir = new File(System.getProperty("user.dir"));
        File configFile = new File(dir, "a3servers.xml");
        configFile.deleteOnExit();

        String[] lines = {
                "<?xml version=\"1.0\"?>",
                "<config>",
                "  <property name=\"Transaction\" value=\"fr.dyade.aaa.util.NTransaction\" />",
                "  <server id=\"" + SERVER_ID + "\" name=\"joram\" hostname=\"" + host + "\">",
                "    <service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\"",
                "             args=\"root root\" />",
                "    <service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\"",
                "             args=\"" + (port + 1) + "\" />",
                "    <service class=\"fr.dyade.aaa.jndi2.server.JndiServer\"",
                "      args=\"" + port + "\" />",
                "  </server>",
                "</config>"
        };

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
    }
}

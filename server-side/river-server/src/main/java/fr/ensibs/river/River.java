package fr.ensibs.river;

import com.sun.jini.start.ServiceStarter;

import java.io.File;
import java.io.PrintStream;
import java.rmi.RMISecurityManager;

/**
 * Class used to start the fr.ensibs.river.River JavaSpace service and allow a client
 * application to look for this service
 *
 * @author launay
 */
public class River extends Thread {

    public static String HOST; // the server host name
    public static int PORT;    // the server port number

    /**
     * Constructor
     *
     * @param host the local host name
     * @param port the server's port number
     */
    public River(String host, int port) {
        River.HOST = host;
        River.PORT = port;
        System.setSecurityManager(new RMISecurityManager());
    }

    /**
     * Start the JavaSpace service
     */
    @Override
    public void run() {
        if (!new File(RiverConfiguration.POLICY).exists()) {
            System.err.println("Unable to start the fr.ensibs.river.River services: no \"" + RiverConfiguration.POLICY + "\" file");
        } else {
            try {
                System.setProperty("java.security.manager", "true");
                System.setProperty("java.security.policy", RiverConfiguration.POLICY);
                System.setProperty("java.rmi.server.useCodebaseOnly", "false");
                System.setProperty("java.rmi.server.hostname", River.HOST);

                System.out.println("Error stream assigned to \"river.err\"");
                System.setErr(new PrintStream("river.err"));

                RiverConfiguration config = new RiverConfiguration(HOST, PORT);
                ServiceStarter.main(config);
                System.out.println("fr.ensibs.river.River services successfully started at " + HOST + ":" + PORT);
            } catch (Exception ex) {
                System.err.println("Unable to start the fr.ensibs.river.River services");
                ex.printStackTrace();
            }
        }
    }
}

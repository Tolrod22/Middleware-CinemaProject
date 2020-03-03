package fr.ensibs.cinema;

import fr.ensibs.RiverLookup;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Scanner;

public class Cinema {

    /**
     * The shared JavaSpace where all the objects are stored.
     */
    private JavaSpace space;

    /**
     * The server host name
     */
    private String host;

    /**
     * The server host port
     */
    private int port;

    /**
     * The cinema name
     */
    private String name;

    /**
     * The cinema city
     */
    private String city;

    /**
     * Constructor of the app instance
     *
     * @param host the host
     * @param port the port
     * @param name the name
     * @param city the city
     * @throws Exception Throw an exception if the connection cannot be done.
     */
    public Cinema(String host, String port, String name, String city) throws Exception {
        this.name = name;
        this.city = city;
        this.host = host;
        this.port = Integer.parseInt(port);
        this.space = new RiverLookup().lookup(host, Integer.parseInt(port), JavaSpace.class);
    }

    /**
     * This method is managing all the user interactions with the app.
     *
     * @param command The last command used by the user.
     * @param scanner The scanner instance.
     * @return A response message to inform the user of the remote space evolution.
     * @throws RemoteException
     * @throws TransactionException
     * @throws InterruptedException
     * @throws UnusableEntryException
     * @throws ParseException
     */
    private String manageCommand(String command, Scanner scanner) throws Exception {
        String[] splited = command.split(" ");

        switch (splited[0]) {
            case "get":
            case "GET":
                return "Get command"; //TODO

            case "add movie":
            case "ADD movie": //TODO publish the movie added and add tickets to the shared memory

            case "remove movie":
            case "REMOVE movie":
            default:
                return "Error : Command error, use -h for more information"; //TODO
        }
    }

    /**
     * Main methods launching the FlightReservation app.
     *
     * @param args The host name and the port.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        Cinema app = new Cinema(args[0], args[1], args[2], args[3]);
        System.out.print("System is on, ready to be used !!!\n");

        while (true) {
            String cmd = input.nextLine();
            System.out.println(app.manageCommand(cmd, input));
        }
    }
}

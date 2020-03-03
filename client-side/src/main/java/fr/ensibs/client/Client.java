package fr.ensibs.client;

import fr.ensibs.RiverLookup;
import fr.ensibs.shareable.Ticket;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class is an app who allow the user to manage an flight reservation system with Airports, Flights, Seats and a bank account system.
 */
public class Client {

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

    private String name;

    private ArrayList<Ticket> ticketList;

    /**
     * Constructor of the app instance
     *
     * @param host the host
     * @param port the port
     * @param name the name
     * @throws Exception Throw an exception if the connection cannot be done.
     */
    public Client(String host, String port, String name) throws Exception {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.name = name;
        this.ticketList = new ArrayList<>();
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

            case "publish ticket":
            case "PUBLISH ticket":
                return "publish command"; //TODO

            case "request ticket":
            case "REQUEST ticket":
                return "request command"; //TODO
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
        Client app = new Client(args[0], args[1], args[2]);
        System.out.print("System is on, ready to be used !!!\n");

        while (true) {
            String cmd = input.nextLine();
            System.out.println(app.manageCommand(cmd, input));
        }
    }
}

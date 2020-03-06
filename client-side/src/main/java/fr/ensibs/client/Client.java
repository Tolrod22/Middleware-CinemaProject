package fr.ensibs.client;

import fr.ensibs.RiverLookup;
import fr.ensibs.shareable.Ticket;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import java.io.IOException;
import java.rmi.MarshalledObject;
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

    private RemoteEventListener listener = remoteEvent -> System.out.println("Coucou");

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

    private Ticket getTicket(String movieName, String cinemaName) throws TransactionException, UnusableEntryException, RemoteException, InterruptedException {
        Ticket template = new Ticket(movieName, null, cinemaName, null);
        return (Ticket) space.take(template, null, Lease.DURATION);
    }

    private void requestTicket(String movieName) throws IOException, TransactionException {
        Ticket template = new Ticket(movieName, null, null, null);

        String toMarshall = "marchall me";
        MarshalledObject<String> mo = new MarshalledObject<>(toMarshall);
        space.notify(template, null, new RemoteEventListener() {
            @Override
            public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
                mo.notify();
            }
        }, Lease.ANY, null);
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
                System.out.print("Movie name : ");
                String movieName = scanner.nextLine();

                System.out.print("Cinema name : ");
                String cinemaName = scanner.nextLine();

                Ticket theTicket = getTicket(movieName, cinemaName);
                if (theTicket != null) {
                    theTicket.owner = this;
                    ticketList.add(theTicket);
                    return "You just get a ticket for " + theTicket.movieName;
                } else {
                    return "No ticket remaining for " + movieName;
                }

            case "publish":
            case "PUBLISH":
                return "publish command"; //TODO

            case "request":
            case "REQUEST":
                System.out.print("Movie name : ");
                String movieRequest = scanner.nextLine();
                requestTicket(movieRequest); //TODO
                return "You are now requesting ticket for " + movieRequest;
            default:
                return "Error : Command error, use -h for more information"; //TODO
        }
    }

    /**
     * Main methods launching the Client app.
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

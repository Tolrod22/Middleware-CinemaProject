package fr.ensibs.client;

import fr.ensibs.shareable.Ticket;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import javax.jms.*;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class is an app who allow the user to manage an flight reservation system with Airports, Flights, Seats and a bank account system.
 */
public class Client {

    /**
     * The client name
     */
    private String name;

    private ArrayList<Ticket> ticketList;

    private RemoteEventListener listener = remoteEvent -> System.out.println("Coucou");

    private ArrayList<String> cinemas;

    /**
     * The session used to receive messages from the server
     */
    private final Session sessionConsumer;

    /**
     * The consumer used to receive messages from the server
     */
    private MessageConsumer consumer;

    /**
     * The default destination of each message
     */
    private final Destination destination;

    /**
     * The shared JavaSpace where all the objects are stored.
     */
    private JavaSpace space;

    /**
     * @param name            the client name
     * @param sessionConsumer the session used to receive messages from the server
     * @param consumer        the consumer used to receive messages from the server
     * @param destination     the default destination of each message
     * @param space           the JavaSpace used to share objects
     */
    public Client(String name, Session sessionConsumer, MessageConsumer consumer, Destination destination, JavaSpace space) {
        this.name = name;
        this.sessionConsumer = sessionConsumer;
        this.consumer = consumer;
        this.destination = destination;
        this.space = space;
        this.cinemas = new ArrayList<>();
        this.ticketList = new ArrayList<>();
    }

    /**
     * Launch the application process that executes user commands
     */
    public void run() throws Exception {
        System.out.println("Hello, " + this.name + ". Enter commands:"
                + "\n QUIT      to quit the application"
                + "\n GET       to get a ticket for a movie"
                + "\n FILTER    to sub to a special cinema"
                + "\n UNFILTER  to unsub to a special cinema"
                + "\n PUBLISH   to publish tickets for a movie"
                + "\n REQUEST   to withdraw tickets for a movie");

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!line.equals("quit") && !line.equals("QUIT")) {
            String[] command = line.split(" +");
            switch (command[0]) {
                case "filter":
                case "FILTER":
                    System.out.print("Cinema name : ");
                    String cinemaNameAdded = scanner.nextLine();
                    addFilter(cinemaNameAdded);
                    break;
                case "unfilter":
                case "UNFILTER":
                    System.out.print("Cinema name : ");
                    String cinemaNameRemoved = scanner.nextLine();
                    removeFilter(cinemaNameRemoved);
                    break;
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
                        System.out.println("You just get a ticket for " + theTicket.movieName);
                    } else {
                        System.out.println("No ticket remaining for " + movieName);
                    }
                    break;
                case "publish":
                case "PUBLISH":
                    // TODO
                    break;
                case "request":
                case "REQUEST":
                    System.out.print("Movie name : ");
                    String movieRequest = scanner.nextLine();
                    requestTicket(movieRequest); //TODO
                    System.out.println("You are now requesting ticket for " + movieRequest);
                    break;
                default:
                    System.err.println("Unknown command: \"" + command[0] + "\"");
            }
            line = scanner.nextLine();
        }
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

    private void addFilter(String cinemaAdded) throws JMSException {
        this.cinemas.add(cinemaAdded);
        filter();
    }


    private void removeFilter(String cinemaRemoved) throws JMSException {
        this.cinemas.remove(cinemaRemoved);
        filter();
    }

    /**
     * Specify the photos the user is interested in by setting new tags
     */
    public void filter() throws JMSException {
        if (consumer != null) {
            consumer.close();
        }
        consumer = sessionConsumer.createConsumer(destination);
        consumer.setMessageListener(msg -> {
            try {
                if (msg != null) {
                    String cinema = msg.getStringProperty("owner");
                    if (cinemas.contains(cinema)) {
                        String movieName = msg.getStringProperty("movieName");
                        System.out.println(movieName + " available in " + cinema);
                    }
                } else {
                    System.out.println("Aucun film disponible");
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Filter on " + cinemas + " has been set");
    }
}

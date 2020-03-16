package fr.ensibs.client;

import fr.ensibs.shareable.Ticket;
import net.jini.core.entry.Entry;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The class is an app who allow the user to manage an flight reservation system with Airports, Flights, Seats and a bank account system.
 */
public class Client implements Serializable, Entry {

    /**
     * The client name
     */
    private String identifiant;

    /**
     * the list of tickets the user have
     */
    private ArrayList<Ticket> ticketList;

    /**
     * the list of cinemas the user has subscribed
     */
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
     * @param identifiant     the client name
     * @param sessionConsumer the session used to receive messages from the server
     * @param consumer        the consumer used to receive messages from the server
     * @param destination     the default destination of each message
     * @param space           the JavaSpace used to share objects
     */
    public Client(String identifiant, Session sessionConsumer, MessageConsumer consumer, Destination destination, JavaSpace space) {
        this.identifiant = identifiant;
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
        System.out.println("Hello, " + this.identifiant + ". Enter commands:"
                + "\n QUIT      to quit the application"
                + "\n GET       to get a ticket for a movie"
                + "\n FILTER    to sub to a special cinema"
                + "\n UNFILTER  to unsub to a special cinema"
                + "\n PUBLISH   to publish tickets for a movie"
                + "\n REQUEST   to withdraw tickets for a movie"
                + "\n DISPLAY   to show your actual ticket list");

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
                        theTicket.owner = this.identifiant;
                        ticketList.add(theTicket);
                        System.out.println("You just get a ticket for " + theTicket.movieName);
                    } else {
                        System.out.println("No ticket remaining for " + movieName);
                    }
                    break;
                case "publish":
                case "PUBLISH":
                    System.out.println(ticketListToString());
                    System.out.print("Select the index of the ticket you want to publish: ");
                    int index = scanner.nextInt();
                    if (index >= 0 && index <= ticketList.size()) {
                        space.write(ticketList.get(index), null, Lease.FOREVER);
                        ticketList.remove(index);
                        System.out.println("Ticket successfully published");
                    } else {
                        System.out.println("Error in the ticket selection please retry");
                    }
                    break;
                case "request":
                case "REQUEST":
                    System.out.print("Movie name : ");
                    String movieRequest = scanner.nextLine();
                    requestTicket(movieRequest);
                    System.out.println("You are now requesting ticket for " + movieRequest);
                    break;
                case "display":
                case "DISPLAY":
                    System.out.println(ticketListToString());
                    break;
                default:
                    System.err.println("Unknown command: \"" + command[0] + "\"");
            }
            line = scanner.nextLine();
        }
    }

    /**
     * Gives the list of tickets in a String format
     *
     * @return the list of tickets in a String format
     */
    private String ticketListToString() {
        StringBuilder builder = new StringBuilder();
        for (Ticket ticket : ticketList) {
            builder.append("[").append(ticketList.indexOf(ticket)).append("] ");
            builder.append(ticket.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Request a ticket for a movie for a cinema on the JavaSpace server
     *
     * @param movieName  the movie name
     * @param cinemaName the cinema name
     * @return a ticket (if exists) or null
     * @throws Exception
     */
    private Ticket getTicket(String movieName, String cinemaName) throws Exception {
        Ticket template = new Ticket(movieName, null, cinemaName, null);
        return (Ticket) space.take(template, null, Lease.DURATION);
    }

    /**
     * Notify the user when a ticket is available for a movie
     *
     * @param movieName the movie name
     * @throws IOException
     * @throws TransactionException
     */
    private void requestTicket(String movieName) throws IOException, TransactionException {
        Ticket template = new Ticket(movieName, null, null, null);
        RemoteEventListener listener = event -> {
            try {
                Ticket tmp = (Ticket) space.read(template, null, Lease.FOREVER);
                if (tmp.owner != null) {
                    Ticket tk = (Ticket) space.take(tmp, null, Lease.FOREVER);
                    System.out.println("You get a ticket for " + tk.movieName + " from " + tk.owner);
                    tk.owner = Client.this.identifiant;
                    Client.this.ticketList.add(tk);
                }
            } catch (Exception ex) {
                System.err.println("Error while taking entry");
                ex.printStackTrace();
            }
        };
        RemoteEventListener stub = (RemoteEventListener) UnicastRemoteObject.exportObject(listener, 0);
        space.notify(template, null, stub, Lease.FOREVER, null);
    }

    /**
     * Add a cinema to the filter list of cinemas
     *
     * @param cinemaAdded the cinema name to add
     * @throws JMSException
     */
    private void addFilter(String cinemaAdded) throws JMSException {
        this.cinemas.add(cinemaAdded);
        filter();
    }

    /**
     * Add a cinema from the filter list of cinemas
     *
     * @param cinemaRemoved the cinema name to remove
     * @throws JMSException
     */
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

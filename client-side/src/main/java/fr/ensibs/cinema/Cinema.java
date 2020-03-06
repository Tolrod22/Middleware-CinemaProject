package fr.ensibs.cinema;

import fr.ensibs.shareable.Ticket;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import javax.jms.*;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

public class Cinema {

    /**
     * The cinema name
     */
    private String name;

    /**
     * The cinema city
     */
    private String city;

    /**
     * The session used to send messages to the server
     */
    private final Session sessionProducer;

    /**
     * The producer used to send messages to the server
     */
    private final MessageProducer producer;

    /**
     * The default destination of each message
     */
    private final Destination destination;

    /**
     * The shared JavaSpace where all the objects are stored.
     */
    private JavaSpace space;

    /**
     * @param name            the cinema name
     * @param city            the cinema city
     * @param sessionProducer the session used to send messages to the server
     * @param producer        the producer used to send messages to the server
     * @param destination     the default destination of each message
     * @param space           the JavaSpace used to share objects
     */
    public Cinema(String name, String city, Session sessionProducer, MessageProducer producer, Destination destination, JavaSpace space) {
        this.name = name;
        this.city = city;
        this.sessionProducer = sessionProducer;
        this.producer = producer;
        this.destination = destination;
        this.space = space;
    }

    /**
     * Launch the application process that executes user commands
     */
    public void run() throws Exception {
        System.out.println("Hello, " + this.name + ". Enter commands:"
                + "\n QUIT      to quit the application"
                + "\n ADD       to share a new available movie"
                + "\n REMOVE    to remove an unavailable movie");

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!line.equals("quit") && !line.equals("QUIT")) {
            String[] command = line.split(" +");
            switch (command[0]) {
                case "add":
                case "ADD":
                    System.out.print("Movie name : ");
                    String movieName = scanner.nextLine();
                    shareMovie(movieName);
                    System.out.print("How many tickets : ");
                    int places = scanner.nextInt();
                    boolean res = addTickets(movieName, places);
                    if (res)
                        System.out.println(movieName + " successfully added to the space with " + places + " tickets.");
                    else System.out.println("Error on adding " + movieName);
                    break;
                case "remove":
                case "REMOVE":
                    System.out.print("Movie name : ");
                    String movieToRemove = scanner.nextLine();
                    System.out.println(removeMovie(movieToRemove));
                    break;
                default:
                    System.err.println("Unknown command: \"" + command[0] + "\"");
            }
            line = scanner.nextLine();
        }
    }


    private boolean addTickets(String movieName, int numberOfTicket) throws RemoteException, TransactionException {
        try {
            String masterRand = UUID.randomUUID().toString();
            for (int i = 0; i < numberOfTicket; i++) {
                Ticket ticket = new Ticket(movieName, masterRand + i, this.name, null);
                space.write(ticket, null, 60 * 60 * 1000);
            }
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @param movieName
     * @return
     * @throws Exception
     */
    private String removeMovie(String movieName) throws Exception {
        Ticket template = new Ticket(movieName, null, this.name, null);
        Ticket test = (Ticket) space.take(template, null, Lease.DURATION);
        while (test != null) test = (Ticket) space.take(template, null, Lease.DURATION);
        return "All your tickets for " + movieName + " were deleted";
    }

    private void shareMovie(String name) throws JMSException, IOException {
        Message msg = sessionProducer.createMessage();

        msg.setStringProperty("owner", this.name);
        msg.setStringProperty("movieName", name);

        producer.send(msg);
    }
}

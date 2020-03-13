package fr.ensibs.cinema;

import fr.ensibs.shareable.Ticket;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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
     * The shared JavaSpace where all the objects are stored.
     */
    private JavaSpace space;

    /**
     * The list of movies available in the cinema
     */
    private List<String> availableMovies;

    /**
     * @param name            the cinema name
     * @param city            the cinema city
     * @param sessionProducer the session used to send messages to the server
     * @param producer        the producer used to send messages to the server
     * @param space           the JavaSpace used to share objects
     */
    public Cinema(String name, String city, Session sessionProducer, MessageProducer producer, JavaSpace space) {
        this.name = name;
        this.city = city;
        this.sessionProducer = sessionProducer;
        this.producer = producer;
        this.space = space;
        this.availableMovies = new ArrayList<>();
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
                    if (this.availableMovies.contains(movieName)) {
                        System.out.println(movieName + " already available in your cinema");
                    } else {
                        System.out.print("Number of tickets : ");
                        int places = scanner.nextInt();
                        if (places > 0) {
                            addTickets(movieName, places);
                            shareMovie(movieName);
                            this.availableMovies.add(movieName);
                            System.out.println(movieName + " successfully added to the space with " + places + " tickets.");
                        } else {
                            System.out.println("Number of tickets must be more than 0");
                        }
                    }
                    break;
                case "remove":
                case "REMOVE":
                    System.out.print("Movie name : ");
                    String movieToRemove = scanner.nextLine();
                    if (!this.availableMovies.contains(movieToRemove)) {
                        System.out.println(movieToRemove + " is already unavailable in your cinema");
                    } else {
                        removeMovie(movieToRemove);
                        this.availableMovies.remove(movieToRemove);
                        System.out.println(movieToRemove + " successfully removed from your cinema");
                    }
                    break;
                default:
                    System.err.println("Unknown command: \"" + command[0] + "\"");
            }
            line = scanner.nextLine();
        }
    }

    /**
     * Method used to add tickets for an available movie
     *
     * @param movieName       the name of the movie
     * @param numberOfTickets the number of tickets to add
     * @throws RemoteException
     * @throws TransactionException
     */
    private void addTickets(String movieName, int numberOfTickets) throws RemoteException, TransactionException {
        String masterRand = UUID.randomUUID().toString();
        for (int i = 0; i < numberOfTickets; i++) {
            Ticket ticket = new Ticket(movieName, masterRand + i, this.name, null);
            space.write(ticket, null, 60 * 60 * 1000);
        }
    }

    /**
     * Method used to remove a movie
     *
     * @param movieName the name of the movie to remove
     * @throws Exception
     */
    private void removeMovie(String movieName) throws Exception {
        Ticket template = new Ticket(movieName, null, this.name, null);
        Ticket test = (Ticket) space.take(template, null, Lease.DURATION);
        while (test != null) {
            test = (Ticket) space.take(template, null, Lease.DURATION);
        }
    }

    /**
     * Method used to share a movie on the JMS server
     *
     * @param name the name of the movie
     * @throws JMSException
     */
    private void shareMovie(String name) throws JMSException {
        Message msg = sessionProducer.createMessage();

        msg.setStringProperty("owner", this.name);
        msg.setStringProperty("movieName", name);

        producer.send(msg);
    }
}

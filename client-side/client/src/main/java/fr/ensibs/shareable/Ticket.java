package fr.ensibs.shareable;

import net.jini.core.entry.Entry;

public class Ticket implements Entry {

    /**
     * the name of the movie
     */
    public String movieName;

    /**
     * the ticket id
     */
    public String ticketId;

    /**
     * the cinema where the ticket can be used
     */
    public String cinema;

    /**
     * the owner of the ticket
     */
    public String owner;

    /**
     * Empty constructor
     */
    public Ticket() {
    }

    /**
     * Constructor
     *
     * @param movieName the name of the movie
     * @param ticketId  the ticket id
     * @param cinema    the cinema where the ticket can be used
     * @param owner     the owner of the ticket
     */
    public Ticket(String movieName, String ticketId, String cinema, String owner) {
        this.movieName = movieName;
        this.ticketId = ticketId;
        this.cinema = cinema;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "movieName='" + movieName + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", cinema='" + cinema + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}

package fr.ensibs.shareable;

import net.jini.core.entry.Entry;

public class Ticket implements Entry {

    public String movieName;

    public String ticketId;

    public String cinema;

    public String owner;

    public Ticket() {
    }

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

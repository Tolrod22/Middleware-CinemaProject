package fr.ensibs.shareable;

import fr.ensibs.client.Client;
import net.jini.core.entry.Entry;

public class Ticket implements Entry {

    public String movieName;

    public String ticketId;

    public String cinema;

    public Client owner = null;

    public Ticket() {
    }

    public Ticket(String movieName, String ticketId, String cinema, Client owner) {
        this.movieName = movieName;
        this.ticketId = ticketId;
        this.cinema = cinema;
        this.owner = owner;
    }
}

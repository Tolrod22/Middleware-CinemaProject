package fr.ensibs.shareable;

import fr.ensibs.client.Client;

public class Ticket {

    private String movieName;

    private String ticketId;

    private String cinema;

    private Client owner = null;

    public Ticket(String movieName, String ticketId, String cinema, Client owner) {
        this.movieName = movieName;
        this.ticketId = ticketId;
        this.cinema = cinema;
        this.owner = owner;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCinema() {
        return cinema;
    }

    public void setCinema(String cinema) {
        this.cinema = cinema;
    }

    public Client getOwner() {
        return owner;
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }
}

package edu.uci.ics.fabflixmobile;

import java.util.ArrayList;

public class Movie {
    private final String id;
    private final String name;
    private final short year;
    private final String director;
    private double rating;
    private ArrayList<Star> stars;
    private ArrayList<Genre> genres;

    public Movie(String id, String name, short year,String director) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.rating = 0.0;
        this.stars = new ArrayList<Star>();
        this.genres = new ArrayList<Genre>();
    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() { return director; }

    public double getRating() { return rating; }

    public ArrayList<Star> getStars() { return stars;}

    public ArrayList<Genre> getGenres() { return genres;}

    public void setRating(double rate) { this.rating = rate; }

    public void addStar(Star star) { stars.add(star); }

    public void addGenre(Genre genre) { genres.add(genre); }

}
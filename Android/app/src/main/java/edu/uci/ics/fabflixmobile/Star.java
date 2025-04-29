package edu.uci.ics.fabflixmobile;

public class Star {
    private String id;
    private String name;
    private short birthYear;

    public Star(String id, String name, short year) {
        this.id = id;
        this.name = name;
        this.birthYear = year;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public short getBirthYear() { return birthYear; }

}

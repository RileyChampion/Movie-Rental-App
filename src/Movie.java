import java.util.ArrayList;

public class Movie {

    private String XMLid;
    private String id;
    private String title;
    private String directorName;
    private int releaseYear;
    private ArrayList<Genre> genresList;

    public Movie(String id) {
        this.id = id;
        this.genresList = new ArrayList<>();
        this.title = "";
        this.directorName = "";
        this.releaseYear = -1;
    }

    public void setXMLid(String xmlId) {
        this.XMLid = xmlId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirectorName(String director) {
        this.directorName = director;
    }

    public void setReleaseYear(int year) {
        this.releaseYear = year;
    }

    public void addGenre(Genre currentGenre) {
        this.genresList.add(currentGenre);
    }

    public String getXMLid(){
        return XMLid;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDirectorName() {
        return directorName;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public ArrayList<Genre> getGenresList() {
        return this.genresList;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("id: " + getId());
        sb.append(", ");
        sb.append("title: " + getTitle());
        sb.append(", ");
        sb.append("directorName: " + getDirectorName());
        sb.append(", ");
        sb.append("releaseYear:" + getReleaseYear());
        sb.append(".");

        return sb.toString();
    }

}
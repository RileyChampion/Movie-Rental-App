
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.sql.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovieParser extends DefaultHandler implements MySqlAuth{
    private XMLReader reader;

    //List<Employee> myEmpls;
    // private String myQuery;
    private Map<String, Map<String, Integer>> seenMovies;
    private Map<String, String> movieGenres;
    private Map<String, Movie> xmlIdToMovie;

    private String tempVal;
    private int currStarId;
    private int batchSize;

    private Connection conn;
    private CallableStatement statementGenre;
    private CallableStatement statementGenreInMovies;
    private CallableStatement statementMovies;
    private CallableStatement statementStarsInMovies;

    //to maintain context
    //private Employee tempEmp;
    private Movie tempMovie;
    private Star tempStar;
    private Genre tempGenre;

    public MovieParser() {
        //myEmpls = new ArrayList<Employee>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySqlAuth.loginurl, MySqlAuth.username, MySqlAuth.password);
            PreparedStatement getMaxIdQuery = conn.prepareStatement("SELECT cast(substr(max(id),3) AS UNSIGNED) as maxId FROM movies");
            ResultSet rs = getMaxIdQuery.executeQuery();

            while(rs.next()) {
                currStarId = Integer.parseInt(rs.getString("maxId"));
            }

            conn.setAutoCommit(false);
            statementGenre = conn.prepareCall("{call add_genre(?)}");
            statementGenreInMovies = conn.prepareCall("{call add_genre_in_movies(?,?)}");
            statementMovies = conn.prepareCall("{call add_movie_only(?,?,?,?)}");
            statementStarsInMovies = conn.prepareCall("{call add_star_in_movies(?,?)}");


        }
        catch (Exception e) {
            System.out.println("Database Error: 500");
        }
        
        
        seenMovies = new HashMap<String, Map<String, Integer>>();
        movieGenres = new HashMap<>();
        xmlIdToMovie = new HashMap<>();
        populateMovieGenresMap();
        tempVal = "";
        batchSize = 0;


    }

    private void populateMovieGenresMap() {
        File file = new File("genreMapping/movieGenres.txt");
        Scanner sc;
        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] splitString = line.split(" ");
                movieGenres.put(splitString[0].toUpperCase(), splitString[1]);
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void runExample() {
        parseDocument();
    }

    public void runExample2() {
        parseSecondDocument();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("xml/mains243.xml", this);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void parseSecondDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("xml/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void finishConnection() {

        try{
            statementGenre.executeBatch();
            statementGenreInMovies.executeBatch();
            statementMovies.executeBatch();
            statementStarsInMovies.executeBatch();
            this.conn.commit();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            tempMovie = new Movie("tt" + String.format("%07d", currStarId));
            tempGenre = new Genre();
            this.currStarId += 1;
            //tempMovie.setType(attributes.getValue("type"));
        }
        if (qName.equalsIgnoreCase("filmc")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            if (this.seenMovies.containsKey(tempMovie.getTitle()) &&
                    this.seenMovies.get(tempMovie.getTitle()).containsKey(tempMovie.getDirectorName()) &&
                    this.seenMovies.get(tempMovie.getTitle()).get(tempMovie.getDirectorName()) ==  tempMovie.getReleaseYear()) {
                this.currStarId -= 1;
                System.out.printf("[MOVIE INCONSISTENCY] (%s, %s, %d) is a duplicate movie. \n", tempMovie.getTitle(), tempMovie.getDirectorName(), tempMovie.getReleaseYear());
            } else if (tempMovie.getTitle().isEmpty()) {
                this.currStarId -= 1;
            } else if (tempMovie.getDirectorName().isEmpty()) {
                this.currStarId -= 1;
            } else if (tempMovie.getReleaseYear() == -1) {
                this.currStarId -= 1;
            } else {
                Map<String, Integer> innerMap = new HashMap<>();
                innerMap.put(tempMovie.getDirectorName(), tempMovie.getReleaseYear());
                seenMovies.put(tempMovie.getTitle(), innerMap);
                xmlIdToMovie.put(tempMovie.getXMLid(), tempMovie);

                HashSet<String> seenGenres = new HashSet<>();

                for(Genre g: tempMovie.getGenresList()) {
                    if (seenGenres.contains(g.getName())) {
                        System.out.printf("[GENRE INCONSISTENCY] %s is a duplicate genre for this movie \n", tempVal);
                    }
                    else {
                        //Check if genre is in db otherwise add
                        try {
//                            statement = conn.prepareCall("{call add_genre(?)}");
                            statementGenre.setString(1, g.getName());
                            statementGenre.executeQuery();
//                            statementGenre.addBatch();
//                            batchSize += 1;
//                            if (batchSize == 100) {
//                                statementGenre.executeBatch();
//                                statementGenreInMovies.executeBatch();
//                                statementMovies.executeBatch();
//                                batchSize = 0;
//                            }

//                            statement = conn.prepareCall("{call add_genre_in_movies(?,?)}");
                            statementGenreInMovies.setString(1, tempMovie.getId());
                            statementGenreInMovies.setString(2, g.getName());
                            statementGenreInMovies.executeQuery();
//                            statementGenreInMovies.addBatch();
//                            batchSize += 1;
//                            if (batchSize == 100) {
//                                statementGenre.executeBatch();
//                                statementGenreInMovies.executeBatch();
//                                statementMovies.executeBatch();
//                                batchSize = 0;
//                            }
                        } catch(Exception e) {
                             System.out.println(e);
                        }
//                        //Check if genre is in
                        System.out.println(g.toString());
                    }
                }

                System.out.println(tempMovie.toString());
                 try {
//                     statement = conn.prepareCall("{call add_movie_only(?,?,?,?)}");
                     statementMovies.setString(1,tempMovie.getId());
                     statementMovies.setString(2,tempMovie.getTitle());
                     statementMovies.setInt(3,tempMovie.getReleaseYear());
                     statementMovies.setString(4,tempMovie.getDirectorName());
                     statementMovies.executeQuery();
//                     statementMovies.addBatch();
//                     batchSize += 1;
//                     if (batchSize == 100) {
//                         statementGenre.executeBatch();
//                         statementGenreInMovies.executeBatch();
//                         statementMovies.executeBatch();
//                         batchSize = 0;
//                     }
                 } catch(Exception e) {
                     System.out.println(e);
                 }
            }



        } else if (qName.equalsIgnoreCase("cat")) {
            if(tempVal.isEmpty()) {
            }
            else if (!movieGenres.containsKey(tempVal.toUpperCase())) {
                System.out.printf("[GENRE INCONSISTENCY] %s is an invalid Genre Type \n", tempVal);
            } else {
                tempGenre.setName(movieGenres.get(tempVal.toUpperCase()));
                tempMovie.addGenre(tempGenre);
                tempGenre = new Genre();
            }
        }else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setXMLid(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            if (qName.length() == 0) {
                System.out.println("[MOVIE INCONSISTENCY] Movie title cannot be empty\n");
                tempMovie.setTitle("");
            }
            else {
                tempMovie.setTitle(tempVal);
            }
        } else if (qName.equalsIgnoreCase("dir")) {


            if(tempVal.isEmpty()) {
                System.out.printf("[MOVIE INCONSISTENCY] %s is an invalid director name.\n", tempVal);
                tempMovie.setDirectorName("");
            }
            else if (Character.isDigit(tempVal.charAt(0))) {
                System.out.printf("[MOVIE INCONSISTENCY] %s is an invalid director name.\n", tempVal);
                tempMovie.setDirectorName("");
            }
            else {
//                String[] splitDirectorName = tempVal.split(" ");
//                for(String s: splitDirectorName) {
//                    if(!s.matches("^[a-zA-Z]*$")){
//                        System.out.printf("[MOVIE INCONSISTENCY] %d is an invalid director name.\n", tempVal);
//                        noErrors = 0;
//                        break;
//                    }
//                }
//                if(noErrors == 1) {
                tempMovie.setDirectorName(tempVal);
//                }
            }
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                int parsedReleaseYear = Integer.parseInt(tempVal);

                if(tempVal.length() == 4) {
                    tempMovie.setReleaseYear(Integer.parseInt(tempVal));
                }
                else {
                    System.out.printf("[MOVIE INCONSISTENCY] %d is an invalid release year.\n", tempVal);
                    tempMovie.setReleaseYear(-1);
                }
            }
            catch (Exception e) {
                System.out.printf("[MOVIE INCONSISTENCY] %s is an invalid release year.\n", tempVal);
                tempMovie.setReleaseYear(-1);
            }
        } else if(qName.equalsIgnoreCase("f")) {
            tempStar.setXMLid(tempVal);
        } else if(qName.equalsIgnoreCase("a")) {
            if(tempVal.isEmpty()) {
                System.out.printf("Skip: {a}, {%s} \n", tempVal);
            }
            else if(xmlIdToMovie.containsKey(tempStar.getXMLid())) {
                try {
                    statementStarsInMovies = conn.prepareCall("{call add_star_in_movies(?,?)}");
                    statementStarsInMovies.setString(1,xmlIdToMovie.get(tempStar.getXMLid()).getId());
                    statementStarsInMovies.setString(2,tempVal);
                    statementStarsInMovies.executeQuery();
//                    statementStarsInMovies.addBatch();
//                     batchSize += 1;
//                     if (batchSize == 100) {
//                         statementStarsInMovies.executeBatch();
//                        batchSize = 0;
//                     }
                 } catch(Exception e) {
                     System.out.println(e);
                 }
                System.out.printf("%s: %s and %s linked!\n", tempStar.getXMLid(), xmlIdToMovie.get(tempStar.getXMLid()).getTitle(), tempVal);
            }
            else {
                System.out.printf("Skip: {a}, {%s} \n", tempVal);
            }
        }

    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runExample();
        spe.runExample2();
        spe.finishConnection();
    }

}

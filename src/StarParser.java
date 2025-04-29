
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
import java.io.FileWriter;

public class StarParser extends DefaultHandler implements MySqlAuth{
    private XMLReader reader;

    //List<Employee> myEmpls;
    // private String myQuery;
    private Map<String, Integer> seenStars;

    private String tempVal;

    private Connection conn;

    //to maintain context
    //private Employee tempEmp;
    private Movie tempMovie;
    private Star tempStar;
    private Genre tempGenre;
    private int batchSize;
    private FileWriter fileOut;
    private CallableStatement statement;

    // public void init() {
    //     try {
    //         dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
    //     } catch (NamingException e) {
    //         e.printStackTrace();
    //     }
    // }

    public StarParser() {
        //myEmpls = new ArrayList<Employee>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySqlAuth.loginurl, MySqlAuth.username, MySqlAuth.password);
            conn.setAutoCommit(false);
        }
        catch (Exception e) {
            System.out.println("Database Error: 500");
        }
        seenStars = new HashMap<>();
        tempVal = "";
        batchSize = 0;
    }

    public void runExample() {
        parseDocument();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("xml/actors63.xml", this);

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
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of movie
            tempStar = new Star();
            //tempMovie.setType(attributes.getValue("type"));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            // myQuery = "INSERT INTO stars VALUES(?,?,?)"
            //insert prepared statement here
            //ship it out like a fedex fool
            if(seenStars.containsKey(tempStar.getName()) && seenStars.get(tempStar.getName()).intValue() == tempStar.getBirthYear()) {
                System.out.printf("[STAR INCONSISTENCY] (%s, %d) is a duplicate star. \n", tempStar.getName(), seenStars.get(tempStar.getName()).intValue());
            }
            else if(tempStar.getName().length() == 0) {
                System.out.printf("[STAR INCONSISTENCY] %s name cannot be empty.\n", tempStar.getName());
            }
            else {
                seenStars.put(tempStar.getName(), tempStar.getBirthYear());
                System.out.println(tempStar.toString());
                try {
                    statement = conn.prepareCall("{call add_star(?,? )}");
                    statement.setString(1,tempStar.getName());
                    statement.setString(2,(tempStar.getBirthYear() == -1 ? null : String.valueOf(tempStar.getBirthYear())));
                    statement.executeQuery();
//                    statement.addBatch();
//                    batchSize += 1;
//                    if (batchSize == 100) {
//                        statement.executeBatch();
//                        batchSize = 0;
//                    }
                } catch(Exception e) {
                    System.out.println(e);
                }
            }
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                int parsedDob = Integer.parseInt(tempVal);
                if(tempVal.length() == 4) {
                    tempStar.setBirthYear(parsedDob);
                }
                else {
                    System.out.printf("[STAR INCONSISTENCY] Date of Birth was empty.\n", tempVal);
                    tempStar.setBirthYear(-1);
                }
            }
            catch(NumberFormatException nfe) {
                System.out.printf("[STAR INCONSISTENCY] %s is not a valid year.\n", tempVal);
                tempStar.setBirthYear(-1);
            }
        }

    }

    public static void main(String[] args) {
        StarParser spe = new StarParser();
        spe.runExample();
        spe.finishConnection();
    }

}

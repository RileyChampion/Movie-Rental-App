
/*import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class StarMovieParserExample extends DefaultHandler {

    //List<Employee> myEmpls;
    private String myQuery;

    private String tempVal;

    //to maintain context
    //private Employee tempEmp;
    private Movie tempMovie;
    private Star tempStar;
    private Genre tempGenre;

    public void MovieParserExample() {
        //myEmpls = new ArrayList<Employee>();
        myQuery = "";
        tempVal = "";
    }

    public void runExample() {
        parseDocument();
        printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("employees.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */

    //Event Handlers
    /*public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
            tempMovie = new Movie();
            //tempMovie.setType(attributes.getValue("type"));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            //myQuery = "INSERT INTO movies VALUES(?,?,?,?)"
            //insert prepared statement here
            //ship it out like a fedex fool
            //Parse the actors xml ==> stars
            // "XMLID" -> Star();
            // """
            //myQuery = ""; //reset query string

        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setXMLid(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("dir")) {
            tempMovie.setDirectorName(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setReleaseYear(Integer.parseInt(tempVal));
        }

    }

    public static void main(String[] args) {
        MovieParserExample spe = new MovieParserExample();
        spe.runExample();
    }

}*/

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet(name = "FullSearchServlet", urlPatterns = "/api/fullsearch")
public class FullSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String> stopwords = new ArrayList<String>(Arrays.asList("a",
        "about", "am", "are", "as", "at", "be", "by", "com", "de",
        "en", "for", "from", "how", "i", "in", "is", "it", "la", "of", "on", "or", "that", "the", "this",
        "to", "was", "what", "when", "where", "who", "will", "with", "und", "the", "www"));

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /*public FullSearchServlet() {
        super();
    }*/

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.nanoTime();
        long endTime = 0;
        long startQueryTime = 0;
        long queryTime = 0;
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            //System.out.println("real path: " + getServletContext().getRealPath("/"));

            // Declare our statement
            Statement statement = conn.createStatement();


            //String requestType = request.getParameter("type");
            String requestText = request.getParameter("title");
            /*String requestAmount = request.getParameter("N");
            String requestSortingOne = request.getParameter("sorting-option1");
            String requestSortingTwo = request.getParameter("sorting-option2");
            String requestOrder = request.getParameter("order");
            String requestPage = request.getParameter("page");

            System.out.println(requestSortingOne + " : " + requestSortingTwo + " : " + requestOrder);

            int limit = Integer.parseInt(requestAmount);
            int offset = Integer.parseInt(requestPage);


            int currLimit = limit;
            int currOffset = currLimit * offset - limit;*/
            String sentence = requestText;
            String[] words = sentence.split(" ");
            String answer = "";
            for (String s : words) {
                if (stopwords.contains(s) == false) {
                    answer += "'+*" + s + "*' ";
                }
            }

            String query = "";
            PreparedStatement prepStatement = null;

            query = "SELECT * FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;";
            prepStatement = conn.prepareStatement(query);
            System.out.print(requestText);
            prepStatement.setString(1, answer);


        // Perform the query
            startQueryTime = System.nanoTime();
            ResultSet rs = prepStatement.executeQuery();
            queryTime = System.nanoTime();

            JsonArray jsonArray = new JsonArray();
            Boolean hasNext = false;
            while (rs.next()) {
                JsonObject m = generateJsonObject(rs.getString("title"));
                m.addProperty("id", rs.getString("id"));
                jsonArray.add(m);
            }

            //int totalResults = 0;
            //Boolean hasNext = false;

            JsonObject jsonObject = new JsonObject();

            //jsonObject.addProperty("page", offset);
            //jsonObject.addProperty("hasNext", hasNext);
            jsonObject.add("results", jsonArray);

            // write JSON string to output
            out.write(jsonObject.toString());
            endTime = System.nanoTime();
            // set response status to 200 (OK)
            response.setStatus(200);


        } catch (Exception e) {

            // write error message JSON object to output
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            endTime = System.nanoTime();

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            long ts = endTime - startTime;
            long tj = startQueryTime - startTime;
            //insert log file print statement here
            try {
//                String contextPath = request.getServletContext().getRealPath("/") + "search_servlet_log.txt";
//                System.out.println("real path: " + contextPath);
                String contextPath = "/home/ubuntu/cs122b-spring21-team-39/logs/search_servlet_log.txt";
                FileWriter myWriter = new FileWriter(contextPath, true);
                myWriter.write("TS:" + Long.toString(ts) + ";TJ:" + Long.toString(tj) + "\n");
                myWriter.close();
                System.out.println("Successfully logged time to the file.");
            } catch (Exception e) {
                System.out.println("An error occurred while logging time.");
                e.printStackTrace();
            }
            out.close();

        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);
        return jsonObject;
    }


}
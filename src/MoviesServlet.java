import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    private final ArrayList<String> stopwords = new ArrayList<String>(Arrays.asList("a",
        "about", "am", "are", "as", "at", "be", "by", "com", "de",
        "en", "for", "from", "how", "i", "in", "is", "it", "la", "of", "on", "or", "that", "the", "this",
        "to", "was", "what", "when", "where", "who", "will", "with", "und", "the", "www"));

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();


            String requestType = request.getParameter("type");
            String requestText = request.getParameter("text");
            String requestAmount = request.getParameter("N");
            String requestSortingOne = request.getParameter("sorting-option1");
            String requestSortingTwo = request.getParameter("sorting-option2");
            String requestOrder = request.getParameter("order");
            String requestPage = request.getParameter("page");

            System.out.println(requestSortingOne + " : " + requestSortingTwo + " : " + requestOrder);

            int limit = Integer.parseInt(requestAmount);
            int offset = Integer.parseInt(requestPage);


            int currLimit = limit;
            int currOffset = currLimit * offset - limit;

            String query = "";
            PreparedStatement prepStatement = null;

            if(requestType.equals("genre")) {
                query = "select * \n" +
                        "from movies, genres, genres_in_movies, ratings \n" +
                        "where movies.id = genres_in_movies.movieId \n" +
                        "and genres_in_movies.genreId = genres.id \n" +
                        "and movies.id = ratings.movieId \n" +
                        "and genres.name = ? \n" +
                        "order by " + requestSortingOne + (!requestSortingTwo.equals("") ? ", " + requestSortingTwo + " " : " ") + requestOrder + "\n" +
                        "limit ? offset ?;";

                prepStatement = conn.prepareStatement(query);
                prepStatement.setString(1, requestText);
                prepStatement.setInt(2, currLimit + 1);
                prepStatement.setInt(3, currOffset);

            } else if(requestType.equals("star")) {
                query = "select * \n" +
                        "from movies, stars, stars_in_movies, ratings \n" +
                        "where movies.id = stars_in_movies.movieId \n" +
                        "and stars_in_movies.starId = stars.id \n" +
                        "and movies.id = ratings.movieId \n" +
                        "and stars.name like ?\n" +
                        "order by " + requestSortingOne + (!requestSortingTwo.equals("") ? ", " + requestSortingTwo + " " : " ") + requestOrder + "\n" +
                        "limit ? offset ?;";

                prepStatement = conn.prepareStatement(query);
                prepStatement.setString(1,  "%" + requestText + "%");
                prepStatement.setInt(2, currLimit + 1);
                prepStatement.setInt(3, currOffset);
            }else if(requestType.equals("title")){
                query = "select *\n" +
                        "from movies, ratings\n" +
                        "where movies.id = ratings.movieId\n " +
                        "and MATCH (movies.title) AGAINST (? IN BOOLEAN MODE)\n" +
                        "order by " + requestSortingOne + (!requestSortingTwo.equals("") ? ", " + requestSortingTwo + " " : " ") + requestOrder + "\n" +
                        "limit ? offset ?;";
                prepStatement = conn.prepareStatement(query);

                String sentence = requestText;
                String[] words = sentence.split(" ");
                String answer = "";
                for (String s : words) {
                    if (stopwords.contains(s) == false) {
                        answer += "'+*" + s + "*' ";
                    }
                }

                System.out.println(answer);

                prepStatement.setString(1, answer);
                prepStatement.setInt(2, currLimit + 1);
                prepStatement.setInt(3, currOffset);

            }else {
                query = "select *\n" +
                        "from movies, ratings\n" +
                        //"inner join ratings\n" +
                        //"on movies.id = ratings.movieId\n" +
                        "where movies.id = ratings.movieId\n " +
                        (requestType.equals("director")  ? "and " +  requestType + " like '%" +  requestText + "%'\n" :  "")  +
                        (requestType.equals("alphabet") && requestText.equals("*") ? " and title REGEXP '^[^0-9A-Za-z]'\n" :  "") +
                        (requestType.equals("alphabet") && !requestText.equals("*") ? " and title like '" +  requestText + "%'\n" :  "") +
                        (requestType.equals("year") ? " and year = " + requestText + "\n" : "") +
                        "order by " + requestSortingOne + (!requestSortingTwo.equals("") ? ", " + requestSortingTwo + " " : " ") + requestOrder + "\n" +
                        "limit ? offset ?;";
                prepStatement = conn.prepareStatement(query);
                prepStatement.setInt(1, currLimit + 1);
                prepStatement.setInt(2, currOffset);
            }

            // Perform the query
            ResultSet rs = prepStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            int totalResults = 0;
            Boolean hasNext = false;

            // Iterate through each row of rs
            while (rs.next()) {

                totalResults += 1;
                if(totalResults == limit + 1) {
                    hasNext = true;
                    break;
                }

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

//                Statement genreStatement = conn.createStatement();

                String genreQuery = "select genres.name\n" +
                        "from genres, genres_in_movies, movies\n" +
                        //"join genres_in_movies\n" +
                        //"on id = genreId\n" +
//                        "where id = (select genreID from genres_in_movies)\nand " +
                        //"join movies\n" +
                        //"on movieId = movies.id\n" +
//                        "genres_in_movies.movieId = (select id from movies)\nand " +
                        "where genres.id = genres_in_movies.genreID and genres_in_movies.movieId = movies.id and " +
                        "movies.title = ? \n" +
                        "limit 3";

                PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
                genreStatement.setString(1,movie_title);

                // Perform the query
                ResultSet gs = genreStatement.executeQuery();

                JsonArray genreJsonArray = new JsonArray();

                while (gs.next()) {
                    String movie_genre = gs.getString("name");

                    JsonObject genreJsonObject = new JsonObject();
                    genreJsonObject.addProperty("movie_genre", movie_genre);
                    genreJsonArray.add(genreJsonObject);
                }
                gs.close();
                genreStatement.close();

//                Statement starStatement = conn.createStatement();

                String starQuery = "select stars.name, stars.id\n" +
                        "from stars, stars_in_movies, movies\n" +
                        //"join stars_in_movies\n" +
                        //"on id = starId\n" +
//                        "where id = (select starID from stars_in_movies)\nand " +
                        //"join movies\n" +
                        //"on movieId = movies.id\n" +
//                        "movieId = (select id from movies)\nand " +
                        "where stars.id = stars_in_movies.starId  and stars_in_movies.movieId = movies.id and " +
                        "movies.title = ? \n" +
                        "limit 3";

                PreparedStatement starStatement = conn.prepareStatement(starQuery);
                starStatement.setString(1,movie_title);

                // Perform the query
                ResultSet ss = starStatement.executeQuery();

                JsonArray starJsonArray = new JsonArray();

                while (ss.next()) {
                    String star_name = ss.getString("name");
                    String star_id = ss.getString("id");

                    JsonObject starJsonObject = new JsonObject();
                    starJsonObject.addProperty("star_name", star_name);
                    starJsonObject.addProperty("star_id", star_id);
                    starJsonArray.add(starJsonObject);
                }
                ss.close();
                starStatement.close();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.add("movie_genres", genreJsonArray);
                jsonObject.add("movie_stars", starJsonArray);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("page", offset);
            jsonObject.addProperty("hasNext", hasNext);
            jsonObject.add("results", jsonArray);

            // write JSON string to output
            out.write(jsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);


        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
        // always remember to close db connection after usage. Here it's done by try-with-resources
    }
}

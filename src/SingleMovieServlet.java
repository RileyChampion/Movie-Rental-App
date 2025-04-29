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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "select *\n" +
                    "from movies\n" +
                    "inner join ratings\n" +
                    "on movies.id = ratings.movieId\n" +
                    "where movies.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                //String movie_genres = rs.getString("genres");
                //String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

//                Statement genreStatement = conn.createStatement();

                String genreQuery = "select genres.name\n" +
                        "from genres\n" +
                        "join genres_in_movies\n" +
                        "on id = genreId\n" +
                        "join movies\n" +
                        "on movieId = movies.id\n" +
                        "where title = ?;";

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

                String starQuery = "select stars.name, stars.id, stars.birthYear\n" +
                        "from stars\n" +
                        "join stars_in_movies\n" +
                        "on id = starId\n" +
                        "join movies\n" +
                        "on movieId = movies.id\n" +
                        "where title = ?;";

                PreparedStatement starStatement = conn.prepareStatement(starQuery);
                starStatement.setString(1, movie_title);

                // Perform the query
                ResultSet ss = starStatement.executeQuery();

                JsonArray starJsonArray = new JsonArray();

                while (ss.next()) {
                    String star_name = ss.getString("name");
                    String star_id = ss.getString("id");
                    String starDob = ss.getString("birthYear");

                    JsonObject starJsonObject = new JsonObject();
                    starJsonObject.addProperty("star_name", star_name);
                    starJsonObject.addProperty("star_id", star_id);
                    starJsonObject.addProperty("star_dob", starDob);
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

            // write JSON string to output
            out.write(jsonArray.toString());
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
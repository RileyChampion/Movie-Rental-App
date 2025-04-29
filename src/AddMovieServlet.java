import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String movieTitle = req.getParameter("movieTitle");
        String movieYear = req.getParameter("movieYear");
        String movieDirector = req.getParameter("movieDirector");
        String starName = req.getParameter("starName");
        String starYear = req.getParameter("starYear");
        String movieGenre = req.getParameter("movieGenre");

        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            CallableStatement statement = conn.prepareCall("{call add_movie(?,? ,? ,? ,? ,? )}");
            statement.setString(1,movieTitle);
            statement.setString(2,movieYear);
            statement.setString(3,movieDirector);
            statement.setString(4,starName);
            statement.setString(5,(starYear.equals("") ? null : starYear ));
            statement.setString(6,movieGenre);

            ResultSet rs = statement.executeQuery();

            String status = "";

            while(rs.next()) {
                status = rs.getString("message");
            }
            rs.close();
            statement.close();

            JsonObject newObject = new JsonObject();
            newObject.addProperty("status", status);

            out.write(newObject.toString());

        } catch (Exception e) {
        } finally {
            out.close();
        }
    }
}


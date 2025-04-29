import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String starName = req.getParameter("starName2");
        String starYear = req.getParameter("starYear2");

        // Output stream to STDOUT
        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            CallableStatement statement = conn.prepareCall("{call add_star(?,? )}");
            statement.setString(1,starName);
            statement.setString(2,(starYear.equals("") ? null : starYear ));

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

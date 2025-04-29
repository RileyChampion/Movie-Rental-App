import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "PaymentServlet", urlPatterns = "/api/purchase")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        ArrayList<JsonObject> cartItems = (ArrayList<JsonObject>) session.getAttribute("cartItems");

        String qfirstName = request.getParameter("InputFirstName");
        String qlastName = request.getParameter("InputLastName");
        String qcreditCard = request.getParameter("InputCreditCard");
        String qexperation = request.getParameter("InputExperation");


        /*InputFirstName
          InputLastName
          InputCreditCard
          InputExperation*/

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            //GET CREDITCARDNUMBER
            Statement ccStatement = conn.createStatement();

            String ccQuery = "";

            ccQuery = "SELECT * from creditcards " +
                      "where expiration = ?" +
                      " AND id = ?" +
                      " AND firstName = ?" +
                      " AND lastName = ?";

            PreparedStatement prepStatement = conn.prepareStatement(ccQuery);
            prepStatement.setString(1, qexperation);
            prepStatement.setString(2, qcreditCard);
            prepStatement.setString(3, qfirstName);
            prepStatement.setString(4, qlastName);




            ResultSet ccRs = prepStatement.executeQuery(ccQuery);

            if (ccRs.next()==false) {
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("errorMessage", e.getMessage());
//                out.write(jsonObject.toString());
                ccRs.close();
                ccStatement.close();
                throw new Exception("No Payment Information Found.");
            }

            ccRs.close();
            ccStatement.close();

            //GET CUSTOMER ID
//            Statement customerIdStatement = conn.createStatement();

            User currUser = (User) session.getAttribute("user");

            String customerIdQuery = "";

            customerIdQuery = "SELECT id FROM customers WHERE ccId = ? AND email = ?;";
            PreparedStatement customerIdStatement = conn.prepareStatement(customerIdQuery);
            customerIdStatement.setString(1,qcreditCard);
            customerIdStatement.setString(2,currUser.getUser());


            ResultSet customerIdRs = customerIdStatement.executeQuery(customerIdQuery);

            if (customerIdRs.next()==false) {
                customerIdRs.close();
                customerIdStatement.close();
                throw new Exception("No User Found.");
            }

            String customerId = customerIdRs.getString("id");

            customerIdRs.close();
            customerIdStatement.close();

            for (int i = 0; i < cartItems.size(); i++) {

                Random rng = new Random();

                int saleID = rng.nextInt(1000000);

                //get today's date here
                Date d = new Date();
                String today = d.toString();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = formatter.format(d);

                //GET MOVIE ID

                String movieIdQuery = "";

//                Statement movidIdStatement = conn.createStatement();

                System.out.println(cartItems.get(i).get("movie-title").getAsString());

                movieIdQuery = "SELECT id FROM movies WHERE title = ?";
                PreparedStatement movidIdStatement = conn.prepareStatement(movieIdQuery);
                movidIdStatement.setString(1,cartItems.get(i).get("movie-title").getAsString());

                ResultSet movieIdRs = movidIdStatement.executeQuery(movieIdQuery);

                String movieId = "";

                if(movieIdRs.next()) {
                    movieId = movieIdRs.getString("id");
                    System.out.println(movieId);
                }


                movieIdRs.close();
                movidIdStatement.close();

                //INSERT INTO SALES
//                Statement salesStatement = conn.createStatement();

                String salesQuery = "";

                salesQuery = "INSERT INTO sales VALUES(?,?,?,?)";

                PreparedStatement salesStatement = conn.prepareStatement(salesQuery);
                salesStatement.setInt(1,saleID);
                salesStatement.setString(2,customerId);
                salesStatement.setString(3,movieId);
                salesStatement.setString(4,formattedDate);


                salesStatement.executeUpdate();

                System.out.println("EXECUTRED");

//                salesRs.close();
                salesStatement.close();
            }
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
            out.write(responseJsonObject.toString());
        }
        catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", e.getMessage());

            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
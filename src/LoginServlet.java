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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
        String username = request.getParameter("username");
        String password = request.getParameter("password");
//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//        System.out.println(gRecaptchaResponse);

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */

        String queryName = "";
        String queryPass = "";
        String empQueryName = "";
        String empQueryPass = "";
        boolean foundUser = false;
        boolean foundEmp = false;

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            Statement empStatement = conn.createStatement();

            String empQuery = "select *\n" +
                    "from employees\n" +
                    "where employees.email = ?";
            PreparedStatement updateEmployees = conn.prepareStatement(empQuery);
            updateEmployees.setString(1,username);

            ResultSet empRs = updateEmployees.executeQuery();



            if(empRs.next()) {
                String employee_email = empRs.getString("email");
                String employee_password = empRs.getString("password");
                empQueryName = employee_email;
                empQueryPass = employee_password;
                foundEmp = true;
            }

            empRs.close();
            empStatement.close();

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "select *\n" +
                    "from customers\n" +
                    "where customers.email = ?";
            PreparedStatement updateCustomers = conn.prepareStatement(query);
            updateCustomers.setString(1,username);

            ResultSet rs = updateCustomers.executeQuery();



            if(rs.next()) {
                String customer_email = rs.getString("email");
                String customer_password = rs.getString("password");
                queryName = customer_email;
                queryPass = customer_password;
                foundUser = true;
            }

            rs.close();
            statement.close();

            // set response status to 200 (OK)
            response.setStatus(200);


        } catch (Exception e) {
        } finally {
        }

        JsonObject responseJsonObject = new JsonObject();
//        System.out.println(username + " : " + queryName);
//        System.out.println(password + " : " + queryPass);
//        System.out.println(foundUser);
        //if (username.equals(queryName) && password.equals(queryPass) && foundUser) {
        try {
            boolean notRobot = true;
//            try {
//                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//                notRobot = true;
//            }
//            catch (Exception e) {
//                responseJsonObject.addProperty("status", "fail");
//                responseJsonObject.addProperty("message", "recaptcha failed; you are a robot");
//                response.getWriter().write(responseJsonObject.toString());
//                return;
//            }
            if (username.equals(empQueryName) && VerifyEmpPassword.checkPassword(empQueryName,password) && foundEmp && notRobot) {
                request.getSession().setAttribute("user", new User(username));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "employee success");
            }
            else if (username.equals(queryName) && VerifyPassword.checkPassword(queryName,password) && foundUser && notRobot) {
                // Login success:

                // set this user into the session
                request.getSession().setAttribute("user", new User(username));

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");

                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                if (!username.equals(queryName) && !username.equals(empQueryName)) {
                    responseJsonObject.addProperty("message", "user " + queryName + " doesn't exist");
                } else {
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
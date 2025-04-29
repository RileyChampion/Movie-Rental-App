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

@WebServlet(name = "MetadataServlet", urlPatterns = "/api/moviedb-metadata")
public class MetadataServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter out = resp.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();

            String queryTables = "SHOW TABLES;";

            PreparedStatement ps = conn.prepareStatement(queryTables);

            ResultSet rs = ps.executeQuery();

            JsonArray tableArr = new JsonArray();

            while(rs.next()) {

                JsonObject tableObject = new JsonObject();

                String currTable = rs.getString("Tables_in_moviedb");

                if(currTable.equals("customers_backup")) {
                    continue;
                }

                String tableQuery = "SELECT * FROM " + currTable;
                PreparedStatement tableQueryPrepare = conn.prepareStatement(tableQuery);

                ResultSet tablers = tableQueryPrepare.executeQuery();
                ResultSetMetaData tablemeta = tablers.getMetaData();
                int numberOfColumns = tablemeta.getColumnCount();

                JsonArray columnArray = new JsonArray();

                for(int i = 1; i <= numberOfColumns; i++) {
                    JsonObject colObject = new JsonObject();

                    String colName = tablemeta.getColumnName(i);
                    String colType = tablemeta.getColumnTypeName(i);

                    colObject.addProperty("colName", colName);
                    colObject.addProperty("colType", colType);

                    columnArray.add(colObject);
                }

                tableObject.addProperty("tableName", currTable);
                tableObject.add("columns", columnArray);

                tableArr.add(tableObject);
            }
            rs.close();
            ps.close();
            statement.close();

            JsonObject result = new JsonObject();
            result.add("result", tableArr);

            out.write(result.toString());
            resp.setStatus(200);

        } catch (Exception e) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            resp.setStatus(500);

        } finally {
            out.close();
        }
    }
}

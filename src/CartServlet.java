import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
//        responseJsonObject.addProperty("sessionID", sessionId);
//        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        ArrayList<JsonObject> cartItems = (ArrayList<JsonObject>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        JsonArray cartItemsJsonArray = new JsonArray();
        cartItems.forEach(cartItemsJsonArray::add);
        responseJsonObject.add("shoppingCartItems", cartItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieTitle = request.getParameter("movie-title");
        System.out.println(movieTitle);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        ArrayList<JsonObject> cartItems = (ArrayList<JsonObject>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            JsonObject addItem =  new JsonObject();
            addItem.addProperty("movie-title", movieTitle);
            addItem.addProperty("quantity", 1);
            cartItems.add(addItem);
            session.setAttribute("cartItems", cartItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (cartItems) {

                System.out.println("Cart Exists");

                JsonObject findItem = inCart(movieTitle, cartItems);

                if(findItem != null) {
                    int currQuantity = findItem.get("quantity").getAsInt();
                    editMovie(findItem, currQuantity + 1);
                }
                else {
                    JsonObject addItem =  new JsonObject();
                    addItem.addProperty("movie-title", movieTitle);
                    addItem.addProperty("quantity", 1);
                    cartItems.add(addItem);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray cartItemsJsonArray = new JsonArray();
        cartItems.forEach(cartItemsJsonArray::add);
        responseJsonObject.add("shoppingCartItems", cartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieTitle = request.getParameter("movie-title");
        String movieQuantity = request.getParameter("movie-quantity");
        System.out.println(movieTitle);
        HttpSession session = request.getSession();

        ArrayList<JsonObject> cartItems = (ArrayList<JsonObject>) session.getAttribute("cartItems");
        synchronized (cartItems) {

            JsonObject findItem = inCart(movieTitle, cartItems);

            if(findItem != null) {
                if(Integer.parseInt(movieQuantity) <= 0) {
                    deleteMovie(movieTitle, cartItems);
                }
                else {
                    editMovie(findItem, Integer.parseInt(movieQuantity));
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray cartItemsJsonArray = new JsonArray();
        cartItems.forEach(cartItemsJsonArray::add);
        responseJsonObject.add("shoppingCartItems", cartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieTitle = request.getParameter("movie-title");
        System.out.println(movieTitle);
        HttpSession session = request.getSession();

        ArrayList<JsonObject> cartItems = (ArrayList<JsonObject>) session.getAttribute("cartItems");
        synchronized (cartItems) {
            deleteMovie(movieTitle, cartItems);
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray cartItemsJsonArray = new JsonArray();
        cartItems.forEach(cartItemsJsonArray::add);
        responseJsonObject.add("shoppingCartItems", cartItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }

    public JsonObject inCart(String movie, ArrayList<JsonObject> cart) {
        for (JsonObject j : cart) {
            if(j.get("movie-title").getAsString().equals(movie)) {
                System.out.println("FOUND MOVIE");
                return j;
            }
        }
        return null;
    }

    public void editMovie(JsonObject item, int quantity) {
        item.addProperty("quantity", quantity);
    }

    public void deleteMovie(String movie, ArrayList<JsonObject> cart) {

        int removeInd = -1;

        for (int i = 0; i < cart.size(); i++) {
            if(cart.get(i).get("movie-title").getAsString().equals(movie)) {
                removeInd = i;
                break;
            }
        }
        cart.remove(removeInd);
    }
}
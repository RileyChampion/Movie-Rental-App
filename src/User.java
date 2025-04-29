/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
import java.util.ArrayList;
import java.util.HashMap;

public class User {

    private final String username;
    private HashMap<String,Integer> shoppingCart;

    public User(String username) {
        this.username = username;
    }

    public String getUser() {
        return this.username;
    }

    public boolean inCart(String movie) {
        return shoppingCart.containsKey(movie);
    }

    public void addMovie(String movie) {
        if (inCart(movie) == false) {
            shoppingCart.put(movie,1);
        }
        else {
            shoppingCart.replace(movie,shoppingCart.get(movie)+1);
        }
    }

    public void removeMovie(String movie) {
        if (inCart(movie) == true) {
            if (shoppingCart.get(movie)==0) {
                shoppingCart.remove(movie);
            }
            else {
                shoppingCart.replace(movie,shoppingCart.get(movie)-1);
            }
        }
    }

    public void deleteMovie(String movie) {
        shoppingCart.remove(movie);
    }

}
package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieViewActivity extends Activity {

    private Movie singleMovie;
    private final String host = "3.143.213.98";
    private final String port = "8443";
    private final String domain = "cs122b-spring21-team-39";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private String TAG = SingleMovieViewActivity.class.getSimpleName();

    TextView titleView;
    TextView directorView;
    TextView yearView;
    TextView genresView;
    TextView starsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.singlemovie);

        String extraId = getIntent().getExtras().getString("id");

        this.getRequest(extraId);

        titleView = findViewById(R.id.Title);
        directorView = findViewById(R.id.Director);
        yearView = findViewById(R.id.Year);
        genresView = findViewById(R.id.Genres);
        starsView = findViewById(R.id.Stars);
        /*TextView ratingView = view.findViewById(R.id.rating);
        TextView genresView = view.findViewById(R.id.genres);
        TextView starsView = view.findViewById(R.id.stars);*/



    }

    private void getRequest(String id) {

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        Context curr = this;

        String url = baseURL + "/api/single-movie?id=" + id;

        JsonArrayRequest moviesRequest = new JsonArrayRequest
                (url, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            System.out.println(response.toString());
                            JSONArray respMovies = new JSONArray(response.toString());
                            System.out.println(respMovies.toString());

                            //for(int i = 0; i < respMovies.length(); i++) {

                            JSONObject currMovie = respMovies.getJSONObject(0);

                            System.out.println("checkpoint 1");

                            String movieId = currMovie.getString("movie_id");
                            String movieTitle = currMovie.getString("movie_title");
                            short movieYear = (short) currMovie.getInt("movie_year");
                            String movieDirector = currMovie.getString("movie_director");

                            singleMovie  = new Movie(movieId, movieTitle, movieYear, movieDirector);

                            JSONArray genres = currMovie.getJSONArray("movie_genres");

                            for(int j = 0; j < genres.length(); j++) {

                                JSONObject currGenre = genres.getJSONObject(j);

                                String genreName = currGenre.getString("movie_genre");

                                Genre newGenre = new Genre(null, genreName);
                                singleMovie.addGenre(newGenre);
                            }

                            JSONArray stars = currMovie.getJSONArray("movie_stars");

                            for(int j = 0; j < stars.length(); j++) {

                                JSONObject currStars = stars.getJSONObject(j);

                                String starId = currStars.getString("star_id");
                                String starName = currStars.getString("star_name");

                                Star newStar = new Star(starId, starName, (short) -100);
                                singleMovie.addStar(newStar);
                            }

                            Double rating = currMovie.getDouble("movie_rating");

                            singleMovie.setRating(rating);

                            String genresString = "";
                            String starsString = "";
                            ArrayList<Genre> gList = singleMovie.getGenres();
                            ArrayList<Star> sList = singleMovie.getStars();

                            for (Genre g : gList) {
                                genresString += g.getName() + ", ";
                            }
                            for (Star s : sList) {
                                starsString += s.getName() + ", ";
                            }

                            titleView.setText(singleMovie.getName());
                            // need to cast the year to a string to set the label
                            yearView.setText("Year: " +singleMovie.getYear() + "");
                            directorView.setText("Director: " + singleMovie.getDirector());
                            genresView.setText(genresString);
                            starsView.setText(starsString);

                            //movies.add(newMovie);
                            //};

                        } catch (final JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(TAG, "VolleyError: " + error.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

//        moviesRequest.setRetryPolicy(new DefaultRetryPolicy(
//                9000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(moviesRequest);
    }
}

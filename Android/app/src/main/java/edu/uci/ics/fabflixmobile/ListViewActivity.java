package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;
import java.util.ArrayList;

public class ListViewActivity extends Activity  {

    private final String host = "3.143.213.98";
    private final String port = "8443";
    private final String domain = "cs122b-spring21-team-39";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private String TAG = ListViewActivity.class.getSimpleName();


    private ArrayList<Movie> movies = new ArrayList<>();
    private boolean hasNext = true;
    private boolean hasPrev = false;
    private int pageNumber = 1;
    private String searchField = "";



    private Button prevButton;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        // TODO: this should be retrieved from the backend server
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(view -> prevMovies());
        nextButton.setOnClickListener(view -> nextMovies());

        searchField = getIntent().getExtras().getString("searchField");

        this.getRequest(searchField,  String.valueOf(pageNumber));



//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);
//
//        ListView listView = findViewById(R.id.list);
//        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            Movie movie = movies.get(position);
////            String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
////            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//        });

//        runningTask = new LongOperation(this, "good", "1");
//        runningTask.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void prevMovies() {
        pageNumber -= 1;
        movies.clear();
        this.getRequest(searchField,  String.valueOf(pageNumber));
    }

    public void nextMovies() {
        pageNumber += 1;
        movies.clear();
        this.getRequest(searchField,  String.valueOf(pageNumber));
    }

    private void getRequest(String title, String pageNumber) {

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        Context curr = this;

        String url = baseURL + "/api/movies?type=title&text=" + title +"&N=20&sorting-option1=rating&sorting-option2=&order=DESC&page=" + pageNumber;

        JsonObjectRequest moviesRequest = new JsonObjectRequest
        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    hasPrev = ((int) response.getInt("page") > 1);
                    hasNext = ((boolean) response.getBoolean("hasNext") ? true : false);

                    if(hasPrev) {
                        prevButton.setEnabled(true);
                    }
                    else {
                        prevButton.setEnabled(false);
                    }

                    if(hasNext) {
                        nextButton.setEnabled(true);
                    }
                    else {
                        nextButton.setEnabled(false);
                    }

                    JSONArray respMovies = response.getJSONArray("results");

                    for(int i = 0; i < respMovies.length(); i++) {

                        JSONObject currMovie = respMovies.getJSONObject(i);

                        String movieId = currMovie.getString("movie_id");
                        String movieTitle = currMovie.getString("movie_title");
                        short movieYear = (short) currMovie.getInt("movie_year");
                        String movieDirector = currMovie.getString("movie_director");

                        Movie newMovie = new Movie(movieId, movieTitle, movieYear, movieDirector);

                        JSONArray genres = currMovie.getJSONArray("movie_genres");

                        for(int j = 0; j < genres.length(); j++) {

                            JSONObject currGenre = genres.getJSONObject(j);

                            String genreName = currGenre.getString("movie_genre");

                            Genre newGenre = new Genre(null, genreName);
                            newMovie.addGenre(newGenre);
                        }

                        JSONArray stars = currMovie.getJSONArray("movie_stars");

                        for(int j = 0; j < stars.length(); j++) {

                            JSONObject currStars = stars.getJSONObject(j);

                            String starId = currStars.getString("star_id");
                            String starName = currStars.getString("star_name");

                            Star newStar = new Star(starId, starName, (short) -100);
                            newMovie.addStar(newStar);
                        }

                        Double rating = currMovie.getDouble("movie_rating");

                        newMovie.setRating(rating);

                        movies.add(newMovie);
                    }

                    MovieListViewAdapter adapter = new MovieListViewAdapter(movies, curr);

                    ListView listView = findViewById(R.id.list);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        Movie movie = movies.get(position);
                        Intent newAct = new Intent(ListViewActivity.this, SingleMovieViewActivity.class);
                        newAct.putExtra("id", movie.getId());
                        startActivity(newAct);
    //            String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getName(), movie.getYear());
    //            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        });

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
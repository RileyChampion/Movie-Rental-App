package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.row, movies);
        this.movies = movies;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row, parent, false);

        Movie movie = movies.get(position);

        TextView titleView = view.findViewById(R.id.title);
        TextView directorView = view.findViewById(R.id.director);
        TextView yearView = view.findViewById(R.id.year);
        TextView ratingView = view.findViewById(R.id.rating);
        TextView genresView = view.findViewById(R.id.genres);
        TextView starsView = view.findViewById(R.id.stars);

        titleView.setText(movie.getName());
        // need to cast the year to a string to set the label
        directorView.setText("Director: " + movie.getDirector());
        yearView.setText("Year: " +movie.getYear() + "");
        ratingView.setText("Ratings: " + movie.getRating() + "");


        String genres = "Genres: ";

        ArrayList<Genre> genresList = movie.getGenres();
        int len = 0;

        for(Genre g : genresList) {
            genres += g.getName();
            len += 1;
            if((len < genresList.size())) {
                genres += ", ";
            }
        }

        genresView.setText(genres);

        String stars = "Stars: ";

        ArrayList<Star> starsList = movie.getStars();
        len = 0;

        for(Star s : starsList) {
            stars += s.getName();
            len += 1;
            if((len < starsList.size())) {
                stars += ", ";
            }
        }

        starsView.setText(stars);

        return view;
    }
}
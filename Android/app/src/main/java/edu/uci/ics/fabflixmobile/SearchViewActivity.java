package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SearchViewActivity extends Activity {

    private EditText searchField;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search);

        searchField = findViewById(R.id.movieSearch);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(view -> search());
    }

    public void search() {
        Intent listPage = new Intent(SearchViewActivity.this, ListViewActivity.class);
        listPage.putExtra("searchField", searchField.getText().toString());
        startActivity(listPage);
    }

}

package com.andrew.associate.projectmoviecatalogue3.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.andrew.associate.projectmoviecatalogue3.R;
import com.andrew.associate.projectmoviecatalogue3.adapter.MovieAdapter;
import com.andrew.associate.projectmoviecatalogue3.entity.Movie;
import com.andrew.associate.projectmoviecatalogue3.entity.MovieFavorite;
import com.andrew.associate.projectmoviecatalogue3.entity.MovieResult;
import com.andrew.associate.projectmoviecatalogue3.rest.MovieClient;
import com.andrew.associate.projectmoviecatalogue3.rest.MovieInterface;
import com.andrew.associate.projectmoviecatalogue3.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.andrew.associate.projectmoviecatalogue3.utils.Utils.API_KEY;
import static com.andrew.associate.projectmoviecatalogue3.utils.Utils.INTENT_DETAIL;
import static com.andrew.associate.projectmoviecatalogue3.utils.Utils.INTENT_TAG;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.recycler_search)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar_search)
    Toolbar toolbar;

    MovieAdapter movieAdapter;

    List<MovieResult> movieList;
    MovieInterface movieService;
    Call<Movie> movieCall;
    Call<MovieResult> movieFavoriteCall;
    MovieResult movieResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.search_title));
        }

        movieList = new ArrayList<>();
        movieResult = new MovieResult();

        if(getIntent() != null){
            if (getIntent().getStringExtra(INTENT_TAG).equals("search")){
                String q = getIntent().getStringExtra(Utils.INTENT_SEARCH);
                initView();
                getMovies(q);
            }else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.favorite));
                }
                initView();
                ArrayList<MovieFavorite> movieFavoriteArrayList = getIntent()
                        .getParcelableArrayListExtra(INTENT_DETAIL);
                for (MovieFavorite mF : movieFavoriteArrayList){
                    getFavoriteMovies(mF.getId());
                }
            }
        }
    }

    void initView(){
        movieAdapter = new MovieAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getFavoriteMovies(String id){
        movieService = MovieClient.getClient().create(MovieInterface.class);
        movieFavoriteCall = movieService.getMovieById(id, API_KEY);

        movieFavoriteCall.enqueue(new Callback<MovieResult>(){

            @Override
            public void onResponse(@NonNull Call<MovieResult> call, @NonNull Response<MovieResult> response) {
                movieList.add(response.body());
                movieAdapter.setMovieResult(movieList);
                recyclerView.setAdapter(movieAdapter);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResult> call,@NonNull Throwable t) {
                movieResult = null;
            }
        });
    }

    private void getMovies(final String q){
        movieService = MovieClient.getClient().create(MovieInterface.class);
        movieCall = movieService.getMovieBySearch(q, API_KEY);

        movieCall.enqueue(new Callback<Movie>(){
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.body() != null){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                        movieList = Objects.requireNonNull(response.body()).getResults();
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle(getString(R.string.search_hint_result,
                                Objects.requireNonNull(response.body())
                                    .getTotalResults().toString(), q));
                    }
                }
                movieAdapter.setMovieResult(movieList);
                recyclerView.setAdapter(movieAdapter);
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this, "There is Something Wrong"
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

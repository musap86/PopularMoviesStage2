package com.udacity.and.popularmovies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.udacity.and.popularmovies.adapters.ReviewsAdapter;
import com.udacity.and.popularmovies.adapters.TrailersAdapter;
import com.udacity.and.popularmovies.data.DataContract;
import com.udacity.and.popularmovies.utilities.DateUtils;
import com.udacity.and.popularmovies.utilities.IListItemClickListener;
import com.udacity.and.popularmovies.utilities.JsonUtils;
import com.udacity.and.popularmovies.utilities.MovieDetails;
import com.udacity.and.popularmovies.utilities.NetworkUtils;
import com.udacity.and.popularmovies.utilities.UserPrefs;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("WeakerAccess")
public class DetailsActivity
        extends AppCompatActivity
        implements IListItemClickListener,
        LoaderManager.LoaderCallbacks<Object> {

    private final String TAG = DetailsActivity.class.getSimpleName();
    private final int FAVORITES_LOADER = 51;
    private final int DETAIL_LOADER = 54;
    private final int TRAILER_LOADER = 55;
    private final int REVIEW_LOADER = 56;
    @BindView(R.id.rv_reviews)
    RecyclerView mReviews;
    @BindView(R.id.rv_trailers)
    RecyclerView mTrailers;
    @BindView(R.id.iv_backdrop)
    ImageView mHeaderImage;
    @BindView(R.id.tv_title)
    TextView mTextViewTitle;
    @BindView(R.id.tv_release_year)
    TextView mTextViewRelDate;
    @BindView(R.id.tv_movie_runtime)
    TextView mTextViewMovieLength;
    @BindView(R.id.tv_movie_runtime_suffix)
    TextView mTextViewMovieRuntimeSuffix;
    @BindView(R.id.tv_average_vote)
    TextView mTextViewAvgVote;
    @BindView(R.id.tv_plot_synopsis)
    TextView mTextViewPlot;
    @BindView(R.id.tv_trailers_header)
    TextView mTrailersHeader;
    @BindView(R.id.tv_reviews_header)
    TextView mReviewsHeader;
    @BindView(R.id.tv_no_trailers)
    TextView mNoTrailers;
    @BindView(R.id.tv_no_reviews)
    TextView mNoReviews;
    @BindView(R.id.iv_detail_movie_poster)
    ImageView mPoster;
    @BindView(R.id.mark_as_favorite_button)
    Button mFavoriteButton;
    private Map<String, String> mMovieDetails;
    private ReviewsAdapter mReviewsAdapter;
    private TrailersAdapter mTrailersAdapter;
    private int movieId;
    private final View.OnClickListener mFavoriteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isFavorite = (boolean) view.getTag();
            if (isFavorite) {
                getContentResolver().delete(DataContract.DataEntry.CONTENT_URI,
                        DataContract.DataEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movieId)});
            } else {
                ContentValues cv = new ContentValues();
                cv.put(DataContract.DataEntry.COLUMN_MOVIE_ID, movieId);
                cv.put(DataContract.DataEntry.COLUMN_TITLE,
                        mMovieDetails.get(JsonUtils.JSON_VAR_TITLE));
                cv.put(DataContract.DataEntry.COLUMN_POSTER_PATH,
                        mMovieDetails.get(JsonUtils.JSON_VAR_POSTER));
                cv.put(DataContract.DataEntry.COLUMN_SYNOPSIS,
                        mMovieDetails.get(JsonUtils.JSON_VAR_OVERVIEW));
                cv.put(DataContract.DataEntry.COLUMN_USER_RATING,
                        mMovieDetails.get(JsonUtils.JSON_VAR_VOTE_AVG));
                cv.put(DataContract.DataEntry.COLUMN_RELEASE_DATE,
                        mMovieDetails.get(JsonUtils.JSON_VAR_RELEASE));
                cv.put(DataContract.DataEntry.COLUMN_BACKDROP_PATH,
                        mMovieDetails.get(JsonUtils.JSON_VAR_BACKDROP));
                cv.put(DataContract.DataEntry.COLUMN_RUNTIME,
                        mMovieDetails.get(JsonUtils.JSON_VAR_RUNTIME));
                getContentResolver().insert(DataContract.DataEntry.CONTENT_URI, cv);
            }
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, DetailsActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        String MOVIE_ID_NAME = "com.udacity.and.popularmovies.MovieId";
        movieId = getIntent().getIntExtra(MOVIE_ID_NAME, 0);
        getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this);
        mFavoriteButton.setOnClickListener(mFavoriteButtonListener);
        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);
        mTrailers.setLayoutManager(trailersLayoutManager);
        mTrailers.setHasFixedSize(true);
        mTrailersAdapter = new TrailersAdapter(this);
        mTrailers.setAdapter(mTrailersAdapter);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);
        mReviews.setLayoutManager(reviewsLayoutManager);
        mReviews.setHasFixedSize(true);
        mReviewsAdapter = new ReviewsAdapter();
        mReviews.setAdapter(mReviewsAdapter);
        mNoTrailers.setVisibility(View.GONE);
        mNoReviews.setVisibility(View.GONE);
        mTrailersHeader.setVisibility(View.GONE);
        mReviewsHeader.setVisibility(View.GONE);
        mTrailers.setVisibility(View.GONE);
        mReviews.setVisibility(View.GONE);
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getSupportLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getSupportLoaderManager().initLoader(REVIEW_LOADER, null, this);
    }

    @Override
    public void onListItemClick(int clickedItemIndex, View itemView) {
        String key = MovieDetails.getTrailerSource(clickedItemIndex);
        Uri videoUri = Uri.parse("vnd.youtube:" + key);
        Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            videoUri = Uri.parse(NetworkUtils.BASE_YOUTUBE_URL + key);
            intent = new Intent(Intent.ACTION_VIEW, videoUri);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_share:
                String key = MovieDetails.getTrailerSource(0);
                if (key == null || key.length() == 0)
                    return super.onOptionsItemSelected(item);
                Uri videoUri = Uri.parse(NetworkUtils.BASE_YOUTUBE_URL + key);
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle(R.string.share_chooser_title)
                        .setType("text/plain")
                        .setSubject(mMovieDetails.get(JsonUtils.JSON_VAR_TITLE))
                        .setText(videoUri.toString())
                        .startChooser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Object> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<Object>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Object loadInBackground() {
                URL url;
                switch (id) {
                    case FAVORITES_LOADER:
                        try {
                            return getContentResolver().query(DataContract.DataEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    case DETAIL_LOADER:
                        if (NetworkUtils.isOnline(getApplicationContext())) {
                            url = NetworkUtils.generateURL(movieId);
                            return getResponseFromServer(url);
                        } else {
                            try {
                                return getContentResolver().query(DataContract.DataEntry.CONTENT_URI,
                                        null,
                                        DataContract.DataEntry.COLUMN_MOVIE_ID + "=?",
                                        new String[]{String.valueOf(movieId)},
                                        DataContract.DataEntry.COLUMN_TIMESTAMP);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to asynchronously load data.");
                                e.printStackTrace();
                                return null;
                            }
                        }
                    case TRAILER_LOADER:
                        if (!NetworkUtils.isOnline(getApplicationContext())) {
                            return null;
                        }
                        url = NetworkUtils.generateURL(movieId, NetworkUtils.Endpoint.VIDEO);
                        return getResponseFromServer(url);
                    case REVIEW_LOADER:
                        if (!NetworkUtils.isOnline(getApplicationContext())) {
                            return null;
                        }
                        url = NetworkUtils.generateURL(movieId, NetworkUtils.Endpoint.REVIEW);
                        return getResponseFromServer(url);
                    default:
                        return null;
                }
            }

            private String getResponseFromServer(URL url) {
                try {
                    return NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        if (data != null) {
            switch (loader.getId()) {
                case FAVORITES_LOADER:
                    Cursor cursor = (Cursor) data;
                    boolean isFavorite = false;
                    while (cursor.moveToNext() && !isFavorite) {
                        int id = cursor.getInt(cursor.getColumnIndex(
                                DataContract.DataEntry.COLUMN_MOVIE_ID));
                        isFavorite = id == movieId;
                    }
                    cursor.close();
                    if (isFavorite) {
                        mFavoriteButton
                                .setBackgroundColor(getResources().getColor(R.color.colorButtonRemoveFavorite));
                        mFavoriteButton.setText(R.string.remove_favorite);
                    } else {
                        mFavoriteButton
                                .setBackgroundColor(getResources().getColor(R.color.colorButtonMarkFavorite));
                        mFavoriteButton.setText(R.string.mark_favorite);
                    }
                    mFavoriteButton.setTag(isFavorite);
                    break;
                case DETAIL_LOADER:
                    if (NetworkUtils.isOnline(this)) {
                        mMovieDetails = JsonUtils.getMovieDetails((String) data);
                    } else {
                        mMovieDetails = MovieDetails.getMovieDetails((Cursor) data);
                    }
                    mTextViewTitle.setText(mMovieDetails.get(JsonUtils.JSON_VAR_TITLE));
                    String releaseYear = DateUtils.getYearFromDateString(mMovieDetails.get(JsonUtils.JSON_VAR_RELEASE));
                    mTextViewRelDate.setText(releaseYear);
                    String averageVote = mMovieDetails.get(JsonUtils.JSON_VAR_VOTE_AVG) +
                            getResources().getString(R.string.full_vote_point);
                    mTextViewAvgVote.setText(averageVote);
                    mTextViewPlot.setText(mMovieDetails.get(JsonUtils.JSON_VAR_OVERVIEW));
                    String runtime = mMovieDetails.get(JsonUtils.JSON_VAR_RUNTIME);
                    if (runtime.equals("null"))
                        mTextViewMovieRuntimeSuffix.setVisibility(View.GONE);
                    else {
                        mTextViewMovieRuntimeSuffix.setVisibility(View.VISIBLE);
                        mTextViewMovieLength.setText(runtime);
                    }
                    float rating = Float.parseFloat(mMovieDetails.get(JsonUtils.JSON_VAR_VOTE_AVG));
                    if (rating == 0) {
                        mTextViewAvgVote.setVisibility(View.GONE);
                    } else {
                        mTextViewMovieRuntimeSuffix.setVisibility(View.VISIBLE);
                    }
                    final String backDropPath = NetworkUtils.generateURL(
                            mMovieDetails.get(JsonUtils.JSON_VAR_BACKDROP), UserPrefs.getImageQuality(), true);
                    Picasso.with(this)
                            .load(backDropPath)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mHeaderImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(DetailsActivity.this)
                                            .load(backDropPath)
                                            .placeholder(R.mipmap.poster_placeholder)
                                            .error(R.mipmap.no_poster_image)
                                            .into(mHeaderImage);
                                }
                            });
                    final String POSTER_IMAGE_PATH = NetworkUtils.generateURL(
                            mMovieDetails.get(JsonUtils.JSON_VAR_POSTER), UserPrefs.getImageQuality(), false);
                    Picasso.with(this)
                            .load(POSTER_IMAGE_PATH)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mPoster, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(DetailsActivity.this)
                                            .load(POSTER_IMAGE_PATH)
                                            .placeholder(R.mipmap.poster_placeholder)
                                            .error(R.mipmap.no_poster_image)
                                            .into(mPoster);
                                }
                            });
                    break;
                case TRAILER_LOADER:
                    JsonUtils.extractVideosFromJson((String) data);
                    mTrailersAdapter = new TrailersAdapter(DetailsActivity.this);
                    mTrailers.setAdapter(mTrailersAdapter);
                    if (mTrailersAdapter.getItemCount() > 0) {
                        mTrailersHeader.setVisibility(View.VISIBLE);
                        mTrailers.setVisibility(View.VISIBLE);
                    } else {
                        mNoTrailers.setVisibility(View.VISIBLE);
                        mTrailersHeader.setVisibility(View.INVISIBLE);
                    }
                    break;
                case REVIEW_LOADER:
                    JsonUtils.extractReviewsFromJson((String) data);
                    mReviewsAdapter = new ReviewsAdapter();
                    mReviews.setAdapter(mReviewsAdapter);
                    if (mReviewsAdapter.getItemCount() > 0) {
                        mReviewsHeader.setVisibility(View.VISIBLE);
                        mReviews.setVisibility(View.VISIBLE);
                    } else {
                        mNoReviews.setVisibility(View.VISIBLE);
                        mReviewsHeader.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {
    }
}
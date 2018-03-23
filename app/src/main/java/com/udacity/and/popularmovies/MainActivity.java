package com.udacity.and.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.udacity.and.popularmovies.adapters.PostersAdapter;
import com.udacity.and.popularmovies.data.DataContract;
import com.udacity.and.popularmovies.data.MovieDetails;
import com.udacity.and.popularmovies.data.UserPrefs;
import com.udacity.and.popularmovies.utilities.JsonUtils;
import com.udacity.and.popularmovies.utilities.NetworkUtils;
import com.udacity.and.popularmovies.utilities.PageStatus;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity
        extends AppCompatActivity
        implements IListItemClickListener,
        LoaderManager.LoaderCallbacks<Object>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        AdapterView.OnItemSelectedListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private final String POSTERS_STATE_KEY = "poster_list_state";
    private final String FIRST_VISIBLE_ITEM_KEY = "first_visible_item";
    private final String LAST_VISIBLE_ITEM_KEY = "last_visible_item";
    private final int FAVORITES_LOADER = 51;
    private final int MOVIE_DATA_LOADER = 52;
    private final int EXTENSION_LOADER = 53;
    private final int ONE_PAGE = 20;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.rv_movie_posters)
    RecyclerView mPosterRecyclerView;
    private PageStatus mMostPopularPageStatus;
    private PageStatus mTopRatedPageStatus;
    private Parcelable mMoviePostersState;
    private GridLayoutManager mLayoutManager;
    private PostersAdapter mAdapter;
    private int mMaxPosterWidth;
    private int mExtendingPage;
    private int mLastPage;
    private int mFirstVisibleItemPos;
    private int mLastVisibleItemPos;
    private boolean isScrollingDown;
    private boolean isLoadersAllowed;
    private boolean isLoadingExtension;
    /**
     * Loads one more page when user reaches to bottom or top of the list, keeps user's view
     * on the item they already see and unloads the previous page.
     */
    private final RecyclerView.OnScrollListener mPostersScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    // Don't try page scrolling if user is on FAVORITES or is not online.
                    if (UserPrefs.getSortOrder(MainActivity.this) == NetworkUtils.SortOrder.FAVORITES
                            || !NetworkUtils.isOnline(MainActivity.this)) {
                        return;
                    }
                    final int DIRECTION_UP = -1;
                    final int DIRECTION_DOWN = 1;
                    if (!recyclerView.canScrollVertically(DIRECTION_DOWN)) {
                        // User reached to bottom of the list. If there's more to load then load it.
                        if (CurrentPage.lowerSide < mLastPage) {
                            // If there's already one task running for pagination, abort this.
                            if (isLoadingExtension) {
                                Log.v(TAG, "EXTENSION LOADER - canceled loading, " +
                                        "task is already running: " + mExtendingPage);
                                return;
                            }
                            isLoadingExtension = true;



                            CurrentPage.increment();
                            mExtendingPage = CurrentPage.lowerSide;
                            isScrollingDown = true;
                            mFirstVisibleItemPos = mLayoutManager.findFirstVisibleItemPosition();
                            getSupportLoaderManager()
                                    .restartLoader(EXTENSION_LOADER, null, MainActivity.this);
                        }
                    } else if (!recyclerView.canScrollVertically(DIRECTION_UP)) {
                        // User reached to top of the list. If there's more to load then load it.
                        final int FIRST_PAGE = 1;
                        if (CurrentPage.upperSide > FIRST_PAGE) {
                            if (isLoadingExtension) {
                                Log.v(TAG, "EXTENSION LOADER - canceled loading, " +
                                        "task is already running: " + mExtendingPage);
                                return;
                            }
                            isLoadingExtension = true;


                            Log.e(TAG, "Decremented");
                            mExtendingPage = CurrentPage.upperSide;
                            isScrollingDown = false;
                            mLastVisibleItemPos = mLayoutManager.findLastVisibleItemPosition();
                            getSupportLoaderManager()
                                    .restartLoader(EXTENSION_LOADER, null, MainActivity.this);
                        }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // TODO: Provide a string resource file (api_key.xml) which contains the api key.
        NetworkUtils.PARAM_API_KEY = getString(R.string.the_movie_database_api_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        UserPrefs.setImageQuality(Integer.parseInt(
                prefs.getString(getString(R.string.pref_quality_key), getString(R.string.pref_quality_value_2))));
        mMostPopularPageStatus = new PageStatus();
        mTopRatedPageStatus = new PageStatus();
        mLayoutManager = new GridLayoutManager(this, optimizePosterWidth());
        mPosterRecyclerView.setLayoutManager(mLayoutManager);
        mPosterRecyclerView.setHasFixedSize(true);
        mAdapter = new PostersAdapter(this, mMaxPosterWidth);
        mPosterRecyclerView.setAdapter(mAdapter);
        mPosterRecyclerView.addOnScrollListener(mPostersScrollListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMoviePostersState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(POSTERS_STATE_KEY, mMoviePostersState);
        outState.putInt(FIRST_VISIBLE_ITEM_KEY, mFirstVisibleItemPos);
        outState.putInt(LAST_VISIBLE_ITEM_KEY, mLastVisibleItemPos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isLoadersAllowed = false;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mMoviePostersState = savedInstanceState.getParcelable(POSTERS_STATE_KEY);
            mFirstVisibleItemPos = savedInstanceState.getInt(FIRST_VISIBLE_ITEM_KEY);
            mLastVisibleItemPos = savedInstanceState.getInt(LAST_VISIBLE_ITEM_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserPrefs.getSortOrder(this) == NetworkUtils.SortOrder.FAVORITES) {
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private int optimizePosterWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int posterGridColCount = UserPrefs.getGridColumnCount(metrics);
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            posterGridColCount *= (metrics.widthPixels / metrics.heightPixels);
        }
        mMaxPosterWidth = metrics.widthPixels / posterGridColCount;
        return posterGridColCount;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isLoadersAllowed = true;
    }

    private void loadMoviesData() {
        if (UserPrefs.getSortOrder(this) == NetworkUtils.SortOrder.FAVORITES) {
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, this);
        } else {
            if (!NetworkUtils.isOnline(this)) {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                return;
            }
            getSupportLoaderManager().restartLoader(MOVIE_DATA_LOADER, null, this);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex, View itemView) {
        int movieId = (int) itemView.getTag();
        Intent intent = new Intent(this, DetailsActivity.class);
        final String MOVIE_ID_NAME = "com.udacity.and.popularmovies.MovieId";
        intent.putExtra(MOVIE_ID_NAME, movieId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Spinner spinner = (Spinner) menu.findItem(R.id.spinner).getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_style_array, R.layout.spinner_text_view);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(UserPrefs.getSortOrderIndex(this));
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "Spinner item is selected on position: " + position);
        switch (position) {
            case 0:
                mPosterRecyclerView.getRecycledViewPool().clear();
                UserPrefs.setSortOrder(NetworkUtils.SortOrder.MOST_POPULAR);
                loadMoviesData();
                break;
            case 1:
                mPosterRecyclerView.getRecycledViewPool().clear();
                UserPrefs.setSortOrder(NetworkUtils.SortOrder.TOP_RATED);
                loadMoviesData();
                break;
            case 2:
                UserPrefs.setSortOrder(NetworkUtils.SortOrder.FAVORITES);
                loadMoviesData();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.pref_quality_key))) {
            String pref = prefs.getString(getString(R.string.pref_quality_key), getString(R.string.pref_quality_value_2));
            UserPrefs.setImageQuality(Integer.parseInt(pref));
            mLayoutManager.setSpanCount(optimizePosterWidth());
            mAdapter.setMaxPosterWidth(mMaxPosterWidth);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Object> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<Object>(this) {

            @Override
            protected void onStartLoading() {
                /*
                   Here, recycler view makes things complicated. While returning back to the
                   MainActivity from DetailsActivity these loaders are starting themselves
                   automatically and this makes the page scrolling a mess.
                 */
                if (isLoadersAllowed) {
                    forceLoad();
                } else {
                    Log.v(TAG, "Loader is not allowed. Loader id: " + id);
                }
            }

            @Override
            public Object loadInBackground() {
                Log.v(TAG, "Loading in background. Loader id: " + id);
                switch (id) {
                    case FAVORITES_LOADER:
                        try {
                            return getContentResolver().query(DataContract.DataEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    DataContract.DataEntry.COLUMN_TIMESTAMP);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    case MOVIE_DATA_LOADER:
                        URL movieRequestUrl = NetworkUtils.generateURL(
                                UserPrefs.getSortOrder(MainActivity.this), CurrentPage.upperSide);
                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    case EXTENSION_LOADER:
                        Log.v(TAG, "EXTENSION LOADER - loading extending page: " + mExtendingPage);
                        URL movieExtendRequestUrl = NetworkUtils.generateURL(
                                UserPrefs.getSortOrder(MainActivity.this), mExtendingPage);
                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieExtendRequestUrl);
                        } catch (IOException e) {
                            if (isScrollingDown)
                                CurrentPage.decrement();
                            else
                                CurrentPage.increment();
                            // Loader for new items are aborted.
                            isLoadingExtension = false;
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    default:
                        return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        if (data != null) {
            // Response will only be a Cursor object if it comes from a database.
            // So, it will be a String object (JSON) if it comes from server.
            if (data instanceof String) {
                if (loader.getId() == EXTENSION_LOADER) {
                    populateExtension((String) data);
                } else {
                    // In case app starts, device rotates or user switches to another sorting order.
                    // Upper part of the page is populated, now it's time to populate lower part.
                    JsonUtils.extractMovieDataFromJson((String) data);
                    isLoadingExtension = true;
                    mExtendingPage = CurrentPage.lowerSide;
                    isScrollingDown = true;
                    getSupportLoaderManager()
                            .restartLoader(EXTENSION_LOADER, null, MainActivity.this);
                }
                mLastPage = MovieDetails.getPageCount();
                CurrentPage.upperSide = MovieDetails.getPage(0);
                if (MovieDetails.getMovieCountInView() > ONE_PAGE) {
                    CurrentPage.lowerSide = MovieDetails.getPage(ONE_PAGE);
                    Toast.makeText(this, getString(R.string.pages_shown) + ": " +
                            CurrentPage.upperSide + " & " + CurrentPage.lowerSide + " / " +
                            mLastPage, Toast.LENGTH_SHORT).show();
                    Log.v(TAG, getString(R.string.pages_shown) + ": " +
                            CurrentPage.upperSide + "," + CurrentPage.lowerSide + " / " + mLastPage);
                } else {
                    Log.v(TAG, getString(R.string.pages_shown) + ": " +
                            CurrentPage.upperSide + " / " + mLastPage);
                }
                mAdapter.setCursor(null);
            } else if (data instanceof Cursor) {
                mAdapter.setCursor((Cursor) data);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {
        if (isLoadingExtension) {
            isLoadingExtension = false;
            Log.e(TAG, "Loader for new items is reset.");
        }
    }

    /**
     * Provides images for the posters list by combining two different loader results. One result is
     * already on the screen as a part of the recycler view. As a result, there will be 40 items in
     * the posters list, 20 from each.
     *
     * @param data JSON response from the loader
     */
    private void populateExtension(String data) {
        final int TWO_PAGES = 2 * ONE_PAGE;
        int[] extensionIds = new int[TWO_PAGES];
        int[] extensionPages = new int[TWO_PAGES];
        String[] extensionPosters = new String[TWO_PAGES];
        String[] extensionTitles = new String[TWO_PAGES];
        if (isScrollingDown) {
            // User is scrolling downwards
            if (MovieDetails.getMovieCountInView() <= ONE_PAGE) {
                for (int i = 0; i < ONE_PAGE; i++) {
                    extensionIds[i] = MovieDetails.getId(i);
                    extensionPages[i] = MovieDetails.getPage(i);
                    extensionPosters[i] = MovieDetails.getImagePath(i);
                    extensionTitles[i] = MovieDetails.getMovieTitle(i);
                }
                JsonUtils.extractMovieDataFromJson(data);
                for (int i = ONE_PAGE; i < TWO_PAGES; i++) {
                    extensionIds[i] = MovieDetails.getId(i - ONE_PAGE);
                    extensionPages[i] = MovieDetails.getPage(i - ONE_PAGE);
                    extensionPosters[i] = MovieDetails.getImagePath(i - ONE_PAGE);
                    extensionTitles[i] = MovieDetails.getMovieTitle(i - ONE_PAGE);
                }
            } else {
                for (int i = 0; i < ONE_PAGE; i++) {
                    extensionIds[i] = MovieDetails.getId(i + ONE_PAGE);
                    extensionPages[i] = MovieDetails.getPage(i + ONE_PAGE);
                    extensionPosters[i] = MovieDetails.getImagePath(i + ONE_PAGE);
                    extensionTitles[i] = MovieDetails.getMovieTitle(i + ONE_PAGE);
                }
                JsonUtils.extractMovieDataFromJson(data);
                for (int i = ONE_PAGE; i < TWO_PAGES; i++) {
                    extensionIds[i] = MovieDetails.getId(i - ONE_PAGE);
                    extensionPages[i] = MovieDetails.getPage(i - ONE_PAGE);
                    extensionPosters[i] = MovieDetails.getImagePath(i - ONE_PAGE);
                    extensionTitles[i] = MovieDetails.getMovieTitle(i - ONE_PAGE);
                }
                // Scroll to late images in view
                mPosterRecyclerView.scrollToPosition(mFirstVisibleItemPos - ONE_PAGE);
            }
        } else {
            // User is scrolling upwards
            for (int i = ONE_PAGE; i < TWO_PAGES; i++) {
                extensionIds[i] = MovieDetails.getId(i - ONE_PAGE);
                extensionPages[i] = MovieDetails.getPage(i - ONE_PAGE);
                extensionPosters[i] = MovieDetails.getImagePath(i - ONE_PAGE);
                extensionTitles[i] = MovieDetails.getMovieTitle(i - ONE_PAGE);
            }
            JsonUtils.extractMovieDataFromJson(data);
            for (int i = 0; i < ONE_PAGE; i++) {
                extensionIds[i] = MovieDetails.getId(i);
                extensionPages[i] = MovieDetails.getPage(i);
                extensionPosters[i] = MovieDetails.getImagePath(i);
                extensionTitles[i] = MovieDetails.getMovieTitle(i);
            }
            // Scroll to late images in view
            mPosterRecyclerView.scrollToPosition(mLastVisibleItemPos + ONE_PAGE);
        }
        // If upper side and lower side of the page is the same, then cancel the process.
        if (extensionPages[0] == extensionPages[ONE_PAGE]) {
            isLoadingExtension = false;
            return;
        }
        // Make new data be seen.
        MovieDetails.setIds(extensionIds);
        MovieDetails.setPages(extensionPages);
        MovieDetails.setPosterPaths(extensionPosters);
        MovieDetails.setMovieTitles(extensionTitles);
        mAdapter.notifyDataSetChanged();
        // Loader for new items are complete.
        isLoadingExtension = false;
    }

    /**
     * Cache for page status to keep track of page scrolling
     */
    private static class CurrentPage {
        static int upperSide = 1;
        static int lowerSide = 2;

        /**
         * Increments page status
         */
        static void increment() {
            upperSide++;
            lowerSide++;
        }

        /**
         * Decrements page status
         */
        static void decrement() {
            upperSide--;
            lowerSide--;
        }
    }

    public static class Popular {
        final static PageStatus page = new PageStatus();

        static PageStatus CurrentPage() {
            return page;
        }
    }

    public static class TopRated {
        final static PageStatus page = new PageStatus();

        static PageStatus CurrentPage() {
            return page;
        }
    }
}

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
import com.udacity.and.popularmovies.utilities.IListItemClickListener;
import com.udacity.and.popularmovies.utilities.JsonUtils;
import com.udacity.and.popularmovies.utilities.MovieDetails;
import com.udacity.and.popularmovies.utilities.NetworkUtils;
import com.udacity.and.popularmovies.utilities.PageStatus;
import com.udacity.and.popularmovies.utilities.UserPrefs;

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
    private final String POSTERS_STATE = "poster_list_state";
    private final String FIRST_VISIBLE_ITEM_MP = "most_popular_first_visible_item";
    private final String FIRST_VISIBLE_ITEM_TR = "top_rated_first_visible_item";
    private final String FIRST_VISIBLE_ITEM = "first_visible_item";
    private final String LAST_VISIBLE_ITEM = "last_visible_item";
    private final String MOST_POPULAR_UPPER = "most_popular_upper";
    private final String MOST_POPULAR_LOWER = "most_popular_lower";
    private final String TOP_RATED_UPPER = "top_rated_upper";
    private final String TOP_RATED_LOWER = "top_rated_lower";
    private final int FAVORITES_LOADER = 51;
    private final int ONLINE_LOADER = 52;
    private final int ONLINE_EXTENSION_LOADER = 53;
    private final int ONE_PAGE = 20;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.rv_movie_posters)
    RecyclerView mPosterRecyclerView;
    private NetworkUtils.order mOrder;
    private PageStatus mMostPopularPage;
    private PageStatus mTopRatedPage;
    private Parcelable mMoviePostersState;
    private GridLayoutManager mLayoutManager;
    private PostersAdapter mAdapter;
    private int mMaxPosterWidth;
    private int mExtension;
    private int mLastPage;
    private int mFirstVisibleItemPos;
    private int mLastVisibleItemPos;
    private int mFirstVisibleItemPosMP;
    private int mFirstVisibleItemPosTR;
    private boolean isScrollingDown;
    private boolean isLoaderAllowed;
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
                    if (mOrder == NetworkUtils.order.FAVORITES
                            || !NetworkUtils.isOnline(MainActivity.this)) {
                        return;
                    }
                    final int DIRECTION_UP = -1;
                    final int DIRECTION_DOWN = 1;
                    if (!recyclerView.canScrollVertically(DIRECTION_DOWN)) {
                        // "MOST POPULAR" and "TOP RATED" selections both have their own cache to keep
                        // their page status. User can see only two pages at a time. Scrolling downwards
                        // will load the next page and unload the oldest one.
                        int lowerPage;
                        if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                            lowerPage = mMostPopularPage.getLowerSide();
                        } else {
                            lowerPage = mTopRatedPage.getLowerSide();
                        }
                        // User reached to bottom of the list. If there's more to load then load it.
                        if (lowerPage < mLastPage) {
                            // If there's already one task running for pagination, abort this one.
                            if (isLoadingExtension) {
                                Log.v(TAG, "EXTENSION LOADER - canceled loading, " +
                                        "task is already running: " + mExtension);
                                return;
                            }
                            isLoadingExtension = true;
                            isScrollingDown = true;
                            mFirstVisibleItemPos = mLayoutManager.findFirstVisibleItemPosition();
                            if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                                mMostPopularPage.increment();
                                mExtension = mMostPopularPage.getLowerSide();
                            } else {
                                mTopRatedPage.increment();
                                mExtension = mTopRatedPage.getLowerSide();
                            }
                            getSupportLoaderManager()
                                    .restartLoader(ONLINE_EXTENSION_LOADER, null, MainActivity.this);
                        }
                    } else if (!recyclerView.canScrollVertically(DIRECTION_UP)) {
                        // User reached to top of the list. If there's more to load then load it.
                        final int FIRST_PAGE = 1;
                        int upperPage;
                        if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                            upperPage = mMostPopularPage.getUpperSide();
                        } else {
                            upperPage = mTopRatedPage.getUpperSide();
                        }
                        if (upperPage > FIRST_PAGE) {
                            if (isLoadingExtension) {
                                Log.v(TAG, "EXTENSION LOADER - canceled loading, " +
                                        "task is already running: " + mExtension);
                                return;
                            }
                            isLoadingExtension = true;
                            isScrollingDown = false;
                            mLastVisibleItemPos = mLayoutManager.findLastVisibleItemPosition();
                            if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                                mMostPopularPage.decrement();
                                mExtension = mMostPopularPage.getUpperSide();
                            } else {
                                mTopRatedPage.decrement();
                                mExtension = mTopRatedPage.getUpperSide();
                            }
                            getSupportLoaderManager()
                                    .restartLoader(ONLINE_EXTENSION_LOADER, null, MainActivity.this);
                        }
                    }
                }
            };
    private boolean isSortingChanged;

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
        mMostPopularPage = new PageStatus();
        mTopRatedPage = new PageStatus();
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
        outState.putParcelable(POSTERS_STATE, mMoviePostersState);
        outState.putInt(FIRST_VISIBLE_ITEM, mFirstVisibleItemPos);
        outState.putInt(LAST_VISIBLE_ITEM, mLastVisibleItemPos);
        outState.putInt(FIRST_VISIBLE_ITEM_MP, mFirstVisibleItemPosMP);
        outState.putInt(FIRST_VISIBLE_ITEM_TR, mFirstVisibleItemPosTR);
        outState.putInt(MOST_POPULAR_UPPER, mMostPopularPage.getUpperSide());
        outState.putInt(MOST_POPULAR_LOWER, mMostPopularPage.getLowerSide());
        outState.putInt(TOP_RATED_UPPER, mTopRatedPage.getUpperSide());
        outState.putInt(TOP_RATED_LOWER, mTopRatedPage.getLowerSide());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isLoaderAllowed = false;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mMoviePostersState = savedInstanceState.getParcelable(POSTERS_STATE);
            mLayoutManager.onRestoreInstanceState(mMoviePostersState);
            mFirstVisibleItemPos = savedInstanceState.getInt(FIRST_VISIBLE_ITEM);
            mLastVisibleItemPos = savedInstanceState.getInt(LAST_VISIBLE_ITEM);
            mFirstVisibleItemPosMP = savedInstanceState.getInt(FIRST_VISIBLE_ITEM_MP);
            mFirstVisibleItemPosTR = savedInstanceState.getInt(FIRST_VISIBLE_ITEM_TR);
            mMostPopularPage.setUpperSide(savedInstanceState.getInt(MOST_POPULAR_UPPER));
            mMostPopularPage.setLowerSide(savedInstanceState.getInt(MOST_POPULAR_LOWER));
            mTopRatedPage.setUpperSide(savedInstanceState.getInt(TOP_RATED_UPPER));
            mTopRatedPage.setLowerSide(savedInstanceState.getInt(TOP_RATED_LOWER));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOrder == NetworkUtils.order.FAVORITES) {
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
        isLoaderAllowed = true;
    }

    private void loadMoviesData() {
        mOrder = UserPrefs.getSortOrder(this);
        if (mOrder == NetworkUtils.order.FAVORITES) {
            getSupportLoaderManager().restartLoader(FAVORITES_LOADER, null, this);
        } else {
            if (!NetworkUtils.isOnline(this)) {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                return;
            }
            getSupportLoaderManager().restartLoader(ONLINE_LOADER, null, this);
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
        NetworkUtils.order order = UserPrefs.getSortOrder(this);
        switch (position) {
            case 0:
                if (mLayoutManager.getChildCount() != 0 && order != NetworkUtils.order.MOST_POPULAR) {
                    Log.e(TAG, "Sorting is changed!");
                    isSortingChanged = true;
                    if (order == NetworkUtils.order.TOP_RATED)
                        mFirstVisibleItemPosTR = mLayoutManager.findFirstVisibleItemPosition();
                }
                mPosterRecyclerView.getRecycledViewPool().clear();
                UserPrefs.setSortOrder(NetworkUtils.order.MOST_POPULAR);
                loadMoviesData();
                break;
            case 1:
                if (mLayoutManager.getChildCount() != 0 && order != NetworkUtils.order.TOP_RATED) {
                    Log.e(TAG, "Sorting is changed!");
                    isSortingChanged = true;
                    if (order == NetworkUtils.order.MOST_POPULAR)
                        mFirstVisibleItemPosMP = mLayoutManager.findFirstVisibleItemPosition();
                }
                mPosterRecyclerView.getRecycledViewPool().clear();
                UserPrefs.setSortOrder(NetworkUtils.order.TOP_RATED);
                loadMoviesData();
                break;
            case 2:
                UserPrefs.setSortOrder(NetworkUtils.order.FAVORITES);
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
                if (isLoaderAllowed) {
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
                    case ONLINE_LOADER:
                        int upperPage;
                        if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                            upperPage = mMostPopularPage.getUpperSide();
                        } else {
                            upperPage = mTopRatedPage.getUpperSide();
                        }
                        URL movieRequestUrl = NetworkUtils.generateURL(
                                mOrder, upperPage);
                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    case ONLINE_EXTENSION_LOADER:
                        Log.v(TAG, "EXTENSION LOADER - loading extending page: " + mExtension);
                        URL movieExtendRequestUrl = NetworkUtils.generateURL(
                                mOrder, mExtension);
                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieExtendRequestUrl);
                        } catch (IOException e) {
                            if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                                if (isScrollingDown)
                                    Popular.page.decrement();
                                else
                                    Popular.page.increment();
                            } else {
                                if (isScrollingDown)
                                    TopRated.page.decrement();
                                else
                                    TopRated.page.increment();
                            }
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
                if (loader.getId() == ONLINE_EXTENSION_LOADER) {
                    populateExtension((String) data);
                } else {
                    // In case app starts, device rotates or user switches to another sorting order.
                    // Upper part of the page is populated, now it's time to populate lower part.
                    JsonUtils.extractMovieDataFromJson((String) data);
                    isLoadingExtension = true;
                    if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                        mExtension = mMostPopularPage.getLowerSide();
                    } else {
                        mExtension = mTopRatedPage.getLowerSide();
                    }
                    isScrollingDown = true;
                    getSupportLoaderManager()
                            .restartLoader(ONLINE_EXTENSION_LOADER, null, MainActivity.this);
                }
                mLastPage = MovieDetails.getPageCount();
                if (mOrder == NetworkUtils.order.MOST_POPULAR) {
                    Popular.page.setUpperSide(MovieDetails.getPage(0));
                    if (MovieDetails.getMovieCountInView() > ONE_PAGE) {
                        Popular.page.setLowerSide(MovieDetails.getPage(ONE_PAGE));
                        Toast.makeText(this, getString(R.string.pages_shown) + ": " +
                                Popular.page.getUpperSide() + " & " +
                                Popular.page.getLowerSide() + " / " +
                                mLastPage, Toast.LENGTH_SHORT).show();
                        Log.v(TAG, getString(R.string.pages_shown) + ": " +
                                Popular.page.getUpperSide() + " & " +
                                Popular.page.getLowerSide() + " / " + mLastPage);
                    } else {
                        Log.v(TAG, getString(R.string.pages_shown) + ": " +
                                Popular.page.getUpperSide() + " / " + mLastPage);
                    }
                } else {
                    TopRated.page.setUpperSide(MovieDetails.getPage(0));
                    if (MovieDetails.getMovieCountInView() > ONE_PAGE) {
                        TopRated.page.setLowerSide(MovieDetails.getPage(ONE_PAGE));
                        Toast.makeText(this, getString(R.string.pages_shown) + ": " +
                                TopRated.page.getUpperSide() + " & " +
                                TopRated.page.getLowerSide() + " / " +
                                mLastPage, Toast.LENGTH_SHORT).show();
                        Log.v(TAG, getString(R.string.pages_shown) + ": " +
                                TopRated.page.getUpperSide() + " & " +
                                TopRated.page.getLowerSide() + " / " + mLastPage);
                    } else {
                        Log.v(TAG, getString(R.string.pages_shown) + ": " +
                                TopRated.page.getUpperSide() + " / " + mLastPage);
                    }
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
        // If "Most Popular" <-> "Top Rated" transition has taken place, then scroll to last seen
        // item in that selection.
        if (isSortingChanged) {
            isSortingChanged = false;
            // Scrolling to late position due to changed sorting order.
            if (mOrder == NetworkUtils.order.MOST_POPULAR)
                mPosterRecyclerView.scrollToPosition(mFirstVisibleItemPosMP);
            else
                mPosterRecyclerView.scrollToPosition(mFirstVisibleItemPosTR);
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

    public static class Popular {
        final static PageStatus page = new PageStatus();
    }

    public static class TopRated {
        final static PageStatus page = new PageStatus();
    }
}

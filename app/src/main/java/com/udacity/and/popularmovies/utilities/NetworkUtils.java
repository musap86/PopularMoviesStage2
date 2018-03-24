package com.udacity.and.popularmovies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Utility class for retrieving data from the server.
 * It's important to provide a string resource file (api_key.xml) which contains the api key
 * for The Movie Database API.
 */
public class NetworkUtils {
    public static final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String BASE_MOVIE_URL = "http://api.themoviedb.org/3/movie";
    private static final String PATH_POPULAR = "popular";
    private static final String PATH_TOP_RATED = "top_rated";
    private static final String TRAILERS_TAG = "videos";
    private static final String REVIEWS_TAG = "reviews";
    private static final String QUERY_API_KEY = "api_key";
    private static final String QUERY_PAGE = "page";
    public static String PARAM_API_KEY;

    /**
     * Builds the URL used to talk to the movie database server using a sorting type.
     *
     * @param sortingType The type of sorting that will be queried for.
     * @return The URL to query from the movieDB server.
     */
    public static URL generateURL(order sortingType, int page) {
        if (sortingType == null)
            sortingType = order.MOST_POPULAR;
        String path = "";
        int START_PAGE = 1;
        page = (page < START_PAGE) ? START_PAGE : page;
        switch (sortingType) {
            case MOST_POPULAR:
                path = PATH_POPULAR;
                break;
            case TOP_RATED:
                path = PATH_TOP_RATED;
                break;
        }
        Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(path)
                .appendQueryParameter(QUERY_API_KEY, PARAM_API_KEY)
                .appendQueryParameter(QUERY_PAGE, String.valueOf(page))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "#1 Query URL " + url);
        return url;
    }

    /**
     * Builds the URL used to talk to the movieDB server using image path for a movie poster.
     *
     * @param imagePath The path for a movie poster.
     * @return The Url in String format to use to fetch the image file.
     */
    public static String generateURL(String imagePath, int posterImageQuality, boolean isBackdropImage) {
        String imageQuality;
        if (isBackdropImage) {
            switch (posterImageQuality) {
                case 1:
                case 2:
                    imageQuality = "w300";
                    break;
                case 3:
                case 4:
                    imageQuality = "w780";
                    break;
                case 5:
                    imageQuality = "w1280";
                    break;
                default:
                    imageQuality = "w780";
            }
        } else {
            switch (posterImageQuality) {
                case 1:
                    imageQuality = "w154";
                    break;
                case 2:
                    imageQuality = "w185";
                    break;
                case 3:
                    imageQuality = "w342";
                    break;
                case 4:
                    imageQuality = "w500";
                    break;
                case 5:
                    imageQuality = "w780";
                    break;
                default:
                    imageQuality = "w342";
            }
        }
        StringBuilder stringBuilder = new StringBuilder(BASE_IMAGE_URL)
                .append(imageQuality)
                .append(imagePath);
        Log.v(TAG, "Image URL " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    public static URL generateURL(long id) {
        Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(id))
                .appendQueryParameter(QUERY_API_KEY, PARAM_API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "#2 Query URL " + url);
        return url;
    }

    public static URL generateURL(long id, Endpoint Endpoint) {
        String path = "";
        switch (Endpoint) {
            case VIDEO:
                path = TRAILERS_TAG;
                break;
            case REVIEW:
                path = REVIEWS_TAG;
                break;
        }
        Uri builtUri = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(id))
                .appendEncodedPath(path)
                .appendQueryParameter(QUERY_API_KEY, PARAM_API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "#3 Query URL " + url);
        return url;
    }

    /**
     * Returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            Log.v(TAG, "Connecting to URL, getting input stream...");
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
            Log.v(TAG, "Disconnected from URL stream.");
        }
    }

    /**
     * Returns if the user is online or not.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (manager != null) {
            netInfo = manager.getActiveNetworkInfo();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public enum order {MOST_POPULAR, TOP_RATED, FAVORITES}

    public enum Endpoint {VIDEO, REVIEW}
}

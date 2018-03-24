package com.udacity.and.popularmovies.utilities;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static final String JSON_VAR_BACKDROP = "backdrop_path";
    public static final String JSON_VAR_OVERVIEW = "overview";
    public static final String JSON_VAR_RELEASE = "release_date";
    public static final String JSON_VAR_RUNTIME = "runtime";
    public static final String JSON_VAR_TITLE = "title";
    public static final String JSON_VAR_VOTE_AVG = "vote_average";
    public static final String JSON_VAR_POSTER = "poster_path";
    public static final String JSON_VAR_ID = "id";
    private static final String JSON_VAR_PAGE = "page";
    private static final String JSON_VAR_PAGES = "total_pages";
    private static final String JSON_VAR_RESULTS = "results";
    private static final String JSON_VAR_AUTHOR = "author";
    private static final String JSON_VAR_CONTENT = "content";
    private static final String JSON_VAR_NAME = "name";
    private static final String JSON_VAR_KEY = "key";
    private static final String CLASS_TAG = JsonUtils.class.getSimpleName();

    /**
     * Populates data for the static variables of the class for listing page (main activity)
     * by extracting the given JSON string.
     */
    public static void extractMovieDataFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            MovieDetails.setPageCount(jsonObject.optInt(JSON_VAR_PAGES));
            JSONArray results = jsonObject.getJSONArray(JSON_VAR_RESULTS);
            int[] ids = new int[results.length()];
            int[] pages = new int[results.length()];
            String[] posterPaths = new String[results.length()];
            String[] movieTitles = new String[results.length()];
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.optJSONObject(i);
                ids[i] = result.optInt(JSON_VAR_ID);
                pages[i] = jsonObject.optInt(JSON_VAR_PAGE);
                posterPaths[i] = result.optString(JSON_VAR_POSTER);
                movieTitles[i] = result.optString(JSON_VAR_TITLE);
            }
            MovieDetails.setIds(ids);
            MovieDetails.setPages(pages);
            MovieDetails.setPosterPaths(posterPaths);
            MovieDetails.setMovieTitles(movieTitles);
        } catch (JSONException e) {
            Log.e(CLASS_TAG, e.getMessage());
        }
    }

    public static void extractReviewsFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray results = jsonObject.getJSONArray(JSON_VAR_RESULTS);
            String[][] reviewsArray = new String[results.length()][2];
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.optJSONObject(i);
                reviewsArray[i][0] = result.optString(JSON_VAR_AUTHOR);
                reviewsArray[i][1] = result.optString(JSON_VAR_CONTENT);
            }
            MovieDetails.setReviewsArray(reviewsArray);
        } catch (JSONException e) {
            Log.e(CLASS_TAG, e.getMessage());
        }
    }

    public static void extractVideosFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray results = jsonObject.getJSONArray(JSON_VAR_RESULTS);
            String[] videoNames = new String[results.length()];
            String[] videoKeys = new String[results.length()];
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.optJSONObject(i);
                videoNames[i] = result.optString(JSON_VAR_NAME);
                videoKeys[i] = result.optString(JSON_VAR_KEY);
            }
            MovieDetails.setVideoNames(videoNames);
            MovieDetails.setVideoKeys(videoKeys);
        } catch (JSONException e) {
            Log.e(CLASS_TAG, e.getMessage());
        }
    }

    public static Map<String, String> getMovieDetails(String json) {
        Map<String, String> results = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            results.put(JSON_VAR_ID, jsonObject.optString(JSON_VAR_ID));
            results.put(JSON_VAR_POSTER, jsonObject.optString(JSON_VAR_POSTER));
            results.put(JSON_VAR_BACKDROP, jsonObject.optString(JSON_VAR_BACKDROP));
            results.put(JSON_VAR_OVERVIEW, jsonObject.optString(JSON_VAR_OVERVIEW));
            results.put(JSON_VAR_RELEASE, jsonObject.optString(JSON_VAR_RELEASE));
            results.put(JSON_VAR_RUNTIME, jsonObject.optString(JSON_VAR_RUNTIME));
            results.put(JSON_VAR_TITLE, jsonObject.optString(JSON_VAR_TITLE));
            results.put(JSON_VAR_VOTE_AVG, jsonObject.optString(JSON_VAR_VOTE_AVG));
        } catch (JSONException e) {
            Log.e(CLASS_TAG, e.getMessage());
        }
        return results;
    }
}

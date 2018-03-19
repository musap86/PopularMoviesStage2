package com.udacity.and.popularmovies.data;

import android.database.Cursor;

import com.udacity.and.popularmovies.utilities.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class MovieDetails {
    private static int[] sIds;
    private static String[] sPosterPaths;
    private static String[] sVideoNames;
    private static String[] sVideoKeys;
    private static String[][] sReviewsArray;
    private static int sPageCount;
    private static int sCurrentPage;

    public static void setIds(int[] ids) {
        sIds = ids;
    }

    public static void setPosterPaths(String[] posterPaths) {
        sPosterPaths = posterPaths;
    }

    public static void setVideoNames(String[] videoNames) {
        sVideoNames = videoNames;
    }

    public static void setVideoKeys(String[] videoKeys) {
        sVideoKeys = videoKeys;
    }

    public static void setReviewsArray(String[][] reviewsArray) {
        sReviewsArray = reviewsArray;
    }

    public static int getId(int index) {
        return sIds[index];
    }

    public static String getImagePath(int index) {
        return sPosterPaths[index];
    }

    public static String getReviewAuthor(int index) {
        return sReviewsArray[index][0];
    }

    public static String getTrailerSource(int index) {
        if (sVideoKeys.length <= index)
            return null;
        else
            return sVideoKeys[index];
    }

    public static String getTrailerName(int index) {
        return sVideoNames[index];
    }

    public static String getReviewContent(int index) {
        return sReviewsArray[index][1];
    }

    public static int getPageCount() {
        return sPageCount;
    }

    public static void setPageCount(int movieCount) {
        sPageCount = movieCount;
    }

    public static int getCurrentPage() {
        return sCurrentPage;
    }

    public static void setCurrentPage(int currentPage) {
        MovieDetails.sCurrentPage = currentPage;
    }

    public static int getMoviesCountInPage() {
        if (sIds == null)
            return 0;
        else
            return sIds.length;
    }

    public static int getReviewsCount() {
        if (sReviewsArray == null)
            return 0;
        else
            return sReviewsArray.length;
    }

    public static int getTrailersCount() {
        if (sVideoNames == null)
            return 0;
        else
            return sVideoNames.length;
    }

    public static Map<String, String> getMovieDetails(Cursor cursor) {
        Map<String, String> results = new HashMap<>();
        while (cursor.moveToNext()) {
            results.put(JsonUtils.JSON_VAR_ID,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID)));
            results.put(JsonUtils.JSON_VAR_TITLE,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE)));
            results.put(JsonUtils.JSON_VAR_POSTER,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH)));
            results.put(JsonUtils.JSON_VAR_OVERVIEW,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_SYNOPSIS)));
            results.put(JsonUtils.JSON_VAR_VOTE_AVG,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_USER_RATING)));
            results.put(JsonUtils.JSON_VAR_RELEASE,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE)));
            results.put(JsonUtils.JSON_VAR_BACKDROP,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_BACKDROP_PATH)));
            results.put(JsonUtils.JSON_VAR_RUNTIME,
                    cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RUNTIME)));
        }
        cursor.close();
        return results;
    }
}

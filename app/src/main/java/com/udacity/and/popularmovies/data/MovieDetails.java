package com.udacity.and.popularmovies.data;

import android.database.Cursor;

import com.udacity.and.popularmovies.utilities.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for details that is extracted from a JSON response fetched from server
 */
public class MovieDetails {
    private static int[] sIds;
    private static int[] sPages;
    private static String[] sPosterPaths;
    private static String[] sMovieTitles;
    private static String[] sVideoNames;
    private static String[] sVideoKeys;
    private static String[][] sReviewsArray;
    private static int sPageCount;

    public static void setIds(int[] ids) {
        sIds = ids;
    }

    public static int getId(int index) {
        try {
            return sIds[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setPages(int[] pages) {
        sPages = pages;
    }

    public static int getPage(int index) {
        return sPages[index];
    }

    public static void setPosterPaths(String[] posterPaths) {
        sPosterPaths = posterPaths;
    }

    public static String getImagePath(int index) {
        try {
            return sPosterPaths[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getMovieTitle(int index) {
        return sMovieTitles[index];
    }

    public static void setMovieTitles(String[] movieTitles) {
        sMovieTitles = movieTitles;
    }

    public static void setVideoNames(String[] videoNames) {
        sVideoNames = videoNames;
    }

    public static String getTrailerName(int index) {
        try {
            return sVideoNames[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setVideoKeys(String[] videoKeys) {
        sVideoKeys = videoKeys;
    }

    public static String getTrailerSource(int index) {
        try {
            return sVideoKeys[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setReviewsArray(String[][] reviewsArray) {
        sReviewsArray = reviewsArray;
    }

    public static String getReviewAuthor(int index) {
        try {
            return sReviewsArray[index][0];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getReviewContent(int index) {
        try {
            return sReviewsArray[index][1];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getPageCount() {
        return sPageCount;
    }

    public static void setPageCount(int movieCount) {
        sPageCount = movieCount;
    }

    public static int getMovieCountInView() {
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
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_MOVIE_ID)));
            results.put(JsonUtils.JSON_VAR_TITLE,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_TITLE)));
            results.put(JsonUtils.JSON_VAR_POSTER,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_POSTER_PATH)));
            results.put(JsonUtils.JSON_VAR_OVERVIEW,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_SYNOPSIS)));
            results.put(JsonUtils.JSON_VAR_VOTE_AVG,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_USER_RATING)));
            results.put(JsonUtils.JSON_VAR_RELEASE,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_RELEASE_DATE)));
            results.put(JsonUtils.JSON_VAR_BACKDROP,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_BACKDROP_PATH)));
            results.put(JsonUtils.JSON_VAR_RUNTIME,
                    cursor.getString(cursor.getColumnIndex(DataContract.DataEntry.COLUMN_RUNTIME)));
        }
        cursor.close();
        return results;
    }
}

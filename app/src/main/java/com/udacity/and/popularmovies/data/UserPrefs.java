package com.udacity.and.popularmovies.data;

import android.content.Context;
import android.util.DisplayMetrics;

import com.udacity.and.popularmovies.utilities.NetworkUtils;

/**
 * Data class for the user preferences to view movie posters.
 * Posters of most popular, top rated or favorite movies are shown
 * in preferred image quality.
 */
public class UserPrefs {
    private static NetworkUtils.SortOrder sSortingOrder;
    private static int sImageQuality;

    /**
     * Returns the opted type of sorting order for the poster images of related movies
     * in the main activity.
     */
    public static NetworkUtils.SortOrder getSortOrder() {
        if (sSortingOrder == null) {
            sSortingOrder = NetworkUtils.SortOrder.MOST_POPULAR;
        }
        return sSortingOrder;
    }

    /**
     * Changes the opted type of sorting order for the poster images of related movies
     * in the main activity.
     */
    public static void setSortOrder(NetworkUtils.SortOrder sortingOrder) {
        sSortingOrder = sortingOrder;
    }

    public static int setSortOrder(Context context) {
        if (sSortingOrder == null) {
            if (NetworkUtils.isOnline(context)) {
                sSortingOrder = NetworkUtils.SortOrder.MOST_POPULAR;
                return 0;
            } else {
                sSortingOrder = NetworkUtils.SortOrder.FAVORITES;
                return 2;
            }
        } else {
            return sSortingOrder.ordinal();
        }
    }

    /**
     * Returns the opted quality of poster images for downloading.
     */
    public static int getImageQuality() {
        return sImageQuality;
    }

    /**
     * Sets the opted quality of poster images for downloading.
     */
    public static void setImageQuality(int quality) {
        sImageQuality = quality;
    }

    /**
     * Returns the count for grid columns to show movie posters. In order to achieve
     * a nice user xp, column count is calculated in accordance with the screen
     * width in pixels and image quality of each movie poster.
     */
    public static int getGridColumnCount(DisplayMetrics metrics) {
        int imageQuality;
        switch (sImageQuality) {
            case 1:
                imageQuality = 154;
                break;
            case 2:
                imageQuality = 185;
                break;
            case 3:
                imageQuality = 342;
                break;
            case 4:
                imageQuality = 500;
                break;
            case 5:
                imageQuality = 780;
                break;
            default:
                imageQuality = 342;
        }
        int columnCount = metrics.widthPixels / (imageQuality * 2);
        columnCount = columnCount < 2 ? 2 : columnCount;
        columnCount = columnCount > 5 ? 5 : columnCount;
        return columnCount;
    }
}

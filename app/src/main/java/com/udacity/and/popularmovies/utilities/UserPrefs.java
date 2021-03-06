package com.udacity.and.popularmovies.utilities;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Cache for user preferences to view movie posters. Posters of most popular, top rated or
 * favorite movies are shown in preferred image quality.
 */
public class UserPrefs {
    private static NetworkUtils.order sSortingOrder;
    private static int sImageQuality;

    /**
     * Returns the opted type of sorting order for the poster images of related movies
     * in the main activity.
     */
    public static NetworkUtils.order getSortOrder(Context context) {
        if (sSortingOrder == null) {
            if (NetworkUtils.isOnline(context)) {
                sSortingOrder = NetworkUtils.order.MOST_POPULAR;
            } else {
                sSortingOrder = NetworkUtils.order.FAVORITES;
            }
        }
        return sSortingOrder;
    }

    public static int getSortOrderIndex(Context context) {
        if (sSortingOrder == null) {
            if (NetworkUtils.isOnline(context)) {
                sSortingOrder = NetworkUtils.order.MOST_POPULAR;
            } else {
                sSortingOrder = NetworkUtils.order.FAVORITES;
            }
        }
        return sSortingOrder.ordinal();
    }

    /**
     * Changes the opted type of sorting order for the poster images of related movies
     * in the main activity.
     */
    public static void setSortOrder(NetworkUtils.order sortingOrder) {
        sSortingOrder = sortingOrder;
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
     * Returns the count for grid columns to show movie posters.
     * Column count is calculated in accordance with the screen
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

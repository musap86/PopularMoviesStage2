package com.udacity.and.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesContract {
    static final String AUTHORITY = "com.udacity.and.popularmovies";
    static final String PATH_FAVORITES = "favorites";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class FavoritesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "posterPath";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_BACKDROP_PATH = "backdropPath";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        static final String TABLE_NAME = "favorites";
    }
}
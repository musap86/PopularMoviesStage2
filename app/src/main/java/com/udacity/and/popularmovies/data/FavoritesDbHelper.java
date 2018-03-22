package com.udacity.and.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                DataContract.DataEntry.TABLE_NAME + "(" +
                DataContract.DataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataContract.DataEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                DataContract.DataEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_USER_RATING + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                DataContract.DataEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.DataEntry.TABLE_NAME);
        onCreate(db);
    }
}

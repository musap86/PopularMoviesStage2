package com.udacity.and.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class FavoritesContentProvider extends ContentProvider {

    private static final int FAVORITES = 100;
    private static final int FAVORITES_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDbHelper mFavoritesDbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.PATH_FAVORITES + "/#", FAVORITES_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavoritesDbHelper = new FavoritesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case FAVORITES:
                long id = db.insert(DataContract.DataEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(DataContract.DataEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mFavoritesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (match) {
            case FAVORITES:
                retCursor = db.query(DataContract.DataEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = DataContract.DataEntry._ID + "=?";
                String[] mSelectionArgs = new String[]{id};
                retCursor = db.query(DataContract.DataEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int updatedCount;
        switch (match) {
            case FAVORITES:
                updatedCount = db.update(DataContract.DataEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = DataContract.DataEntry._ID + "=?";
                String[] mSelectionArgs = new String[]{id};
                updatedCount = db.update(DataContract.DataEntry.TABLE_NAME,
                        values,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updatedCount != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updatedCount;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deletedCount;
        switch (match) {
            case FAVORITES:
                deletedCount = db.delete(DataContract.DataEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = DataContract.DataEntry._ID + "=?";
                String[] mSelectionArgs = new String[]{id};
                deletedCount = db.delete(DataContract.DataEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deletedCount != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedCount;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(DataContract.DataEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITES:
                return "vnd.android.cursor.dir" + "/" +
                        DataContract.AUTHORITY + "/" +
                        DataContract.PATH_FAVORITES;
            case FAVORITES_WITH_ID:
                return "vnd.android.cursor.item" + "/" +
                        DataContract.AUTHORITY + "/" +
                        DataContract.PATH_FAVORITES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}

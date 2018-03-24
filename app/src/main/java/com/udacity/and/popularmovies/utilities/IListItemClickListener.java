package com.udacity.and.popularmovies.utilities;

import android.view.View;

/**
 * List item click listener interface for movie posters in MainActivity
 */
public interface IListItemClickListener {
    void onListItemClick(int clickedItemIndex, View itemView);
}

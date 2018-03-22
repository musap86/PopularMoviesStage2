package com.udacity.and.popularmovies.adapters;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.and.popularmovies.IListItemClickListener;
import com.udacity.and.popularmovies.R;
import com.udacity.and.popularmovies.data.DataContract;
import com.udacity.and.popularmovies.data.MovieDetails;
import com.udacity.and.popularmovies.data.UserPrefs;
import com.udacity.and.popularmovies.utilities.NetworkUtils;

/**
 * Adapter for movie poster list in MainActivity
 */
public class PostersAdapter extends RecyclerView.Adapter<PostersAdapter.MoviePostersViewHolder> {
    private final IListItemClickListener mOnClickListener;
    private int mMaxPosterWidth;
    private Cursor mCursor;

    /**
     * Initiates an onClickListener on the movie poster list and sets maximum width for the posters
     *
     * @param itemClickListener an activity class which implements IListItemClickListener interface
     * @param posterWidth       width in pixels
     */
    public PostersAdapter(IListItemClickListener itemClickListener, int posterWidth) {
        mOnClickListener = itemClickListener;
        mMaxPosterWidth = posterWidth;
    }

    /**
     * Sets maximum width for each poster in movie poster list in MainActivity
     *
     * @param width width in pixels
     */
    public void setMaxPosterWidth(int width) {
        mMaxPosterWidth = width;
        notifyDataSetChanged();
    }

    /**
     * Sets cursor for the adapter to read data from favorites database
     *
     * @param cursor data cursor from favorites database
     */
    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoviePostersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_poster_item, parent, false);
        return new MoviePostersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviePostersViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            // poster list will be populated with favorite movies
            return mCursor.getCount();
        else
            // poster list will be populated with either most popular or top rated movies
            return MovieDetails.getMovieCountInView();
    }

    /**
     * Cache of the child image view for a movie poster list item.
     */
    class MoviePostersViewHolder
            extends RecyclerView.ViewHolder
            implements OnClickListener {

        private final ImageView mMoviePosterImageView;

        /**
         * Initiates an image view from movie poster item layout file
         *
         * @param itemView an inflated view with a movie poster item layout file
         */
        private MoviePostersViewHolder(View itemView) {
            super(itemView);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        /**
         * Binds a corresponding image for each movie unless no movie poster image is provided.
         * Each item will be provided with a movie id as a tag and an image to show.
         *
         * @param pos position of the item in the layout
         */
        void bind(int pos) {
            String imageUrlString = null;
            if (mCursor == null) {
                //item is either a most popular or a top rated movie
                itemView.setTag(MovieDetails.getId(pos));
                /* For a few of the movies, the server returns a json response in which there is
                a "null" statement instead of a path for an image file.*/
                if (MovieDetails.getImagePath(pos).equals("null")) {
                    mMoviePosterImageView.setImageResource(R.mipmap.no_poster_image);
                } else {
                    imageUrlString = NetworkUtils
                            .generateURL(MovieDetails.getImagePath(pos), UserPrefs.getImageQuality(), false);
                }
            } else {
                //item is a favorite movie
                if (mCursor.moveToPosition(pos)) {
                    int id = mCursor.getInt(mCursor.getColumnIndex(
                            DataContract.DataEntry.COLUMN_MOVIE_ID));
                    itemView.setTag(id);
                    String posterPath = mCursor.getString(mCursor.getColumnIndex(
                            DataContract.DataEntry.COLUMN_POSTER_PATH));
                    imageUrlString = NetworkUtils
                            .generateURL(posterPath, UserPrefs.getImageQuality(), false);
                }
            }

            if (imageUrlString != null) {
                Picasso.with(itemView.getContext())
                        .load(imageUrlString)
                        .placeholder(R.mipmap.poster_placeholder)
                        .error(R.mipmap.no_poster_image)
                        .resize(mMaxPosterWidth, mMaxPosterWidth / 2 * 3)
                        .into(mMoviePosterImageView);
            } else {
                mMoviePosterImageView.setImageResource(R.mipmap.no_poster_image);
            }

            mMoviePosterImageView.setMaxWidth(mMaxPosterWidth);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition, itemView);
        }
    }
}

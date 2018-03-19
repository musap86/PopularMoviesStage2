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
import com.udacity.and.popularmovies.data.FavoritesContract;
import com.udacity.and.popularmovies.data.MovieDetails;
import com.udacity.and.popularmovies.data.UserPrefs;
import com.udacity.and.popularmovies.utilities.NetworkUtils;

public class PostersAdapter extends RecyclerView.Adapter<PostersAdapter.MoviePostersViewHolder> {
    private final IListItemClickListener mOnClickListener;
    private int mMaxPosterWidth;
    private Cursor mCursor;

    public PostersAdapter(IListItemClickListener listener, int posterWidth) {
        mOnClickListener = listener;
        mMaxPosterWidth = posterWidth;
    }

    public void setMaxPosterWidth(int width) {
        mMaxPosterWidth = width;
        notifyDataSetChanged();
    }

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
            return mCursor.getCount();
        else
            return MovieDetails.getMoviesCountInPage();
    }

    /**
     * Cache of the child image view for a movie poster list item.
     */
    class MoviePostersViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        private final ImageView moviePosterImageView;

        public MoviePostersViewHolder(View itemView) {
            super(itemView);
            moviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        /**
         * Shows corresponding poster image for each movie in a recycler view unless no movie
         * poster image is provided.
         * For a few of the movies the server returns a json response in which there is a
         * "null" statement instead of a path for an image file.
         */
        void bind(int pos) {
            String imageUrlString = null;
            if (mCursor == null) {
                itemView.setTag(MovieDetails.getId(pos));
                if (MovieDetails.getImagePath(pos).equals("null")) {
                    moviePosterImageView.setImageResource(R.mipmap.no_poster_image);
                } else {
                    imageUrlString = NetworkUtils
                            .generateURL(MovieDetails.getImagePath(pos), UserPrefs.getImageQuality(), false);
                }
            } else {
                if (mCursor.moveToPosition(pos)) {
                    int id = mCursor.getInt(mCursor.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
                    itemView.setTag(id);
                    String posterPath = mCursor.getString(mCursor.getColumnIndex(
                            FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH));
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
                        .into(moviePosterImageView);
            } else {
                moviePosterImageView.setImageResource(R.mipmap.no_poster_image);
            }

            moviePosterImageView.setMaxWidth(mMaxPosterWidth);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition, itemView);
        }
    }
}

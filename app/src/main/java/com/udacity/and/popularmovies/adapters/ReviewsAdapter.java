package com.udacity.and.popularmovies.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.and.popularmovies.R;
import com.udacity.and.popularmovies.utilities.MovieDetails;

/**
 * Adapter for review list in DetailsActivity
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {
    @NonNull
    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.review_item, parent, false);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return MovieDetails.getReviewsCount();
    }

    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView mAuthor;
        private final TextView mContent;

        ReviewsAdapterViewHolder(View itemView) {
            super(itemView);
            mAuthor = itemView.findViewById(R.id.tv_review_author);
            mContent = itemView.findViewById(R.id.tv_review_content);
        }

        void bind(int pos) {
            mAuthor.setText(MovieDetails.getReviewAuthor(pos));
            mContent.setText(MovieDetails.getReviewContent(pos));
        }
    }
}

package com.udacity.and.popularmovies.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.and.popularmovies.R;
import com.udacity.and.popularmovies.utilities.IListItemClickListener;
import com.udacity.and.popularmovies.utilities.MovieDetails;

/**
 * Adapter for trailer video list in DetailsActivity
 */
public class TrailersAdapter
        extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {
    private final IListItemClickListener mOnClickListener;

    public TrailersAdapter(IListItemClickListener listener) {
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.trailer_item, parent, false);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return MovieDetails.getTrailersCount();
    }

    class TrailersAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView mTrailerLabel;
        private final ImageView mTrailerIcon;

        TrailersAdapterViewHolder(View itemView) {
            super(itemView);
            mTrailerLabel = itemView.findViewById(R.id.tv_trailer_label);
            mTrailerIcon = itemView.findViewById(R.id.iv_video_icon);
            itemView.setOnClickListener(this);
        }

        void bind(int pos) {
            mTrailerLabel.setText(MovieDetails.getTrailerName(pos));
            String youtubeId = MovieDetails.getTrailerSource(pos);
            Picasso.with(itemView.getContext())
                    .load("https://img.youtube.com/vi/" + youtubeId + "/default.jpg")
                    .into(mTrailerIcon);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition, itemView);
        }
    }
}

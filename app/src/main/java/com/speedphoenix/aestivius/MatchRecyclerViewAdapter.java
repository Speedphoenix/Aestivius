package com.speedphoenix.aestivius;

import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.speedphoenix.aestivius.MatchFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import android.os.Handler;
/**
 * {@link RecyclerView.Adapter} that can display a {@link Match} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MatchRecyclerViewAdapter extends RecyclerView.Adapter<MatchRecyclerViewAdapter.ViewHolder> {

    private List<Match> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Handler handler;

    public MatchRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
        handler = new Handler();
        refreshList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_match, parent, false);
        return new ViewHolder(view);
    }

    public void refreshList() {
        final MatchRecyclerViewAdapter adapter = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mValues = Arrays.asList(MainActivity.getDao().getAll());
                Collections.reverse(mValues);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);

        holder.mLoserView.setText(holder.mItem.getLoser());
        holder.mDateView.setText(holder.mItem.getDate().toString());
        holder.mLocationView.setText(holder.mItem.getLocation());
        holder.mScoreView.setText(holder.mItem.getFinalScore());
        holder.mWinnerView.setText(holder.mItem.getWinner());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mWinnerView;
        public final TextView mLoserView;
        public final TextView mDateView;
        public final TextView mLocationView;
        public final TextView mScoreView;

        public Match mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWinnerView = (TextView) view.findViewById(R.id.winner);
            mLoserView = (TextView) view.findViewById(R.id.loser);
            mDateView = (TextView) view.findViewById(R.id.date);
            mLocationView = (TextView) view.findViewById(R.id.location);
            mScoreView = (TextView) view.findViewById(R.id.score);
        }

        @Override
        public String toString() {
            return super.toString() + " " + mDateView.getText() + ": '" + mWinnerView.getText() + "' vs '" + mLoserView.getText() + "': " + mScoreView.getText();
        }
    }
}

package com.speedphoenix.aestivius;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.speedphoenix.aestivius.MatchFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Match} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MatchRecyclerViewAdapter extends RecyclerView.Adapter<MatchRecyclerViewAdapter.ViewHolder> {

    private List<Match> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Handler handler;

    public static final int MATCH_VIEW = 0;
    public static final int BUTTON_VIEW = 1;

    private boolean hasLoadedMore = false;
    private Context appContext;

    public MatchRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context appContext) {
        mListener = listener;
        mValues = new ArrayList<>();
        handler = new Handler();
        refreshLocalList();
        this.appContext = appContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            default:
            case MATCH_VIEW:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_match, parent, false);
                return new MatchViewHolder(view);

            case BUTTON_VIEW:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_loadmore, parent, false);
                return new ButtonViewHolder(view);
        }
    }

    public void refreshLocalList() {
        final MatchRecyclerViewAdapter adapter = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mValues = MainActivity.getDbHelper().getAllMatches();
                Collections.sort(mValues);
                Collections.reverse(mValues);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        hasLoadedMore = false;
    }

    public void refreshExternalList() {
        final MatchRecyclerViewAdapter adapter = this;

        RequestQueue requestQueue = Volley.newRequestQueue(appContext);

        String url = MainActivity.EXTERNAL_DB_URL + MainActivity.getPhoneID();

        JSONArray array = new JSONArray();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, array,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mValues = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject nextMatch = response.getJSONObject(i);
                                mValues.add(new Match(new Date(nextMatch.getLong("date")),
                                        nextMatch.getString("location"),
                                        nextMatch.getString("winner"),
                                        nextMatch.getString("loser"),
                                        nextMatch.getString("score"),
                                        nextMatch.getInt("localid")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Collections.sort(mValues);
                            Collections.reverse(mValues);
                            hasLoadedMore = true;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println(error.toString());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public int getItemViewType(int position) {
        if (hasLoadedMore || position < mValues.size())
            return MATCH_VIEW;
        else
            return BUTTON_VIEW;
    }

        @Override
    public void onBindViewHolder(final ViewHolder anyholder, int position) {

        if (anyholder instanceof MatchViewHolder) {
            final MatchViewHolder holder = (MatchViewHolder) anyholder;
            holder.mItem = mValues.get(position);

            holder.mLoserView.setText(holder.mItem.getLoser());
            holder.mDateView.setText(holder.mItem.getDate().toString());
            holder.mLocationView.setText(holder.mItem.getLocation());
            holder.mScoreView.setText(holder.mItem.getFinalScore());
            holder.mWinnerView.setText(holder.mItem.getWinner());

            // this is currently not used, but might be one day
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
        else if (anyholder instanceof ButtonViewHolder) {
            ButtonViewHolder holder = (ButtonViewHolder) anyholder;
            holder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refreshExternalList();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + (hasLoadedMore ? 0 : 1);
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    public class MatchViewHolder extends ViewHolder {
        public final TextView mWinnerView;
        public final TextView mLoserView;
        public final TextView mDateView;
        public final TextView mLocationView;
        public final TextView mScoreView;

        public Match mItem;

        public MatchViewHolder(View view) {
            super(view);
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

    public class ButtonViewHolder extends ViewHolder {
        public final Button mButton;

        public ButtonViewHolder(View view) {
            super(view);
            mButton = view.findViewById(R.id.button_load);
        }
    }
}

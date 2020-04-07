package com.speedphoenix.aestivius;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.speedphoenix.aestivius.Match.MatchEntry;
import com.speedphoenix.aestivius.MatchFragment.OnListFragmentInteractionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Match} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MatchRecyclerViewAdapter extends RecyclerView.Adapter<MatchRecyclerViewAdapter.ViewHolder> {

    private List<Match> mValues;
    private List<Bitmap> toFree;
    private Set<ImageView> usedImageViews = new HashSet<>();
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

    // this is to be able to recycle every bitmap later
    void rememberBitmaps() {
        toFree = new ArrayList<>();
        for (int i = 0; i < mValues.size(); i++) {
            Bitmap inter = mValues.get(i).getBitmap();
            if (inter != null)
                toFree.add(inter);
            mValues.get(i).setBitmap(null);
        }
    }

    void recycleBitmaps() {
        for (ImageView el : usedImageViews) {
            el.setImageDrawable(null);
        }
        usedImageViews = new HashSet<>();

        for (int i = 0; i < toFree.size(); i++) {
            toFree.get(i).recycle();
            toFree.set(i, null);
        }
    }


    public void refreshLocalList() {
        final MatchRecyclerViewAdapter adapter = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                rememberBitmaps();
                mValues = MainActivity.getDbHelper().getAllMatches();
                Collections.sort(mValues);
                Collections.reverse(mValues);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        recycleBitmaps();
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
                        rememberBitmaps();
                        mValues = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject nextMatch = response.getJSONObject(i);
                                mValues.add(new Match(new Date(nextMatch.getLong(MatchEntry.COLUMN_NAME_DATE)),
                                        nextMatch.getString(MatchEntry.COLUMN_NAME_LOCATION),
                                        nextMatch.getString(MatchEntry.COLUMN_NAME_WINNER),
                                        nextMatch.getString(MatchEntry.COLUMN_NAME_LOSER),
                                        nextMatch.getString(MatchEntry.COLUMN_NAME_SCORE),
                                        nextMatch.getString(MatchEntry.COLUMN_NAME_PICTURE),
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
                                    recycleBitmaps();
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

            if (holder.mItem.getPicturePath() != null) {
                if (holder.mItem.getBitmap() == null) {
                    int targetW = (int) appContext.getResources().getDimension(R.dimen.picture_match_creation_width);
                    int targetH = (int) appContext.getResources().getDimension(R.dimen.picture_match_creation_height);
                    holder.mItem.setBitmap(SomeUtils.getPic(holder.mItem.getPicturePath(), targetW, targetH));
                }
                holder.mImageView.setImageBitmap(holder.mItem.getBitmap());
                usedImageViews.add(holder.mImageView);
            }

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
        public final ImageView mImageView;

        public Match mItem;

        public MatchViewHolder(View view) {
            super(view);
            mWinnerView = (TextView) view.findViewById(R.id.winner);
            mLoserView = (TextView) view.findViewById(R.id.loser);
            mDateView = (TextView) view.findViewById(R.id.date);
            mLocationView = (TextView) view.findViewById(R.id.location);
            mScoreView = (TextView) view.findViewById(R.id.score);
            mImageView = (ImageView) view.findViewById(R.id.matchpicture);
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

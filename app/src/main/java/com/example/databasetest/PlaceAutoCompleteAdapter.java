package com.example.databasetest;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlaceAutoCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {
    private static final String TAG = "PlaceAutoCompleteAdapter";

    private List<AutocompletePrediction> mResultList;
    private PlacesClient mPlacesClient;

    public PlaceAutoCompleteAdapter(@NonNull Context context, PlacesClient placesClient, LatLngBounds latLngBounds) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        mPlacesClient = placesClient;
        Log.d(TAG, "PlaceAutoCompleteAdapter initialized");
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(TAG, "Filtering results");
                FilterResults results = new FilterResults();

                if (constraint != null && constraint.length() > 0) {
                    FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                            .setQuery(constraint.toString())
                            .build();

                    Task<FindAutocompletePredictionsResponse> task = mPlacesClient.findAutocompletePredictions(predictionsRequest);

                    try {
                        // wait for the task to complete synchronously
                        Tasks.await(task);

                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse response = task.getResult();
                            mResultList = response.getAutocompletePredictions();
                            results.values = mResultList;
                            results.count = mResultList.size();
                        } else {
                            Log.e(TAG, "Error getting autocomplete predictions: " + task.getException());
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Constraint is null or empty");
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    Log.d(TAG, "Results published, notifying data set changed");
                    notifyDataSetChanged();
                } else {
                    Log.d(TAG, "No results found, dataset invalidated");
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction) {
                    Log.d(TAG, "Converting result to string");
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    Log.d(TAG, "Unknown result, calling super conversion");
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    @Override
    public int getCount() {
        int count = mResultList.size();
        Log.d(TAG, "Getting count: " + count);
        return count;
    }

    @Nullable
    @Override
    public AutocompletePrediction getItem(int position) {
        Log.d(TAG, "Getting item at position: " + position);
        return mResultList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "Getting view for position: " + position);
        View row = super.getView(position, convertView, parent);

        AutocompletePrediction item = getItem(position);

        android.widget.TextView textView1 = row.findViewById(android.R.id.text1);
        android.widget.TextView textView2 = row.findViewById(android.R.id.text2);

        if (item != null) {
            Log.d(TAG, "Updating text views with prediction data");
            textView1.setText(item.getPrimaryText(null));
            textView2.setText(item.getSecondaryText(null));
        } else {
            Log.e(TAG, "Prediction item is null");
        }

        return row;
    }
}
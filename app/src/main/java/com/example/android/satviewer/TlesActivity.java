package com.example.android.satviewer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.satviewer.utilities.CelesTrakTxtUtils;
import com.example.android.satviewer.utilities.NetworkUtils;
import com.example.android.satviewer.utilities.SatelliteUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;

import uk.me.g4dpz.satellite.TLE;

public class TlesActivity extends AppCompatActivity
    implements TlesAdapter.TlesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<TLE[]>{

    // Error message holder
    private TextView mErrorMessageDisplay;

    // The recycler view to display the satellites
    private RecyclerView mRecyclerView;

    // Adapter for the recycler View
    private TlesAdapter mTlesAdapter;

    // Progress bar till the data loads up
    private ProgressBar mLoadingIndicator;

    final static int SATELLITES_LOADER_ID = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tles);

        // Load appropriate views
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_sat_list);


        // Vertical linear layout for the recycler view
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        // Set to be of fixed size
        mRecyclerView.setHasFixedSize(true);

        // An adapter for the recycler view
        mTlesAdapter = new TlesAdapter(this);
        mRecyclerView.setAdapter(mTlesAdapter);

        // The callback
        LoaderManager.LoaderCallbacks<TLE[]> callback = TlesActivity.this;

        // The bundle that holds nothing
        Bundle bundleForLoader = null;

        // Initialize the background task loader to get data and show it
        getSupportLoaderManager().initLoader(SATELLITES_LOADER_ID, bundleForLoader, callback);
    }

    /**
     * Show the recycler view and hide the error
     */
    private void showSatellites(){
        // Hide the error
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Show the recycler view
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the recycler view and show the error message
     */
    private void showErrorMessage(){
        // Hide the recycler view
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Show the error message
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Carry out appropriate action on click
     * TODO: Open a new Intent with the arrows
     * @param selectedTle The satellite that was clicked upon
     */
    @Override
    public void onClick(TLE selectedTle) {
        Context context = this;
        Class destinitionClass = SatelliteFinderActivity.class;
        Intent intentToStartListActivity = new Intent(context, destinitionClass);

        String gsonString = new Gson().toJson(selectedTle);
        intentToStartListActivity.putExtra(SatelliteUtils.SATELLITE, gsonString);
        startActivity(intentToStartListActivity);

    }

    /**
     * Returns the loader that would be running in background
     * @param id id of the loader
     * @param args The extra args bundle passed to this loader
     * @return List of satllites
     */
    @Override
    public Loader<TLE[]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<TLE[]>(this) {

            TLE[] mSatelliteData;

            /**
             * Before running the background, check whether we have the data already
             * If loading is required, make the loading indicator on
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // In case we already have the results, we don't make network calls
                if(mSatelliteData != null) {

                    // Jump to deliver results
                    deliverResult(mSatelliteData);
                }
                else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            /**
             * Make network calls to get the required data
             * @return array of satellites
             */
            @Override
            public TLE[] loadInBackground() {

                // Get the satellite type from calling intent
                Intent thisIntent = getIntent();
                SatelliteUtils.SatelliteType satelliteType = null;

                // Consider the outside chance this being empty
                if(thisIntent.hasExtra(SatelliteUtils.TYPE)){
                    satelliteType = (SatelliteUtils.SatelliteType)
                            thisIntent.getSerializableExtra(SatelliteUtils.TYPE);
                }

                // Fetch the satellites data from appropriate URL
                if(satelliteType != null) {
                    URL url = NetworkUtils.buildUrl(satelliteType);

                    try {
                        String text = NetworkUtils.getResponseFromHttpUrl(url);
                        TLE[] tles = CelesTrakTxtUtils.getTlesFromTxt(text);
                        return tles;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                else{
                    return null;
                }
            }

            /**
             * Save the results to a local reference variable
             * @param data array of satellites
             */
            @Override
            public void deliverResult(TLE[] data) {
                mSatelliteData = data;
                super.deliverResult(data);
            }
        };
    }

    /**
     * Lists the steps to be done when the loader delivers results
     * Setting the loading indicator to invisible, setting the data of the recycler view
     * and showing the view
     * @param loader The loader that would be finished
     * @param data the array of satellites
     */
    @Override
    public void onLoadFinished(Loader<TLE[]> loader, TLE[] data) {
        // Stop the loader
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        // Show the recycler view
        mTlesAdapter.setSatelliteData(data);

        // Show error in case no data is returned
        if(data == null){
            showErrorMessage();
        }
        else{
            showSatellites();
        }
    }

    @Override
    public void onLoaderReset(Loader<TLE[]> loader) {

    }
}

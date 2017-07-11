package com.example.android.satviewer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.android.satviewer.utilities.SatelliteUtils;

public class SatelliteTypesActivity extends AppCompatActivity
    implements SatelliteTypesAdapter.SatelliteTypesAdapterOnClickHandler{
    private RecyclerView mRecyclerView;
    private SatelliteTypesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite_types);

        mRecyclerView = (RecyclerView) findViewById(R.id.sat_types_rv);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SatelliteTypesAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        String[] typesDataset = SatelliteUtils.getAllSatelliteTypeStrings();

        mAdapter.setTypesData(typesDataset);

    }

    /**
     * Handles the event of clicking the listItem
     * @param s
     */
    @Override
    public void onListItemClick(String s) {
        Context context = this;
        Class destinitionClass = TlesActivity.class;
        Intent intentToStartListActivity = new Intent(context, destinitionClass);

        intentToStartListActivity.putExtra(SatelliteUtils.TYPE, SatelliteUtils.getType(s));
        startActivity(intentToStartListActivity);
    }
}

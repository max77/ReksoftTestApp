package com.example.mkomarovskiy.reksofttestapp.ui.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;

public class MapActivity extends FragmentActivity {

    private static final String EXTRA_LOCATION_ID = "locid";
    private static final String EXTRA_LAST_ADDED_LOCATION_ID = "addedlocid";
    private long mLastAddedLocationId = -1;

    public static void show(Activity activity, long locationId, int requestCode) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra(EXTRA_LOCATION_ID, locationId);

        activity.startActivityForResult(intent, requestCode);
    }

    public static long extractLastAddedLocationId(Intent data) {
        return data.getLongExtra(EXTRA_LAST_ADDED_LOCATION_ID, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            long locId = getIntent().getLongExtra(EXTRA_LOCATION_ID, -1);

            LocationsOnMapFragment fragment = new LocationsOnMapFragment();

            fragment.setListener(new LocationsOnMapFragment.Listener() {
                @Override
                public void onReady() {
                    fragment.focusOnLocation(locId, true);
                }

                @Override
                public void onLocationSelected(ILocationInfo locationInfo) {

                }

                @Override
                public void onLocationAdded(ILocationInfo locationInfo) {
                    mLastAddedLocationId = locationInfo.getId();
                }
            });

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit();
        } else {
            mLastAddedLocationId = savedInstanceState.getLong(EXTRA_LAST_ADDED_LOCATION_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putLong(EXTRA_LAST_ADDED_LOCATION_ID, mLastAddedLocationId);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_LAST_ADDED_LOCATION_ID, mLastAddedLocationId));
        finish();
    }
}

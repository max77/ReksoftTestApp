package com.example.mkomarovskiy.reksofttestapp.ui.map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, LocationsOnMapFragment.newInstance(0))
                    .commit();
    }
}

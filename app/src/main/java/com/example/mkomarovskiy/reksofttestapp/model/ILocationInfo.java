package com.example.mkomarovskiy.reksofttestapp.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */

public interface ILocationInfo {
    long ID_UNASSIGNED = -1;

    long getId();

    String getAddress();

    LatLng getLatLng();
}

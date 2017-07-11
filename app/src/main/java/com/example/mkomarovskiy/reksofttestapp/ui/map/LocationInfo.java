package com.example.mkomarovskiy.reksofttestapp.ui.map;

import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.google.android.gms.maps.model.LatLng;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 11/07/2017.
 */

class LocationInfo implements ILocationInfo {

    private long mId;
    private String mAddress;
    private LatLng mLatLng;

    public LocationInfo(long id, String address, LatLng latLng) {
        mId = id;
        mAddress = address;
        mLatLng = latLng;
    }

    public LocationInfo(ILocationInfo other) {
        mId = other.getId();
        mAddress = other.getAddress();
        mLatLng = new LatLng(other.getLatLng().latitude, other.getLatLng().longitude);
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public LatLng getLatLng() {
        return mLatLng;
    }
}

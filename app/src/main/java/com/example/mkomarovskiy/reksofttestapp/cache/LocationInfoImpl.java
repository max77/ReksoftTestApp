package com.example.mkomarovskiy.reksofttestapp.cache;

import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.google.android.gms.maps.model.LatLng;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 09/07/2017.
 */

class LocationInfoImpl implements ILocationInfo {
    private long mId;
    private String mAddress;
    private double mLat;
    private double mLon;

    LocationInfoImpl(long id, String address, double lat, double lon) {
        mId = id;
        mAddress = address;
        mLat = lat;
        mLon = lon;
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
        return new LatLng(mLat, mLon);
    }
}

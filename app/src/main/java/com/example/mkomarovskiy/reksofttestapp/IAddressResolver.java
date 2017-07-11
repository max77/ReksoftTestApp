package com.example.mkomarovskiy.reksofttestapp;

import android.content.Context;

import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.google.android.gms.maps.model.LatLng;

import io.reactivex.functions.Consumer;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 09/07/2017.
 */

public interface IAddressResolver {
    void resolveAddress(Context context, LatLng latLng);

    void subscribe(Consumer<ILocationInfo> consumer, Consumer<Throwable> errorConsumer);

    void unsubscribe();
}

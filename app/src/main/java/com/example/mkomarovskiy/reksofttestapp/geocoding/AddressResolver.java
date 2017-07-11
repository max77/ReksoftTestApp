package com.example.mkomarovskiy.reksofttestapp.geocoding;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.example.mkomarovskiy.reksofttestapp.IAddressResolver;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 09/07/2017.
 */
public class AddressResolver implements IAddressResolver {
    private static final AddressResolver ourInstance = new AddressResolver();
    private static final String TAG = "REKSOFT:AddressResolver";

    public static AddressResolver getInstance() {
        return ourInstance;
    }

    private AddressResolver() {
    }

    private volatile ConnectableObservable<? extends ILocationInfo> mWorker;
    private Disposable mDisposable;

    private ConnectableObservable<? extends ILocationInfo> createWorker(Context context, LatLng latLng) {
        return Observable.fromCallable(() -> {

            Log.d(TAG, "resolving address... ");
            Geocoder geocoder = new Geocoder(context);
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                List<String> addressFragments = new ArrayList<>();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                    addressFragments.add(address.getAddressLine(i));

                return (ILocationInfo) new ILocationInfo() {
                    @Override
                    public long getId() {
                        return ILocationInfo.ID_UNASSIGNED;
                    }

                    @Override
                    public String getAddress() {
                        return TextUtils.join(",", addressFragments);
                    }

                    @Override
                    public LatLng getLatLng() {
                        return latLng;
                    }
                };
            }

            return null;
        })
                .subscribeOn(Schedulers.io())
                .replay();
    }

    @Override
    synchronized public void resolveAddress(Context context, LatLng latLng) {
        mWorker = createWorker(context, latLng);
        mWorker.connect();
    }

    @Override
    synchronized public void subscribe(Consumer<ILocationInfo> consumer, Consumer<Throwable> errorConsumer) {
        if (mWorker == null)
            return;

        unsubscribe();
        mDisposable = mWorker
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    mWorker = null;
                    consumer.accept(s);
                }, error -> {
                    mWorker = null;
                    errorConsumer.accept(error);
                });
    }

    @Override
    synchronized public void unsubscribe() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
    }
}

package com.example.mkomarovskiy.reksofttestapp.ui.map;

import android.Manifest;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.mkomarovskiy.reksofttestapp.IRepository;
import com.example.mkomarovskiy.reksofttestapp.R;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.example.mkomarovskiy.reksofttestapp.ui.IPermissionHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 10/07/2017.
 */

public class MyMapFragment extends SupportMapFragment {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private IRepository mRepository;
    private IPermissionHandler mPermissionHandler;
    private MyMapFragmentListener mListener;
    private Map<Long, Marker> mMarkerMap = new HashMap<>();
    private ImageView mTargetPointer;
    private int mBottomPadding;

    public MyMapFragment() {
    }

    public MyMapFragment setRepository(IRepository repository) {
        mRepository = repository;
        return this;
    }

    public MyMapFragment setPermissionHandler(IPermissionHandler permissionHandler) {
        mPermissionHandler = permissionHandler;
        return this;
    }

    public MyMapFragment setListener(MyMapFragmentListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(layoutInflater, viewGroup, bundle);

        FrameLayout overlay = new FrameLayout(getContext());

        mTargetPointer = new ImageView(getContext());
        mTargetPointer.setVisibility(View.INVISIBLE);
        mTargetPointer.setImageResource(R.drawable.ic_vector_target);
        DrawableCompat.setTint(mTargetPointer.getDrawable(), getResources().getColor(R.color.colorMapTarget));

        overlay.addView(mTargetPointer, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ((ViewGroup) view.getRootView()).addView(overlay);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMapAsync(map -> {
            setupMap(map);
            showAllLocations(map);
        });
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @SuppressWarnings({"MissingPermission"})
    private void setupMap(GoogleMap map) {
        map.setOnCameraIdleListener(() -> {
            if (mListener != null)
                mListener.onCameraMoved();
        });

        map.setOnMapLoadedCallback(() -> setupTargetPointer(map));

        mPermissionHandler.requestPermissions((permissions, grantResult, numGranted) -> {
                    if (numGranted > 0) {
                        map.setMyLocationEnabled(true);
                        map.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                },
                true,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
    }

    private void setupTargetPointer(GoogleMap map) {
        Point center = map.getProjection().toScreenLocation(map.getCameraPosition().target);
        int size = mTargetPointer.getMeasuredWidth();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTargetPointer.getLayoutParams();
        params.leftMargin = center.x - size / 2;
        params.topMargin = center.y - size / 2 - mBottomPadding / 2;
        mTargetPointer.setLayoutParams(params);
    }

    public void showTarget(boolean show) {
        mTargetPointer.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void setMapPadding(int left, int top, int right, int bottom) {
        getMapAsync(map -> {
            mBottomPadding = bottom;
            map.setPadding(left, top, right, bottom);
        });
    }

    public void showAllLocations(GoogleMap map) {
        mCompositeDisposable.add(mRepository
                .getAllLocationInfos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((locations, error) -> {
                    if (error != null) {
                        if (mListener != null)
                            mListener.onError(error.getMessage());
                    } else
                        populateMap(map, locations);
                })
        );
    }

    private void populateMap(GoogleMap map, List<ILocationInfo> locations) {
        if (locations != null)
            for (ILocationInfo location : locations)
                addMarker(map, location);
    }

    public boolean canAddMarker(ILocationInfo locationInfo, double threshold) {
        for (Marker marker : mMarkerMap.values()) {
            if (SphericalUtil.computeDistanceBetween(locationInfo.getLatLng(), marker.getPosition()) < threshold)
                return false;
        }

        return true;
    }

    public void addMarker(ILocationInfo locationInfo) {
        getMapAsync(map -> addMarker(map, locationInfo));
    }

    private void addMarker(GoogleMap map, ILocationInfo locationInfo) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(locationInfo.getLatLng())
                .title(locationInfo.getAddress()));
        marker.setTag(locationInfo.getId());

        mMarkerMap.put(locationInfo.getId(), marker);
    }

    public void focusOnLocationMarker(long locationId) {
        Marker marker = mMarkerMap.get(locationId);
        if (marker != null) {
            marker.showInfoWindow();
            focusOnLatLng(marker.getPosition());
        }
    }

    public void focusOnLatLng(LatLng latLng) {
        getMapAsync(map -> map.animateCamera(CameraUpdateFactory.newLatLng(latLng)));
    }

    public void focusOnLatLngBounds(LatLngBounds bounds) {
        getMapAsync(map -> map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0)));
    }

    public void getVisibleBounds(Consumer<LatLngBounds> consumer) {
        getMapAsync(map -> consumer.accept(map.getProjection().getVisibleRegion().latLngBounds));
    }

    interface MyMapFragmentListener {
        void onCameraMoved();

        void onError(String error);
    }

}

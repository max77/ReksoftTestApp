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

import com.example.mkomarovskiy.reksofttestapp.R;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.example.mkomarovskiy.reksofttestapp.ui.IPermissionHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 10/07/2017.
 */

public class MyMapFragment extends SupportMapFragment {

    private static final float MARKER_FOCUS_ZOOM = 13;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private IPermissionHandler mPermissionHandler;
    private MyMapFragmentListener mListener;
    private Map<Long, Marker> mMarkerMap = new HashMap<>();
    private ImageView mTargetPointer;
    private int mBottomPadding;
    private GoogleMap mGoogleMap;

    public MyMapFragment() {
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

        getMapAsync(this::setupMap);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @SuppressWarnings({"MissingPermission"})
    private void setupMap(GoogleMap map) {
        mGoogleMap = map;

        mGoogleMap.setOnCameraIdleListener(() -> {
            if (mListener != null)
                mListener.onCameraMoved();
        });

        mGoogleMap.setOnMarkerClickListener(marker -> {
            if (mListener != null)
                mListener.onLocationSelected((Long) marker.getTag());
            return false;
        });

        mPermissionHandler.requestPermissions((permissions, grantResult, numGranted) -> {
                    if (numGranted > 0) {
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                },
                true,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);

        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setAllGesturesEnabled(true);

        if (mListener != null)
            mListener.onMapReady();
    }

    private void setupTargetPointer() {
        Point center = mGoogleMap.getProjection().toScreenLocation(mGoogleMap.getCameraPosition().target);
        int size = mTargetPointer.getMeasuredWidth();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTargetPointer.getLayoutParams();
        params.leftMargin = center.x - size / 2;
        params.topMargin = center.y - size / 2 - mBottomPadding / 2;
        mTargetPointer.setLayoutParams(params);
    }

    public void showTarget(boolean show) {
        if (mTargetPointer != null)
            mTargetPointer.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void setMapPadding(int left, int top, int right, int bottom) {
        mBottomPadding = bottom;
        mGoogleMap.setPadding(left, top, right, bottom);
        setupTargetPointer();
    }

    public void populateMap(List<ILocationInfo> locations) {
        mGoogleMap.clear();
        if (locations != null)
            for (ILocationInfo location : locations)
                addMarker(location);
    }

    public boolean canAddMarker(ILocationInfo locationInfo, double threshold) {
        for (Marker marker : mMarkerMap.values()) {
            if (SphericalUtil.computeDistanceBetween(locationInfo.getLatLng(), marker.getPosition()) < threshold)
                return false;
        }

        return true;
    }

    public void addMarker(ILocationInfo locationInfo) {
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(locationInfo.getLatLng())
                .title(locationInfo.getAddress()));
        marker.setTag(locationInfo.getId());

        mMarkerMap.put(locationInfo.getId(), marker);
    }

    public void focusOnLocation(long locationId) {
        Marker marker = mMarkerMap.get(locationId);
        if (marker != null) {
            marker.showInfoWindow();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), MARKER_FOCUS_ZOOM));
        }
    }

    public void focusOnLatLngBounds(LatLngBounds bounds) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
    }

    public LatLngBounds getVisibleBounds() {
        return mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
    }

    interface MyMapFragmentListener {
        void onMapReady();

        void onCameraMoved();

        void onLocationSelected(long locationId);
    }

}

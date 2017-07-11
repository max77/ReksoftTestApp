package com.example.mkomarovskiy.reksofttestapp.ui.map;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.mkomarovskiy.reksofttestapp.IAddressResolver;
import com.example.mkomarovskiy.reksofttestapp.IRepository;
import com.example.mkomarovskiy.reksofttestapp.R;
import com.example.mkomarovskiy.reksofttestapp.cache.DBRepository;
import com.example.mkomarovskiy.reksofttestapp.geocoding.AddressResolver;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.example.mkomarovskiy.reksofttestapp.ui.IPermissionHandler;
import com.example.mkomarovskiy.reksofttestapp.ui.permissions.PermissionHandlerFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.maps.android.SphericalUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */

public class LocationsOnMapFragment extends Fragment {

    private static final String ARG_LOCATION_ID = "locid";

    private static final String TAG_PLACE_INPUT_FRAGMENT = "placeinput";
    private static final String TAG_MAP_FRAGMENT = "map";

    private static final double ADDRESS_RESOLUTION_VIEWPORT_SIZE_THRESHOLD_METERS = 3000;
    private static final double NEIGHBOURING_MARKERS_THRESHOLD_METERS = 10;

    private MyMapFragment mMapFragment;
    private MyPlaceSelectionFragment mPlaceFragment;
    private FloatingActionButton mButtonAddLocation;

    private IAddressResolver mAddressResolver;
    private IRepository mRepository;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ILocationInfo mCurrentLocation;

    public static LocationsOnMapFragment newInstance(long locationId) {
        Bundle args = new Bundle();
        args.putLong(ARG_LOCATION_ID, locationId);

        LocationsOnMapFragment fragment = new LocationsOnMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRepository = DBRepository.getInstance(getContext());
        mAddressResolver = AddressResolver.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations_on_map, container, false);

        mButtonAddLocation = view.findViewById(R.id.button_add_location);

        IPermissionHandler permissionHandlerFragment = PermissionHandlerFragment.attach(getChildFragmentManager());

        if (savedInstanceState == null) {
            mMapFragment = new MyMapFragment();
            mPlaceFragment = new MyPlaceSelectionFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mMapFragment, TAG_MAP_FRAGMENT)
                    .replace(R.id.place_input_container, mPlaceFragment, TAG_PLACE_INPUT_FRAGMENT)
                    .commit();
        } else {
            mMapFragment = (MyMapFragment) getChildFragmentManager().findFragmentByTag(TAG_MAP_FRAGMENT);
            mPlaceFragment = (MyPlaceSelectionFragment) getChildFragmentManager().findFragmentByTag(TAG_PLACE_INPUT_FRAGMENT);
        }

        mMapFragment
                .setPermissionHandler(permissionHandlerFragment)
                .setRepository(mRepository);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                updateMapPadding();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapFragment.setListener(new MyMapFragment.MyMapFragmentListener() {
            @Override
            public void onCameraMoved() {
                handleCameraMove();
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });

        mPlaceFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMapFragment.focusOnLatLngBounds(place.getViewport());
            }

            @Override
            public void onError(Status status) {
                showError(status.getStatusMessage());
            }
        });

        // Типа убогий такой debounce... надо на Rx перходить
        mButtonAddLocation.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
            addLocationToRepository();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToAddressResolver();
    }

    @Override
    public void onStop() {
        unsubscribeFromAddressResolver();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    private void addLocationToRepository() {
        if (mCurrentLocation == null)
            return;

        if (!mMapFragment.canAddMarker(mCurrentLocation, NEIGHBOURING_MARKERS_THRESHOLD_METERS)) {
            showError(getString(R.string.error_markers_too_close, (int) NEIGHBOURING_MARKERS_THRESHOLD_METERS));
            return;
        }

        mCompositeDisposable.add(mRepository
                .addLocationInfo(mCurrentLocation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationInfo -> mMapFragment.addMarker(locationInfo)));
    }

    private void showTooFarMessage(boolean show) {
        getView().findViewById(R.id.too_far_message).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void subscribeToAddressResolver() {
        mAddressResolver.subscribe(
                locationInfo -> {
                    mCurrentLocation = locationInfo;
                    updateControls();
                },
                error -> showError(error.getMessage())
        );
    }

    private void updateControls() {
        if (mCurrentLocation != null) {
            mPlaceFragment.setText(mCurrentLocation.getAddress());
            mButtonAddLocation.setVisibility(View.VISIBLE);
        } else {
            mPlaceFragment.setText("");
            mButtonAddLocation.setVisibility(View.GONE);
        }
    }

    private void unsubscribeFromAddressResolver() {
        mAddressResolver.unsubscribe();
    }

    public void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    private void handleCameraMove() {
        mMapFragment.getVisibleBounds(visibleBounds -> {
            // Нет смысла определять адрес при масштабе карты больше определенного
            if (visibleBounds != null &&
                    SphericalUtil.computeDistanceBetween(visibleBounds.northeast, visibleBounds.southwest) <=
                            ADDRESS_RESOLUTION_VIEWPORT_SIZE_THRESHOLD_METERS) {
                mAddressResolver.resolveAddress(getContext(), visibleBounds.getCenter());
                subscribeToAddressResolver();

                mMapFragment.showTarget(true);
                showTooFarMessage(false);
            } else {
                mCurrentLocation = null;
                updateControls();
                mMapFragment.showTarget(false);
                showTooFarMessage(true);
            }

            mPlaceFragment.setBoundsBias(visibleBounds);
        });
    }

    private void updateMapPadding() {
        int space = getResources().getDimensionPixelOffset(R.dimen.margin_small);
        int topPadding = space + mPlaceFragment.getView().getBottom();

        mMapFragment.setMapPadding(0, topPadding, 0, 0);
    }
}

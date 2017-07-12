package com.example.mkomarovskiy.reksofttestapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.mkomarovskiy.reksofttestapp.IRepository;
import com.example.mkomarovskiy.reksofttestapp.R;
import com.example.mkomarovskiy.reksofttestapp.cache.DBRepository;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;
import com.example.mkomarovskiy.reksofttestapp.ui.map.LocationsOnMapFragment;
import com.example.mkomarovskiy.reksofttestapp.ui.map.MapActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 11/07/2017.
 */

public class AddressListActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_ITEM_ID = "itemid";
    private static final String KEY_LIST_SCROLL_POSITION = "scrollpos";
    private static final String TAG_MAP_PANEL = "mappanel";
    private static final int RQ_MAP_ACTIVITY = 1234;

    private RecyclerView mAddressList;
    private IRepository mRepository;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private LocationsOnMapFragment mLocationsOnMapFragment;
    private View mEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        mRepository = DBRepository.getInstance(this);

        boolean twoPanes = findViewById(R.id.map_panel_container) != null;

        mAddressList = findViewById(R.id.address_list);
        mAddressList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAddressList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAddressList.setAdapter(new AddressListAdapter(clickedItem -> {
            long itemId = clickedItem.getId();

            if (twoPanes) {
                getListAdapter().setSelectedItemId(itemId);
                mLocationsOnMapFragment.focusOnLocation(itemId, true);
            } else {
                showMap(clickedItem.getId());
            }
        }, twoPanes));

        mEmptyMessage = findViewById(R.id.empty_message);

        if (!twoPanes)
            mEmptyMessage.setOnClickListener(v -> showMap(-1));

        init(savedInstanceState, twoPanes);
    }

    private void showMap(long focusedId) {
        MapActivity.show(this, focusedId, RQ_MAP_ACTIVITY);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void init(Bundle savedInstanceState, boolean twoPanes) {
        long itemId = -1;
        int currentListScrollPosition = 0;

        if (savedInstanceState != null) {
            itemId = savedInstanceState.getLong(KEY_CURRENT_ITEM_ID, -1);
            currentListScrollPosition = savedInstanceState.getInt(KEY_LIST_SCROLL_POSITION, 0);
        }

        loadLocationsAndSelectItem(itemId, currentListScrollPosition);

        if (twoPanes) {
            if (savedInstanceState == null) {
                mLocationsOnMapFragment = new LocationsOnMapFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .add(R.id.map_panel_container, mLocationsOnMapFragment, TAG_MAP_PANEL)
                        .commit();
            } else {
                mLocationsOnMapFragment =
                        (LocationsOnMapFragment) getSupportFragmentManager().findFragmentByTag(TAG_MAP_PANEL);
            }

            mLocationsOnMapFragment.setListener(new LocationsOnMapFragment.Listener() {
                @Override
                public void onReady() {

                }

                @Override
                public void onLocationSelected(ILocationInfo locationInfo) {
                    if (locationInfo == null)
                        getListAdapter().setSelectedItemId(-1);
                    else
                        hiliteListItem(locationInfo);
                }

                @Override
                public void onLocationAdded(ILocationInfo locationInfo) {
                    addLocationToList(locationInfo);
                    hiliteListItem(locationInfo);
                }

                private void hiliteListItem(ILocationInfo locationInfo) {
                    getListAdapter().setSelectedItemId(locationInfo.getId());
                    mAddressList.smoothScrollToPosition(getListAdapter().findItemPositionById(locationInfo.getId()));
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_CURRENT_ITEM_ID, getListAdapter().getSelectedItemId());
        outState.putInt(KEY_LIST_SCROLL_POSITION,
                ((LinearLayoutManager) mAddressList.getLayoutManager()).findFirstCompletelyVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_MAP_ACTIVITY && resultCode == RESULT_OK) {
            long locId = MapActivity.extractLastAddedLocationId(data);

            if (locId != -1)
                loadLocationsAndSelectItem(locId, getListAdapter().getItemCount());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void loadLocationsAndSelectItem(long itemId, int currentListScrollPosition) {
        mCompositeDisposable.add(
                mRepository
                        .getAllLocationInfos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(locations -> {
                                    getListAdapter().setData(locations);
                                    getListAdapter().setSelectedItemId(itemId);
                                    mAddressList.scrollToPosition(currentListScrollPosition);
                                    showListOrEmpty();
                                },
                                error -> showError(error.getMessage()))
        );
    }

    private void addLocationToList(ILocationInfo locationInfo) {
        getListAdapter().addItem(locationInfo);
        mAddressList.scrollToPosition(getListAdapter().getItemCount() - 1);
        showListOrEmpty();
    }

    private void showListOrEmpty() {
        mEmptyMessage.setVisibility(getListAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private AddressListAdapter getListAdapter() {
        return (AddressListAdapter) mAddressList.getAdapter();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

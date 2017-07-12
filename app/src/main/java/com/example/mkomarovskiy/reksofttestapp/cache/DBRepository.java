package com.example.mkomarovskiy.reksofttestapp.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.mkomarovskiy.reksofttestapp.IRepository;
import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */


public class DBRepository implements IRepository {

    private static final String TAG = "REKSOFT:Repository";

    private static final DBRepository ourInstance = new DBRepository();

    public static DBRepository getInstance(Context context) {
        return ourInstance.setContext(context);
    }

    private DBRepository() {
    }

    private DBHelper mDBHelper;

    private DBRepository setContext(Context context) {
        if (mDBHelper == null)
            mDBHelper = new DBHelper(context.getApplicationContext());
        return this;
    }

    private ILocationInfo locationFromCursor(Cursor cursor) {
        return new LocationInfoImpl(
                cursor.getLong(LocationTable.IDX_ID),
                cursor.getString(LocationTable.IDX_ADDRESS),
                cursor.getDouble(LocationTable.IDX_LAT),
                cursor.getDouble(LocationTable.IDX_LON));
    }

    @Override
    public Single<ILocationInfo> getLocationInfoById(long id) {
        return Single.fromCallable(() -> getLocationInfoByIdInternal(id));
    }

    @Override
    public Single<List<ILocationInfo>> getAllLocationInfos() {
        return Single.fromCallable(() -> {
            List<ILocationInfo> locationInfos = new ArrayList<>();
            Cursor cursor = null;

            try {
                Log.d(TAG, "Getting all locations from DB...");

                cursor = mDBHelper
                        .getReadableDatabase()
                        .query(LocationTable.TABLE_NAME,
                                LocationTable.LOCATION_COLUMNS,
                                null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        locationInfos.add(locationFromCursor(cursor));
                    } while (cursor.moveToNext());
                }

                Log.d(TAG, "done " + cursor.getPosition());

                return locationInfos;
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        });
    }

    @Override
    public Single<ILocationInfo> addLocationInfo(ILocationInfo info) {
        return Single.fromCallable(() -> {
            Log.d(TAG, "Saving location to DB... " + info.toString());

            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationTable.COLUMN_ADDRESS, info.getAddress());
            contentValues.put(LocationTable.COLUMN_LAT, info.getLatLng().latitude);
            contentValues.put(LocationTable.COLUMN_LON, info.getLatLng().longitude);

            long id = mDBHelper
                    .getWritableDatabase()
                    .insert(LocationTable.TABLE_NAME,
                            null,
                            contentValues);

            return getLocationInfoByIdInternal(id);
        });
    }

    private ILocationInfo getLocationInfoByIdInternal(long id) {
        Cursor cursor = null;

        try {
            cursor = mDBHelper
                    .getReadableDatabase()
                    .query(LocationTable.TABLE_NAME,
                            LocationTable.LOCATION_COLUMNS,
                            LocationTable.COLUMN_ID + " = ?",
                            new String[]{Long.toString(id)},
                            null, null, null, null);

            if (cursor.moveToFirst())
                return locationFromCursor(cursor);
            return null;

        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    public Single<Boolean> deleteLocationInfoById(long id) {
        return Single.fromCallable(() -> mDBHelper
                .getWritableDatabase()
                .delete(LocationTable.TABLE_NAME,
                        LocationTable.COLUMN_ID + " = ?",
                        new String[]{Long.toString(id)}) > 0);
    }

    @Override
    public Single<Boolean> clearRepository() {
        return Single.fromCallable(() -> mDBHelper
                .getWritableDatabase()
                .delete(LocationTable.TABLE_NAME,
                        null,
                        null) > 0);
    }
}

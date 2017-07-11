package com.example.mkomarovskiy.reksofttestapp.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */

class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "reksoft_data.db";

    DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + LocationTable.TABLE_NAME + " ("
                + LocationTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LocationTable.COLUMN_ADDRESS + " TEXT, "
                + LocationTable.COLUMN_LAT + " REAL, "
                + LocationTable.COLUMN_LON + " REAL "
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME);
        onCreate(db);
    }
}

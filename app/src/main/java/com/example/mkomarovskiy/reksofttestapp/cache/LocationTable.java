package com.example.mkomarovskiy.reksofttestapp.cache;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */

interface LocationTable {

    String TABLE_NAME = "locations";

    String COLUMN_ID = "id";
    String COLUMN_ADDRESS = "address";
    String COLUMN_LAT = "lat";
    String COLUMN_LON = "lon";

    String[] LOCATION_COLUMNS = new String[]{
            COLUMN_ID,
            COLUMN_ADDRESS,
            COLUMN_LAT,
            COLUMN_LON
    };

    int IDX_ID = 0;
    int IDX_ADDRESS = 1;
    int IDX_LAT = 2;
    int IDX_LON = 3;
}

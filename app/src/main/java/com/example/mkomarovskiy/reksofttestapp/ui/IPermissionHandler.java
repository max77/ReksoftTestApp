package com.example.mkomarovskiy.reksofttestapp.ui;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 10/07/2017.
 */

public interface IPermissionHandler {
    void requestPermissions(RequestPermissionsCallback callback, boolean atLeastOne, String... permissions);

    interface RequestPermissionsCallback {
        void onRequestPermissionsResult(String[] permissions, int[] grantResult, int numGranted);
    }
}

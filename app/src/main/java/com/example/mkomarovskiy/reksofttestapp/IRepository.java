package com.example.mkomarovskiy.reksofttestapp;

import com.example.mkomarovskiy.reksofttestapp.model.ILocationInfo;

import java.util.List;

import io.reactivex.Single;

/**
 * ReksoftTestApp
 * Created by mkomarovskiy on 08/07/2017.
 */

public interface IRepository {
    Single<ILocationInfo> getLocationInfoById(long id);

    Single<List<ILocationInfo>> getAllLocationInfos();

    Single<ILocationInfo> addLocationInfo(ILocationInfo info);

    Single<Boolean> deleteLocationInfoById(long id);

    Single<Boolean> clearRepository();
}

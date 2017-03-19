package com.example.jerye.errand.module;

import android.content.Context;

import com.example.jerye.errand.classes.ErrandPreferences;
import com.example.jerye.errand.ui.ErrandAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jerye on 3/18/2017.
 */

@Module
public class ViewModule {
    private Context mContext;

    public ViewModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    PlaceSelectionListener providePlaceSelectionListener() {
        return new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


            }

            @Override
            public void onError(Status status) {            }
        };
    }


    @Provides
    @Singleton
    ErrandPreferences provideSharedPreferences() {
        return new ErrandPreferences(mContext);
    }

    @Provides
    @Singleton
    ErrandAdapter provideErrandAdapter() {
        return new ErrandAdapter(mContext);
    }
}

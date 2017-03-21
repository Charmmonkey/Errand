package com.example.jerye.errand.module;

import com.example.jerye.errand.classes.ErrandPreferences;
import com.example.jerye.errand.data.ErrandAdapter;
import com.example.jerye.errand.ui.MapsActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jerye on 3/18/2017.
 */

@Module
public class ViewModule {
    private MapsActivity mapsActivity;

    public ViewModule(MapsActivity activity) {
        mapsActivity = activity;
    }

    @Provides
    @Singleton
    ErrandPreferences provideSharedPreferences() {
        return new ErrandPreferences(mapsActivity);
    }

    @Provides
    @Singleton
    ErrandAdapter provideErrandAdapter() {
        return new ErrandAdapter(mapsActivity, mapsActivity);
    }

}

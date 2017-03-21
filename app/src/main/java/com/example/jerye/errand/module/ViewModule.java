package com.example.jerye.errand.module;

import android.content.Context;

import com.example.jerye.errand.classes.ErrandPreferences;
import com.example.jerye.errand.data.ErrandAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jerye on 3/18/2017.
 */

@Module
public class ViewModule {
    private Context mContext;
    private String destionation;
    private String origin;



    public ViewModule(Context context) {
        mContext = context;
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

package com.example.jerye.errand.module;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

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

    @Provides
    @Singleton
    ItemTouchHelper provideItemTouchHelper(){
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // delete from database

            }
        });
    }

}

package com.example.jerye.errand.component;

import com.example.jerye.errand.module.NetworkModule;
import com.example.jerye.errand.module.ViewModule;
import com.example.jerye.errand.ui.MapsActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by jerye on 3/18/2017.
 */

@Singleton
@Component(modules = {ViewModule.class, NetworkModule.class})
public interface ErrandComponent {
    void inject(MapsActivity mapsActivity);

}

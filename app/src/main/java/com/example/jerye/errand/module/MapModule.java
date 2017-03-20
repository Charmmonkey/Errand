package com.example.jerye.errand.module;

import com.example.jerye.errand.data.model.Leg;
import com.example.jerye.errand.data.model.MapDirectionResponse;
import com.example.jerye.errand.data.model.Polyline;
import com.example.jerye.errand.data.model.Route;
import com.example.jerye.errand.data.model.Step;
import com.example.jerye.errand.data.remote.MapDirectionService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by jerye on 3/19/2017.
 */

@Module
public class MapModule {

    private GoogleMap mGoogleMap;


    public MapModule() {

    }

//    @Provides
//    @Singleton
//    Subscriber<List<LatLng>> provideSubscriber() {
//        return
//        };
//    }

    @Provides
    @Singleton
    Func1<String, List<LatLng>> provideDecodedString() {
        return new Func1<String, List<LatLng>>() {
            @Override
            public List<LatLng> call(String s) {
                return PolyUtil.decode(s);
            }
        };
    }

    @Provides
    @Singleton
    Func1<Polyline, String> providePointsMap() {
        return new Func1<Polyline, String>() {
            @Override
            public String call(Polyline polyline) {
                return polyline.getPoints();
            }
        };
    }

    @Provides
    @Singleton
    Func1<Step, Polyline> providePolylineMap() {
        return new Func1<Step, Polyline>() {
            @Override
            public Polyline call(Step step) {
                return step.getPolyline();
            }
        };
    }

    @Provides
    @Singleton
    Func1<Leg, Observable<Step>> provideStepFlatMap() {
        return new Func1<Leg, Observable<Step>>() {
            @Override
            public Observable<Step> call(Leg leg) {
                return Observable.from(leg.getSteps());
            }
        };
    }

    @Provides
    @Singleton
    Func1<Route, Observable<Leg>> provideLegFlatMap() {
        return new Func1<Route, Observable<Leg>>() {
            @Override
            public Observable<Leg> call(Route route) {
                return Observable.from(route.getLegs());
            }
        };
    }

    @Provides
    @Singleton
    Func1<MapDirectionResponse, Observable<Route>> provideRouteFlatMap() {
        return new Func1<MapDirectionResponse, Observable<Route>>() {
            @Override
            public Observable<Route> call(MapDirectionResponse mapDirectionResponse) {
                return Observable.from(mapDirectionResponse.getRoutes());
            }
        };
    }

    @Provides
    @Singleton
    MapDirectionService provideMapDirectionService(Retrofit retrofitClient) {
        return retrofitClient.create(MapDirectionService.class);
    }

}

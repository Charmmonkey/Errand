package com.example.jerye.errand.module;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.jerye.errand.data.model.MapDirectionResponse;
import com.example.jerye.errand.data.model.Route;
import com.example.jerye.errand.data.remote.MapDirectionService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;

/**
 * Created by jerye on 3/18/2017.
 */


@Module
public class NetworkModule {
    Context mContext;
    FragmentActivity mFragmentActivity;
    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;

    public NetworkModule(Context context, FragmentActivity fragmentActivity, GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        mContext = context;
        mFragmentActivity = fragmentActivity;
        mOnConnectionFailedListener = onConnectionFailedListener;
    }


    @Provides
    @Singleton
    Subscriber<MapDirectionResponse> provideSubscriber() {
        return new Subscriber<MapDirectionResponse>() {
            @Override
            public void onCompleted() {
                Log.d("NetworkModule", "onComplete");

            }


            @Override
            public void onError(Throwable throwable) {
                Log.d("NetworkModule", throwable.toString());

            }

            @Override
            public void onNext(MapDirectionResponse mapDirectionResponse) {
                List<Route> routeList = mapDirectionResponse.getRoutes();
                Iterator<Route> iterator = routeList.iterator();
                while(iterator.hasNext()){
                    Route route = iterator.next();
                    route.getLegs();
                }

                Log.d("NetworkModule", routeList.toString());

            }
        };
    }

    @Provides
    @Singleton
    MapDirectionService provideMapDirectionService(Retrofit retrofitClient) {
        return retrofitClient.create(MapDirectionService.class);
    }

    @Provides
    @Singleton
    Retrofit provideRetrofitClient(@Named("baseURL") String baseURL) {
        return new Retrofit.Builder()
                .baseUrl(baseURL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Named("baseURL")
    String provideBaseURLString() {
        return "https://maps.googleapis.com/maps/api/directions/";
    }

    @Provides
    @Singleton
    GoogleApiClient provideGoogleApiClient() {
        return new GoogleApiClient
                .Builder(mContext)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(mFragmentActivity, mOnConnectionFailedListener)
                .build();
    }

}

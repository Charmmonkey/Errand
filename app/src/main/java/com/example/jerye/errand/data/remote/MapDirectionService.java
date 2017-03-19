package com.example.jerye.errand.data.remote;

import com.example.jerye.errand.data.model.MapDirectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by jerye on 3/16/2017.
 */

public interface MapDirectionService {

    @GET("json?origin=75+9th+Ave+New+York,+NY&destination=MetLife+Stadium+1+MetLife+Stadium+Dr+East+Rutherford,+NJ+07073")
    Observable<MapDirectionResponse> getDirection(@Query("key") String key);

}

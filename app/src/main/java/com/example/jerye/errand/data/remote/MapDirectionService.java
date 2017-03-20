package com.example.jerye.errand.data.remote;

import com.example.jerye.errand.data.model.MapDirectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by jerye on 3/16/2017.
 */

public interface MapDirectionService {

    @GET("json?origin=Boston,MA&destination=Concord,MA&waypoints=Charlestown,MA|Lexington,MA")
    Observable<MapDirectionResponse> getDirection(@Query("key") String key);

}

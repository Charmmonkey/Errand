package com.example.jerye.errand.data.remote;

import com.example.jerye.errand.data.model.MapDirectionResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by jerye on 3/16/2017.
 */

public interface MapDirectionService {

    @GET("")
    Call<MapDirectionResponse> getDirection();

}

package com.example.jerye.errand.ui;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;

import com.example.jerye.errand.R;
import com.example.jerye.errand.component.DaggerErrandComponent;
import com.example.jerye.errand.component.ErrandComponent;
import com.example.jerye.errand.data.model.MapDirectionResponse;
import com.example.jerye.errand.data.remote.MapDirectionService;
import com.example.jerye.errand.module.NetworkModule;
import com.example.jerye.errand.module.ViewModule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnConnectionFailedListener {
    private GoogleMap mMap;
    @BindView(R.id.left_drawer)
    RecyclerView drawer;


    @Inject
    GoogleApiClient mGoogleApiClient;
    @Inject
    MapDirectionService mMapDirectionService;
    @Inject
    Subscriber<MapDirectionResponse> mSubscriber;
    @Inject
    PlaceSelectionListener mPlaceSelectionListener;
    @Inject
    ErrandAdapter mErrandAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        ErrandComponent errandComponent = DaggerErrandComponent.builder()
                .viewModule(new ViewModule(this))
                .networkModule(new NetworkModule(this, this, this))
                .build();
        errandComponent.inject(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(mPlaceSelectionListener);

        mMapDirectionService.getDirection(getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubscriber);


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        drawer.setAdapter(mErrandAdapter);


    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}

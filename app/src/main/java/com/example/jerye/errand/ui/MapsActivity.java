package com.example.jerye.errand.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.jerye.errand.R;
import com.example.jerye.errand.component.DaggerErrandComponent;
import com.example.jerye.errand.component.ErrandComponent;
import com.example.jerye.errand.data.ErrandAdapter;
import com.example.jerye.errand.data.ErrandDBHelper;
import com.example.jerye.errand.data.model.Leg;
import com.example.jerye.errand.data.model.MapDirectionResponse;
import com.example.jerye.errand.data.model.Polyline;
import com.example.jerye.errand.data.model.Route;
import com.example.jerye.errand.data.model.Step;
import com.example.jerye.errand.data.remote.MapDirectionService;
import com.example.jerye.errand.module.MapModule;
import com.example.jerye.errand.module.NetworkModule;
import com.example.jerye.errand.module.ViewModule;
import com.example.jerye.errand.utility.Utility;
import com.facebook.stetho.Stetho;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnConnectionFailedListener,
        ErrandAdapter.ErrandAdapterClickHandler {
    private static GoogleMap mMap;
    private String selectedName;
    private String selectedId;
    private LatLng selectedLatLng;
    private LatLngBounds selectedLatLngBounds;
    private String selectedType;
    private static Cursor locationCursor;
    private static Cursor errandCursor;
    private BriteDatabase db;
    private String TAG = "MapsActivity";
    private Subscription errandSubscription;
    private Subscription locationSubscription;

    @BindView(R.id.left_drawer)
    RecyclerView drawer;

    @Inject
    GoogleApiClient mGoogleApiClient;
    @Inject
    MapDirectionService mMapDirectionService;
    @Inject
    ErrandAdapter mErrandAdapter;
    @Inject
    Func1<MapDirectionResponse, Observable<Route>> mMapDirectionResponse2Route;
    @Inject
    Func1<Route, Observable<Leg>> mRoute2Leg;
    @Inject
    Func1<Leg, Observable<Step>> mLeg2Step;
    @Inject
    Func1<Step, Polyline> mStep2Polyline;
    @Inject
    Func1<Polyline, String> mPolyline2String;
    @Inject
    Func1<String, List<LatLng>> mString2LatLng;


    @Override
    public void onClick(int position) {
        // Access cursor . position get 3 items;
        Log.d("MapsActivity", "Click event");
//        updateMapCamera();

        locationCursor.moveToPosition(position);
//        List<LatLngBounds> latLngs= (List<LatLngBounds>) PolyUtil.decode(locationCursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_LATLNG_BOUND));
        locationCursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_LATLNG_BOUND);
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Stetho.initializeWithDefaults(this);

        ButterKnife.bind(this);

        ErrandComponent errandComponent = DaggerErrandComponent.builder()
                .viewModule(new ViewModule(this))
                .networkModule(new NetworkModule(this, this, this))
                .mapModule(new MapModule())
                .build();
        errandComponent.inject(this);

        // SQLBrite
        ErrandDBHelper errandDBHelper = new ErrandDBHelper(this);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        db = sqlBrite.wrapDatabaseHelper(errandDBHelper, Schedulers.io());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                selectedName = (String) place.getName();
                selectedId = place.getId();
                selectedLatLng = place.getLatLng();
                selectedLatLngBounds = place.getViewport();
                selectedType = place.getPlaceTypes().toString();

                ContentValues locationCV = new ContentValues();
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_ID, selectedId);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_NAME, selectedName);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_LATLNG, selectedLatLng.toString());
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_LATLNG_BOUND, selectedLatLngBounds.toString());
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_TYPE, selectedType);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_ERRAND_ID, 1);
                Log.d(TAG, locationCV.toString());
                db.insert(ErrandDBHelper.LOCATION_TABLE_NAME, locationCV);
                mErrandAdapter.refreshList(locationCursor);
                //                getRoute(selectedName);
//                updateMapCamera(selectedName, selectedLatLng, selectedLatLngBounds);

            }

            @Override
            public void onError(Status status) {
                Log.e("ViewModule", status.toString());
            }
        });


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
        Log.d("MapsActivity", mMap.toString());

//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        LocationProvider location = locationManager.getProvider(LocationManager.GPS_PROVIDER);
//        Log.d("MapsActivity", location.toString()+"");

//         Add a marker in Sydney and move the camera

    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        drawer.setLayoutManager(layoutManager);
        drawer.setAdapter(mErrandAdapter);


        // Errand Query
        Observable<SqlBrite.Query> errandQuery = db.createQuery(
                ErrandDBHelper.ERRAND_TABLE_NAME,
                Utility.SQL_ERRAND_QUERY,
                Utility.getSqlErrandArg(this)
        );
        errandSubscription = errandQuery.subscribe(new Action1<SqlBrite.Query>() {
            @Override
            public void call(SqlBrite.Query query) {
                errandCursor = query.run();
                runLocationQuery();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        errandSubscription.unsubscribe();
        locationSubscription.unsubscribe();


    }

    private void updateMapCamera(String name, LatLng latLng, LatLngBounds latLngBounds) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(name));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));

    }

    private void getRoute(String destination) {
        mMapDirectionService.getDirection(destination, getString(R.string.google_maps_key))
                .flatMap(mMapDirectionResponse2Route)
                .flatMap(mRoute2Leg)
                .flatMap(mLeg2Step)
                .map(mStep2Polyline)
                .map(mPolyline2String)
                .map(mString2LatLng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<LatLng>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onNext(List<LatLng> latLng) {
                        PolylineOptions polylineOptions = new PolylineOptions().
                                jointType(JointType.ROUND).
                                endCap(new RoundCap()).
                                startCap(new RoundCap()).
                                width(20).
                                addAll(latLng);
                        mMap.addPolyline(polylineOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng.get(0), 15));
                    }
                });
    }

    private void runLocationQuery() {
        Observable<SqlBrite.Query> locationQuery = db.createQuery(
                ErrandDBHelper.LOCATION_TABLE_NAME,
                Utility.SQL_LOCATION_QUERY,
                Utility.getSqlErrandArg(this));
        locationSubscription = locationQuery.subscribe(new Action1<SqlBrite.Query>() {
            @Override
            public void call(SqlBrite.Query query) {
                locationCursor = query.run();
                Log.d("MapsActivity", "locationQuery ran");
                Log.d("MapsActivity", "count: " + locationCursor.getCount());
            }
        });
    }
}

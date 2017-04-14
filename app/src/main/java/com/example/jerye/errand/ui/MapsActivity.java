package com.example.jerye.errand.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.jerye.errand.R;
import com.example.jerye.errand.component.DaggerErrandComponent;
import com.example.jerye.errand.component.ErrandComponent;
import com.example.jerye.errand.data.ErrandAdapter;
import com.example.jerye.errand.data.ErrandDBHelper;
import com.example.jerye.errand.data.model.Leg;
import com.example.jerye.errand.data.model.MapDirectionResponse;
import com.example.jerye.errand.data.model.OverviewPolyline;
import com.example.jerye.errand.data.model.Polyline;
import com.example.jerye.errand.data.model.Route;
import com.example.jerye.errand.data.model.Step;
import com.example.jerye.errand.data.remote.MapDirectionService;
import com.example.jerye.errand.module.MapModule;
import com.example.jerye.errand.module.NetworkModule;
import com.example.jerye.errand.module.ViewModule;
import com.example.jerye.errand.utility.GestureUtility;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        OnConnectionFailedListener,
        ErrandAdapter.ErrandAdapterClickHandler {
    private static GoogleMap mMap;
    private String selectedName, selectedId, selectedType;
    private LatLng selectedLatLng;
    private static Cursor locationCursor, errandCursor;
    private BriteDatabase db;
    private String TAG = "MapsActivity";
    private Subscription errandSubscription, locationSubscription;
    public static LatLngBounds latLngBounds;
    private static String points;
    private static com.google.android.gms.maps.model.Polyline polyline;
    private static double NELat = 1000, NELng, SWLat, SWLng; // out of range
    private ArrayList<String> markerNames = new ArrayList<>();
    private ArrayList<LatLng> markerCoords = new ArrayList<>();
    private ArrayList<LatLng> polylineCoords = new ArrayList<>();
    private LatLngBounds polylineBounds;
    public final static String
            INSTANCE_SAVED = "INSTANCE_SAVED", INSTANCE_ROTATE = "INSTANCE_ROTATE",
            INSTANCE_NEW = "INSTANCE_NEW", INSTANCE_SAVED_IDLE = "INSTANCE_SAVED_IDLE",
            INSTANCE_ROTATE_IDLE = "INSTANCE_ROTATE_IDLE", INSTANCE_IDLE = "INSTANCE_IDLE";
    public static String INSTANCE_STATE = INSTANCE_SAVED_IDLE;


    @BindView(R.id.left_drawer)
    RecyclerView drawer;
    @BindView(R.id.drawer_layout)
    DrawerLayout mainView;

    @Inject
    GoogleApiClient mGoogleApiClient;
    @Inject
    MapDirectionService mMapDirectionService;
    @Inject
    ErrandAdapter mErrandAdapter;
    @Inject
    Func1<MapDirectionResponse, Observable<Route>> mMapDirectionResponse2Route;
    //    @Inject
//    Func1<Route, Observable<Leg>> mRoute2Leg;
    @Inject
    Func1<Leg, Observable<Step>> mLeg2Step;
    @Inject
    Func1<Step, Polyline> mStep2Polyline;
    //    @Inject
//    Func1<Polyline, String> mPolyline2String;
    @Inject
    Func1<String, List<LatLng>> mString2LatLng;


    @Override
    public void onClick(int position) {
        // Access cursor . position get 3 items;
        Log.d("MapsActivity", "Click event");
//        updateMapCamera();
        moveCamera(position);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Stetho.initializeWithDefaults(this);

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(getApplication());

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

        GestureUtility.provideItemTouchHelper(db, this, mErrandAdapter).attachToRecyclerView(drawer);


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                INSTANCE_STATE = INSTANCE_NEW;
                selectedName = (String) place.getName();
                selectedId = place.getId();
                selectedLatLng = place.getLatLng();
                selectedType = place.getPlaceTypes().toString();

                addMarker(selectedLatLng, selectedName);

                ContentValues locationCV = new ContentValues();
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_ID, selectedId);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_ORDER, locationCursor.getCount()); // order is offset by -11 from _id (starting 0)
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_NAME, selectedName);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_LAT, selectedLatLng.latitude);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_LNG, selectedLatLng.longitude);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_TYPE, selectedType);
                locationCV.put(ErrandDBHelper.COLUMN_LOCATION_ERRAND_ID, 1);

                Log.d(TAG, "Place selected locationCV: " + locationCV.toString());
                db.insert(ErrandDBHelper.LOCATION_TABLE_NAME, locationCV);
            }

            @Override
            public void onError(Status status) {
                Log.e("ViewModule", status.toString());
            }
        });


        Log.d(TAG, "instance state null");
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
        errandSubscription.unsubscribe();


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.toString(), Toast.LENGTH_LONG).show();
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
        Log.d("MapsActivity", "Map ready: " + mMap.toString());
        mMap.setOnCameraIdleListener(this);

    }

    @Override
    public void onCameraIdle() {
        // calls when layout fully inflates and map ready. Those 2 don't always match up.
        Log.d(TAG, "Camera Idle");
        instanceStateManager(INSTANCE_STATE);


        mMap.setOnCameraIdleListener(null);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d(TAG, "onStart");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        drawer.setLayoutManager(layoutManager);
        drawer.setAdapter(mErrandAdapter);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        mGoogleApiClient.disconnect();

        if (NELat != 1000) {
            ContentValues cv = new ContentValues();
            Log.d(TAG, "onStop points: " + points);
            Log.d(TAG, "onStop NELat: " + NELat);
            cv.put(ErrandDBHelper.COLUMN_ERRAND_PATH, points);
            cv.put(ErrandDBHelper.COLUMN_ERRAND_NAME, "Errand 1");
            cv.put(ErrandDBHelper.COLUMN_ERRAND_NE_LAT, NELat);
            cv.put(ErrandDBHelper.COLUMN_ERRAND_NE_LNG, NELng);
            cv.put(ErrandDBHelper.COLUMN_ERRAND_SW_LAT, SWLat);
            cv.put(ErrandDBHelper.COLUMN_ERRAND_SW_LNG, SWLng);

            if (errandCursor.getCount() == 0) {
                db.insert(ErrandDBHelper.ERRAND_TABLE_NAME, cv);
            } else {
                db.update(ErrandDBHelper.ERRAND_TABLE_NAME, cv, BaseColumns._ID + "= ?", Utility.getSqlErrandArg(this));
            }

        }
        locationSubscription.unsubscribe();


    }

    private void addMarker(LatLng latLng, String name) {
        markerNames.add(name);
        markerCoords.add(latLng);
        mMap.addMarker(new MarkerOptions().position(latLng).title(name));
    }

    private void moveCamera(int position) {
        locationCursor.moveToPosition(position);
        LatLng latLng = new LatLng(
                locationCursor.getDouble(ErrandDBHelper.COLUMN_ID_LOCATION_LAT),
                locationCursor.getDouble(ErrandDBHelper.COLUMN_ID_LOCATION_LNG));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void getRoute(Cursor storedLocationsCursor) {
        Log.d(TAG, "route get");

        mMapDirectionService.getDirection(
                Utility.getOrigin(storedLocationsCursor),
                Utility.getDestination(storedLocationsCursor),
                Utility.getWayPoints(storedLocationsCursor),
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .flatMap(mMapDirectionResponse2Route)
                .map(new Func1<Route, OverviewPolyline>() {
                         @Override
                         public OverviewPolyline call(Route route) {
                             NELat = route.getBounds().getNortheast().getLat();
                             NELng = route.getBounds().getNortheast().getLng();
                             SWLat = route.getBounds().getSouthwest().getLat();
                             SWLng = route.getBounds().getSouthwest().getLng();
                             LatLng northEast = new LatLng(NELat, NELng
                             );
                             LatLng southWest = new LatLng(SWLat, SWLng
                             );
                             latLngBounds = new LatLngBounds(southWest, northEast);
                             return route.getOverviewPolyline();
                         }
                     }
                )
                .map(new Func1<OverviewPolyline, String>() {
                    @Override
                    public String call(OverviewPolyline overviewPolyline) {
                        points = overviewPolyline.getPoints();
                        return points;
                    }
                })
                .map(mString2LatLng)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<LatLng>>() {
                    @Override
                    public void call(List<LatLng> latLngs) {
                        Log.d(TAG, "get Route: plot polyline");
                        plotPolyline(mMap, latLngs, latLngBounds);
                    }
                });
    }

    private void runLocationQuery() {
        Log.d(TAG, "location query ran");

        final Observable<SqlBrite.Query> locationQuery = db.createQuery(
                ErrandDBHelper.LOCATION_TABLE_NAME,
                Utility.SQL_LOCATION_QUERY,
                Utility.getSqlErrandArg(this));
        locationSubscription = locationQuery.map(new Func1<SqlBrite.Query, Cursor>() {
            @Override
            public Cursor call(SqlBrite.Query query) {
                return query.run();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                Log.d(TAG, "location callback triggered");
                locationCursor = cursor;

                instanceStateManager(INSTANCE_STATE);
            }

        });
    }

    public void plotPolyline(GoogleMap googleMap, List<LatLng> path, LatLngBounds bound) {
        Log.d(TAG, "plot polyline: " + path.toString());
        Log.d(TAG, "polyline plotted");

        polylineCoords = (ArrayList<LatLng>) path;
        polylineBounds = bound;

        PolylineOptions polylineOptions = new PolylineOptions().
                jointType(JointType.ROUND).
                endCap(new RoundCap()).
                startCap(new RoundCap()).
                width(15).
                color(Color.BLUE).
                addAll(path);
        polyline = googleMap.addPolyline(polylineOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 100));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("markerCoords", markerCoords);
        outState.putStringArrayList("markerNames", markerNames);
        outState.putParcelableArrayList("polylineCoords", polylineCoords);
        outState.putParcelable("polylineBounds", polylineBounds);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restored instance state");
        super.onRestoreInstanceState(savedInstanceState);
        INSTANCE_STATE = INSTANCE_ROTATE_IDLE;
        markerCoords = savedInstanceState.getParcelableArrayList("markerCoords");
        markerNames = savedInstanceState.getStringArrayList("markerNames");
        polylineCoords = savedInstanceState.getParcelableArrayList("polylineCoords");
        polylineBounds = savedInstanceState.getParcelable("polylineBounds");

    }

    private void recreateMarkers(GoogleMap googleMap, Cursor cursor) {
        Log.d(TAG, "recreated Markers");
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            LatLng latLng = new LatLng(
                    cursor.getDouble(ErrandDBHelper.COLUMN_ID_LOCATION_LAT),
                    cursor.getDouble(ErrandDBHelper.COLUMN_ID_LOCATION_LNG));
            String title = cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_NAME);
            markerCoords.add(latLng);
            markerNames.add(title);

            googleMap.addMarker(new MarkerOptions().title(title).position(latLng));
            Log.d(TAG, "markers recreated");
        }

    }

    private void instanceStateManager(String state) {
        Log.d(TAG, state);
        switch (state) {
            case INSTANCE_NEW:
                // New place selected.

                mMap.clear();

                mErrandAdapter.refreshList(locationCursor);
                recreateMarkers(mMap, locationCursor);
                if (locationCursor.getCount() > 1) {
                    getRoute(locationCursor);
                } else if (locationCursor.getCount() == 1) {
                    moveCamera(0);
                }

                break;
            case INSTANCE_SAVED:
                // Query for cached data one time on activity first start

                mMap.clear();

                if (errandCursor.moveToFirst()) {
                    List<LatLng> preferredRoute = Utility.getPreferredRoutePoints(errandCursor);
                    LatLngBounds preferredBound = Utility.getPreferredRouteBound(errandCursor);
                    Log.d(TAG, "cameraIdle");
                    plotPolyline(mMap, preferredRoute, preferredBound);
                }

                mErrandAdapter.refreshList(locationCursor);
                recreateMarkers(mMap, locationCursor);

                INSTANCE_STATE = INSTANCE_IDLE;

                break;
            case INSTANCE_ROTATE:
                // On restore instance state

                mErrandAdapter.refreshList(locationCursor);
                for (int i = 0; i < markerCoords.size(); i++) {
                    mMap.addMarker(new MarkerOptions().position(markerCoords.get(i)).title(markerNames.get(i)));
                    Log.d(TAG, "instance marker added");
                }

                Log.d(TAG, "Restore instance state polyline: " + polylineCoords.toString());
                plotPolyline(mMap, polylineCoords, polylineBounds);

                INSTANCE_STATE = INSTANCE_IDLE;

                break;
            case INSTANCE_SAVED_IDLE:
                INSTANCE_STATE = INSTANCE_SAVED;
                break;
            case INSTANCE_ROTATE_IDLE:
                INSTANCE_STATE = INSTANCE_ROTATE;
                break;
            case INSTANCE_IDLE:
                break;
        }
    }
}

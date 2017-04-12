package com.example.jerye.errand.utility;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.jerye.errand.data.ErrandDBHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.util.List;

/**
 * Created by jerye on 3/17/2017.
 */

public class Utility {
    private static String[] locationArgs = {"1"};
    private static final String TAG = "Utility.java";

    public static final String SQL_ERRAND_QUERY = "SELECT * " +
            " FROM " + ErrandDBHelper.ERRAND_TABLE_NAME +
            " WHERE " + BaseColumns._ID +
            " = ?";

    public static final String SQL_LOCATION_QUERY = "SELECT * " +
            " FROM " + ErrandDBHelper.LOCATION_TABLE_NAME +
            " WHERE " + ErrandDBHelper.COLUMN_LOCATION_ERRAND_ID +
            " = ?" +
            " ORDER BY " + ErrandDBHelper.COLUMN_LOCATION_ORDER + " ASC";

    public static String getSqlErrandArg(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("errand", "1");
    }

    public static String getOrigin(Cursor cursor) {
        cursor.moveToFirst();
        return "place_id:"+cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_ID);
    }

    public static String getDestination(Cursor cursor) {
        cursor.moveToLast();
        return "place_id:"+cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_ID);
    }

    public static String getWayPoints(Cursor cursor) {
        String waypoint;
        String waypoints = "";
        for (int i = 1; i < cursor.getCount() - 1; i++) {
            cursor.moveToPosition(i);
            waypoint = "via:place_id:"+cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_ID);
            waypoints = waypoints + waypoint + "|";
        }
        Log.d(TAG, waypoints);
        return waypoints;
    }

    public static List<LatLng> getPreferredRoutePoints(Cursor cursor) {
        if (cursor.moveToFirst()) {
            String points = cursor.getString(ErrandDBHelper.COLUMN_ID_ERRAND_PATH);
            return PolyUtil.decode(points);
        }
        return null;
    }

    public static LatLngBounds getPreferredRouteBound(Cursor cursor) {
        LatLng ne = new LatLng(
                cursor.getDouble(ErrandDBHelper.COLUMN_ID_ERRAND_NE_LAT),
                cursor.getDouble(ErrandDBHelper.COLUMN_ID_ERRAND_NE_LNG));
        LatLng sw = new LatLng(
                cursor.getDouble(ErrandDBHelper.COLUMN_ID_ERRAND_SW_LAT),
                cursor.getDouble(ErrandDBHelper.COLUMN_ID_ERRAND_SW_LNG));
        LatLngBounds latLngBounds = new LatLngBounds(sw, ne);
        return latLngBounds;
    }


}

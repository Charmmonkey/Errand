package com.example.jerye.errand.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.example.jerye.errand.data.ErrandAdapter;
import com.example.jerye.errand.data.ErrandDBHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import com.squareup.sqlbrite.BriteDatabase;

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

    public static String getDestination(Cursor cursor) {
        cursor.moveToLast();
        return cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_NAME);
    }

    public static String getWayPoints(Cursor cursor) {
        String waypoint;
        String waypoints = "";
        for (int i = 1; i < cursor.getCount() - 1; i++) {
            waypoint = cursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_NAME);
            waypoints = waypoints + waypoint + "|";
        }
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

    public interface ItemTouchHelperAdapter {
        void onItemDrag(int from, int to);

        void onItemSwipe(int position);
    }

    public static ItemTouchHelper provideItemTouchHelper(final BriteDatabase db, final Context context, final ErrandAdapter errandAdapter) {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            private int fromPosition;
            private int toPosition;
            private int initialPosition = -1;
            private int finalPosition;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                fromPosition = viewHolder.getAdapterPosition();
                toPosition = target.getAdapterPosition();

                Log.d(TAG, "from position: " + fromPosition);
                Log.d(TAG, "to position: " + toPosition);

                if (initialPosition == -1) {
                    initialPosition = fromPosition;
                }
                finalPosition = toPosition;

                errandAdapter.onItemDrag(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // delete from database
                Log.d(TAG, "onSwiped");
                int position = viewHolder.getAdapterPosition();
//                errandAdapter.onItemSwipe(position);

                BriteDatabase.Transaction transaction = db.newTransaction();


                try {

                    db.delete(ErrandDBHelper.LOCATION_TABLE_NAME,
                            ErrandDBHelper.COLUMN_LOCATION_ORDER + " = ?",
                            position + ""
                    );
                    Log.d(TAG, "onSwiped: " + errandAdapter.getItemCount() + "");
                    for (int i = position; i < errandAdapter.getItemCount() - 1; i++) {
                        ContentValues cv = new ContentValues();
                        cv.put(ErrandDBHelper.COLUMN_LOCATION_ORDER, i);
                        db.update(ErrandDBHelper.LOCATION_TABLE_NAME,
                                cv,
                                ErrandDBHelper.COLUMN_LOCATION_ORDER + " = ?",
                                i + 1 + ""
                        );
                        Log.d(TAG, "onSwiped for loop: " + cv.toString());

                    }

                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                }


            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int sign;
                BriteDatabase.Transaction transaction = db.newTransaction();
                ContentValues cvTemp = new ContentValues();
                cvTemp.put(ErrandDBHelper.COLUMN_LOCATION_ORDER, -1);
                ContentValues cvTo = new ContentValues();
                cvTo.put(ErrandDBHelper.COLUMN_LOCATION_ORDER, finalPosition);

                Log.d(TAG, "initial position: " + initialPosition);
                Log.d(TAG, "final position: " + finalPosition);

                try {
                    if (initialPosition > finalPosition) {
                        sign = -1;
                    } else {
                        sign = 1;
                    }

                    db.update(ErrandDBHelper.LOCATION_TABLE_NAME,
                            cvTemp,
                            ErrandDBHelper.COLUMN_LOCATION_ORDER + " = ?",
                            initialPosition + "");

                    for (int i = initialPosition; i * sign < finalPosition * sign; i = i + sign) {
                        ContentValues cv = new ContentValues();
                        cv.put(ErrandDBHelper.COLUMN_LOCATION_ORDER, i);
                        db.update(ErrandDBHelper.LOCATION_TABLE_NAME,
                                cv,
                                ErrandDBHelper.COLUMN_LOCATION_ORDER + " = ?",
                                i + sign + ""
                        );
                    }

                    db.update(ErrandDBHelper.LOCATION_TABLE_NAME,
                            cvTo,
                            ErrandDBHelper.COLUMN_LOCATION_ORDER + " = ?",
                            -1 + "");
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                    initialPosition = -1;
                }


                super.clearView(recyclerView, viewHolder);
            }
        });
    }


}

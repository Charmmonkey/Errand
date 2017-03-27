package com.example.jerye.errand.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by jerye on 3/21/2017.
 */

public class ErrandDBHelper extends SQLiteOpenHelper {
    private static final String TAG = ErrandDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "errand.db";
    private static final int DATABASE_VERSION = 6;
    public static final String LOCATION_TABLE_NAME = "location ";
    public static final String COLUMN_LOCATION_ID = "locationId";
    public static final String COLUMN_LOCATION_ORDER = "locationOrder";
    public static final String COLUMN_LOCATION_NAME = "locationName";
    public static final String COLUMN_LOCATION_LAT = "locationLat";
    public static final String COLUMN_LOCATION_LNG = "locationLng";
    public static final String COLUMN_LOCATION_TYPE = "locationType";
    public static final String COLUMN_LOCATION_ERRAND_ID = "locationErrandId";


    public static final String ERRAND_TABLE_NAME = "errand";
    public static final String COLUMN_ERRAND_NAME = "errandName";
    public static final String COLUMN_ERRAND_PATH = "errandPath";
    public static final String COLUMN_ERRAND_NE_LAT = "errandNELat";
    public static final String COLUMN_ERRAND_NE_LNG = "errandNELng";
    public static final String COLUMN_ERRAND_SW_LAT = "errandSWLat";
    public static final String COLUMN_ERRAND_SW_LNG = "errandSWLng";



    public static final int COLUMN_ID_LOCATION_BASE_ID = 0;
    public static final int COLUMN_ID_LOCATION_ID = 1;
    public static final int COLUMN_ID_LOCATION_ORDER = 2;
    public static final int COLUMN_ID_LOCATION_NAME = 3;
    public static final int COLUMN_ID_LOCATION_LAT = 4;
    public static final int COLUMN_ID_LOCATION_LNG = 5;
    public static final int COLUMN_ID_LOCATION_TYPE = 6;
    public static final int COLUMN_ID_LOCATION_ERRAND_ID = 7;

    public static final int COLUMN_ID_ERRAND_BASE_ID = 0;
    public static final int COLUMN_ID_ERRAND_NAME = 1;
    public static final int COLUMN_ID_ERRAND_PATH = 2;
    public static final int COLUMN_ID_ERRAND_NE_LAT = 3;
    public static final int COLUMN_ID_ERRAND_NE_LNG = 4;
    public static final int COLUMN_ID_ERRAND_SW_LAT = 5;
    public static final int COLUMN_ID_ERRAND_SW_LNG = 6;


    public ErrandDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //TODO: Create and fill the database
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LOCATION_TABLE_NAME + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LOCATION_ID + " TEXT NOT NULL,"
                + COLUMN_LOCATION_ORDER + " INTEGER, "
                + COLUMN_LOCATION_NAME + " TEXT NOT NULL, "
                + COLUMN_LOCATION_LAT + " REAL NOT NULL, "
                + COLUMN_LOCATION_LNG + " REAL NOT NULL, "
                + COLUMN_LOCATION_TYPE + " TEXT NOT NULL, "
                + COLUMN_LOCATION_ERRAND_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_LOCATION_ERRAND_ID
                + ") REFERENCES " + ERRAND_TABLE_NAME
                + "(" + BaseColumns._ID + ")" + " ON DELETE CASCADE"
                + ");";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);

        final String SQL_CREATE_ERRAND_TABLE = "CREATE TABLE " + ERRAND_TABLE_NAME + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ERRAND_NAME + " TEXT NOT NULL, "
                + COLUMN_ERRAND_PATH + " TEXT NOT NULL, "
                + COLUMN_ERRAND_NE_LAT + " REAL NOT NULL, "
                + COLUMN_ERRAND_NE_LNG + " REAL NOT NULL, "
                + COLUMN_ERRAND_SW_LAT + " REAL NOT NULL, "
                + COLUMN_ERRAND_SW_LNG + " REAL NOT NULL"
                + ");";
        db.execSQL(SQL_CREATE_ERRAND_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: Handle database version upgrades
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ERRAND_TABLE_NAME);
        onCreate(db);
    }

}

package com.example.jerye.errand.utility;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.example.jerye.errand.data.ErrandDBHelper;

/**
 * Created by jerye on 3/17/2017.
 */

public class Utility {
    private static String[] locationArgs = {"1"};

    public static final String SQL_ERRAND_QUERY = "SELECT * " +
            " FROM " + ErrandDBHelper.ERRAND_TABLE_NAME +
            " WHERE " + BaseColumns._ID +
            " = ?";

    public static final String SQL_LOCATION_QUERY = "SELECT * " +
            " FROM " + ErrandDBHelper.LOCATION_TABLE_NAME +
            " WHERE " + ErrandDBHelper.COLUMN_LOCATION_ERRAND_ID +
            " = ?";

    public static String getSqlErrandArg(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("errand", "1");
    }


}

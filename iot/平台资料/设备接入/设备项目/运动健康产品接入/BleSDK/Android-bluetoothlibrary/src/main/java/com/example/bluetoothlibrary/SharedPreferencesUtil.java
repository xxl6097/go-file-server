package com.example.bluetoothlibrary;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by laiyiwen on 2017/4/12.
 */

public class SharedPreferencesUtil {


    public final static String PROJECTNAME = "bluetooth";


    public static SharedPreferences getProjectSP(Context context) {
        return context.getSharedPreferences(PROJECTNAME, Context.MODE_PRIVATE);
    }

    public static boolean setEquipmentSynchronizationTime(Context context,
                                                          String equipment_synchronization_time) {
        SharedPreferences sp = getProjectSP(context);
        SharedPreferences.Editor editor = sp.edit();
        return editor.putString("equipment_synchronization_time", equipment_synchronization_time).commit();
    }


    public static String getEquipmentSynchronizationTime(Context context) {
        SharedPreferences sp = getProjectSP(context);
        // return sp
        // .getString(context.getString(R.string.last_time_sync) +
        // "equipment_synchronization_time", "");
        return sp.getString("equipment_synchronization_time", "");
    }
}

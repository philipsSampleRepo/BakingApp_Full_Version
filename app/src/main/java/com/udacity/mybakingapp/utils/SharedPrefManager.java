package com.udacity.mybakingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 *  provides this software on as "as is", "where is" basis, with all faults known and unknown.
 *  makes no warranty, express, statutory or implied, and explicitly disclaims the * *
 * warranties or merchantability, fitness for a particular purpose, any warranty of non-infringement
 * of any third party’s intellectual property rights, any warranty that the licensed * works will
 * meet the requirements of licensee or any other user, any warrantee that the software will be
 * error-free or will operate without interruption, and any warranty that the software will
 * interoperate with any licensee or third party hardware, software or systems.  undertakes
 * no obligation whatsoever to support or maintain all or any part of this software.
 * The software is not fault tolerant and is not designed, intended or authorized for use in any
 * medical, lifesaving or life sustaining systems, or any other application in which the failure
 * of the licensed work could create a situation where personal injury or death may occur.
 * <p>
 * All other rights are reserved.
 **/
public class SharedPrefManager {

    private static final String VIEW_STATUS = "com.udacity.mybakingapp.utils.VIEW_STATUS";
    private static final String TAG = SharedPrefManager.class.getSimpleName();

    public static boolean getViewGuideStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(VIEW_STATUS, Context.MODE_PRIVATE);
        boolean viewStatus = prefs.getBoolean(VIEW_STATUS, false);
        Log.d(TAG, "getViewGuideStatus: " + viewStatus);
        return viewStatus;
    }

    public static void setViewGuideStatus(Context context, boolean value) {
        Log.d(TAG, "setViewGuideStatus: " + value);
        SharedPreferences.Editor editor = context
                .getSharedPreferences(VIEW_STATUS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(VIEW_STATUS, value);
        editor.apply();
    }
}

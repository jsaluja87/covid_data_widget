package com.saluja_apps.covid_widget.InternetChecker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.saluja_apps.covid_widget.GlobalVariables;

public class CheckInternetClass {

    private Context context;
    private static final String TAG = "CheckInternetClass";
    public CheckInternetClass(Context context) {
        this.context = context;
    }

    public void registerDefaultNetworkCallback(){
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            assert connectivityManager != null;
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull android.net.Network network) {
                    super.onAvailable(network);
                    GlobalVariables.isNetworkConnected = true;
                    Log.d(TAG, "onAvailable");
                }

                @Override
                public void onLost(@NonNull android.net.Network network) {
                    super.onLost(network);
                    GlobalVariables.isNetworkConnected = false;
                    Log.d(TAG, "onLost");
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "failed");
            GlobalVariables.isNetworkConnected = false;
        }
    }
}

package com.saluja_apps.covid_widget.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.saluja_apps.covid_widget.widgets.CovidWidgetProvider;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private boolean debug=false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Pixel atleast, clears all the logs after the phone boots up. So it Log.d dosen't capture the message.
        // Toast here shows that the boot is being caught by the app
        if(debug) {
            Log.d("BootCompletedIntentReceiver", " Got action " + intent.getAction());
        }
        //Toast.makeText(context, "Got the boot request", Toast.LENGTH_LONG).show();

        CovidWidgetProvider.setCountryDataAndAlarmForWidget(context, CovidWidgetProvider.setIndiaData);
    }
}

package com.saluja_apps.covid_widget.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.content.Context;
import android.widget.Toast;

import com.saluja_apps.covid_widget.R;
import com.saluja_apps.covid_widget.model.CovidWidgetDataItemClass;
import com.saluja_apps.covid_widget.model.IndiaCovidData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CovidWidgetProvider extends AppWidgetProvider {
    static String TAG = "CovidWidget";
    static boolean debug = false;
    //Constants for click listener use
    //We create our own action to distinguish our work in broadcast receiver

    //Broadcast Receiver Action Strings
    public static final String prevButtonOnClickAction = "com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.prevButtonOnClick";
    public static final String nextButtonOnClickAction = "com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.nextButtonOnClick";
    public static final String addStateButtonOnClickAction = "com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.addStateButtonOnClick";

    //==============================================================================================
    public static final int CountryOffsetNullValue = 100000;
    //India related items
    //Action to set India data and refresh alarm
    public static final String setIndiaData = "com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.setIndiaData";
    public static final String loadIndiaDataIntoAllWidgetInstances = "com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.loadIndiaDataIntoAllWidgetInstances";

    //Parameter to save the data for India Covid case
    static IndiaCovidData indiaCovidData = null;
    public static ArrayList<String> indiaStateListFromConfig = new ArrayList<>();


   //==============================================================================================

    //The mohfw.gov.in gets updated at 8am everyday IST. Setting the alarm to fetch daily data at 8.05am IST
    public static final int IndiaDataRefreshHourOfDay = 8;
    public static final int IndiaDataRefreshMinute = 5;
    public static final int IndiaDataRefreshSecond = 0;

    public static int currentWidgetItemPosition = 0;


    public static ArrayList<CovidWidgetDataItemClass> covidWidgetIndiaDataItemClasses = new ArrayList<>();

    //Make an instance of the config class to get access to the data object
    CovidWidgetConfig covidWidgetConfig = new CovidWidgetConfig();

    Context globalProviderContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //==================================================================
        //Add random data to the object=
        //widgetOutlookDataClass =   WidgetOutlookDataItemClass.addRandomData();
        //==================================================================
        //When a configuration class is there, onUpdate() is not called the first time widget is placed
        //on the home screen. It will still be called on future updates. But we have to make the same changes
        //here from the configuration and set the intent because configuration class won't be called when we make any
        //changes or restart our phone. Instead onUpdate is called
        if(debug) {
            Log.d(TAG, "jassi onUpdate called\n");
        }
        //Go through all widget instances
        for (int appWidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            finalUpdateToTheWidget(rv, context, appWidgetId, appWidgetManager, false);

        }
    }



    /*
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        //Called when we resize our widget.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        //resize the widget
        resizeWidget(newOptions, rv, context);
        //Update the widgets
        finalUpdateToTheWidget(rv, context, appWidgetId, appWidgetManager, false);
    }


    private void resizeWidget(Bundle appWidgetOptions, RemoteViews rv, Context context) {
        //We support WxH = 1x1, 1x2, 2x1, 2x2
        //Portrait mode
        int minWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        //DUO
        //minWidth available = 106, 237, 368, 499 and so on
        //maxHeight available = 98, 220,242,464 and so on
        //minWidth<237, maxHeight=98 -> smallest layout
        //minWidth>=237, maxHeight=98 -> expanded layout
        //minWidth>=237, maxHeight>98 -> detailed center layout

        //Landscape mode
        int maxWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        Log.d(TAG, "widget minWidth x maxHeight is\n"+minWidth + " and " + maxHeight);
        Log.d(TAG, "widget maxWidth x minHeight is\n"+maxWidth + " and " + minHeight);
        //DUO
        //minWidth available = 121, 266, 411, 566 and so on
        //minHeight available = 83, 190,297
        //maxWidth<266, minHeight=83 -> smallest layout
        //maxWidth>=266, minHeight=83 -> expanded layout
        //maxWidth>=266, minHeight>83 -> detailed center layout

        //Doubly making sure that the splash image is invisible for all configs
       // removeSplashImage(rv);

        //Make all other views visible
       // addBackTheDefaultViews(rv, minWidth, maxHeight, maxWidth, minHeight);
    }
    */




    @Override
    public void onReceive(final Context context, Intent intent) {

        super.onReceive(context,intent);
        if(debug) {
            Log.d(TAG, "onReceive called with action" + intent.getAction());
            //Toast.makeText(context, "onReceive with action" + intent.getAction(), Toast.LENGTH_LONG).show();
        }
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, CovidWidgetProvider.class));

        if(nextButtonOnClickAction.equals(intent.getAction())) {
            //Just making sure that there is something inside the data model
            if((indiaCovidData == null) || (indiaCovidData.regionData.size() == 0)) {
                loadIndiaData(appWidgetIds, context, appWidgetManager, false);
            }

            if((covidWidgetIndiaDataItemClasses == null) || (covidWidgetIndiaDataItemClasses.size() == 0)) {
                loadIndiaStateListFromConfig(context);
                updateIndiaDataIntoIndiaDateItem(indiaStateListFromConfig);
            }

            loadNextItem(appWidgetIds, context, appWidgetManager);
        } else if(prevButtonOnClickAction.equals(intent.getAction())) {
            if((indiaCovidData == null) || (indiaCovidData.regionData.size() == 0)) {
                loadIndiaData(appWidgetIds, context, appWidgetManager, false);
            }

            if((covidWidgetIndiaDataItemClasses == null) || (covidWidgetIndiaDataItemClasses.size() == 0)) {
                loadIndiaStateListFromConfig(context);
                updateIndiaDataIntoIndiaDateItem(indiaStateListFromConfig);
            }

            loadPrevItem(appWidgetIds, context, appWidgetManager);
        } else if(setIndiaData.equals(intent.getAction())) {
            loadIndiaData(appWidgetIds, context, appWidgetManager, false);
            //Set another alarm here to load India data tomorrow
            setAlarmToUpdateCountryData(context, IndiaDataRefreshHourOfDay, IndiaDataRefreshMinute,
                    IndiaDataRefreshSecond, setIndiaData,
                    context.getString(R.string.country_india), false
            );
        } else if(loadIndiaDataIntoAllWidgetInstances.equals(intent.getAction())) {
            //USED FOR THE REFRESH BUTTON
            if(debug) {
                Log.d(TAG, "onReceive called for loadIndiaDataIntoAllWidgetInstances action\n");
            }

            final boolean refreshButtonIntent = intent.getBooleanExtra(context.getString(R.string.refresh_action_intent), false);

            //The goal is to give to user a feeling of things being refreshed.
            //Make the numbers invisible for 300ms seconds and make them visible and load the results again(to handle to case when the number might have changed)
            makeWidgetStatsInvisible(appWidgetIds, context, appWidgetManager);
            if(debug) {
                Log.d(TAG, "before delay\n");
            }
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {

                    if(debug) {
                        Log.d(TAG, "after delay inside\n");
                    }
                    loadIndiaData(appWidgetIds, context, appWidgetManager, refreshButtonIntent);
                    makeWidgetStatsVisible(appWidgetIds, context, appWidgetManager);
                }
            };
            handler.postDelayed(r, 100);
        }
    }

    public void makeWidgetStatsInvisible(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager) {

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //Make the splash view visible
            rv.setViewVisibility(R.id.widget_total_cases_value_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_total_deaths_value_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_new_cases_value_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_new_deaths_value_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_total_cases_value_state_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_total_deaths_value_state_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_new_cases_value_state_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_new_deaths_value_state_id, View.INVISIBLE);

            finalUpdateToTheWidget(rv, context, appWidgetId, appWidgetManager, false);
        }
    }

    public void makeWidgetStatsVisible(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager) {

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //Make the splash view visible
            rv.setViewVisibility(R.id.widget_total_cases_value_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_total_deaths_value_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_new_cases_value_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_new_deaths_value_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_total_cases_value_state_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_total_deaths_value_state_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_new_cases_value_state_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_new_deaths_value_state_id, View.VISIBLE);

            finalUpdateToTheWidget(rv, context, appWidgetId, appWidgetManager, false);
        }
    }

    public void advanceToNextItemPosition() {

        if(debug) {
            Log.d(TAG, "current pos is " + currentWidgetItemPosition);
        }
        printCovidWidgetItemClass(covidWidgetIndiaDataItemClasses);
        if(currentWidgetItemPosition == covidWidgetIndiaDataItemClasses.size()-1) {
            currentWidgetItemPosition = 0;
        } else {
            currentWidgetItemPosition = currentWidgetItemPosition + 1;
        }

        if(debug) {
            Log.d(TAG, "next current pos is " + currentWidgetItemPosition);
        }
    }

    //==============================================================================================

    //Only called when we have to fetch data from API
    public void loadIndiaData(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager, boolean refreshAction) {
        boolean fetchSuccessful = false;
            //fetch the data for India
        fetchSuccessful = fetchIndiaDataFromApi(context, refreshAction);



        //Toast.makeText(context, "load india data successful", Toast.LENGTH_LONG).show();
        if(fetchSuccessful) {
            loadIndiaStateListFromConfig(context);

            if(debug) {
                Log.d(TAG, " indiaStateListFromConfig size is \n" + indiaStateListFromConfig.size());
            }
            updateIndiaDataIntoIndiaDateItem(indiaStateListFromConfig);
            if(debug) {
                Log.d(TAG, " covidWidgetIndiaDataItems are \n");
            }
            printCovidWidgetItemClass(covidWidgetIndiaDataItemClasses);
            //Set the data to the widgets
            addDataToWidget(appWidgetIds, context, appWidgetManager, true);
        } else {
            Toast.makeText(context, "Please connect to network to get latest Covid data!", Toast.LENGTH_LONG).show();
        }
    }

    public static void loadIndiaStateListFromConfig(Context context) {
        SharedPreferences mSharedPreference1 =   PreferenceManager.getDefaultSharedPreferences(context);
        indiaStateListFromConfig.clear();
        int size = mSharedPreference1.getInt("State_List_size", 0);
        for(int i=0;i<size;i++) {
            String stateEntry = mSharedPreference1.getString("State_" + i, null);
            if((stateEntry != null) || !(stateEntry.equals(""))) {
                indiaStateListFromConfig.add(stateEntry);
            }
        }
    }

    public void printCovidWidgetItemClass(ArrayList<CovidWidgetDataItemClass> inputCovidWidgetDataItemClass) {
        if(inputCovidWidgetDataItemClass != null) {
            for(int i = 0; i < inputCovidWidgetDataItemClass.size(); i++) {
                if(debug) {
                    Log.d(TAG, "region(" + i + ") is " + inputCovidWidgetDataItemClass.get(i).getRegion());
                    Log.d(TAG, "totalCases(" + i + ") is " + inputCovidWidgetDataItemClass.get(i).getTotalCases());
                    Log.d(TAG, "totalDeaths(" + i + ") is " + inputCovidWidgetDataItemClass.get(i).getTotalDeaths());
                    Log.d(TAG, "newCases(" + i + ") is " + inputCovidWidgetDataItemClass.get(i).getNewCases());
                    Log.d(TAG, "newDeaths(" + i + ") is " + inputCovidWidgetDataItemClass.get(i).getNewDeaths());
                }
            }
        }
    }

    public static boolean fetchIndiaDataFromApi(Context context, boolean refreshAction) {
        final boolean[] fetchSuccess = {false};
        //Saving the response data from API
        final String[] getResponse = {"data"};

        //Little code to tell the user that they already have the latest data
        int indiaPrevTotalCases = -1;
        if(refreshAction) {
            if (indiaCovidData != null) {
                indiaPrevTotalCases = indiaCovidData.getActiveCases() + indiaCovidData.getRecovered() + indiaCovidData.getDeaths();
            }
        }

        //Initialize indiaCovidData here
        indiaCovidData = new IndiaCovidData();
        //Call the API
        OkHttpClient client = new OkHttpClient();

        //Note: The link below gets refreshed every 5 minutes. So I don't need to create my own actor and do a run there.
        Request request = new Request.Builder()
                .url("https://api.apify.com/v2/key-value-stores/toDWvRj1JpTXiM8FF/records/LATEST?disableRedirect=true")
                .get()
                .build();

        //Synchronization mechanism to wait for async threads to finish.
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    indiaCovidData = null;
                    throw new IOException("Unexpected response code " + response);
                } else {
                    getResponse[0] = response.body().string();

                    //Parse the json data
                    JSONObject json = null;
                    try {
                        json = new JSONObject(getResponse[0]);

                        indiaCovidData = IndiaCovidData.fromJSON(json);

                        if(debug) {
                            Log.d(TAG, "indiaCovidData updated on is " + indiaCovidData.getLastUpdatedAtApify() +
                                    " and first entry is: " + indiaCovidData.regionData.get(0).getRegion());
                        }

                        fetchSuccess[0] = true;
                        //Save data to an external file

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if(debug) {
                    Log.d(TAG, "request failed - no response");
                }
                e.printStackTrace();
                indiaCovidData = null;
                countDownLatch.countDown();
            }
        });

        //Wait for the async thread to finish
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Little code here to display to user that they already have the latest number
        //Compare to prev total cases
        if(refreshAction) {
            if (indiaPrevTotalCases != -1) {
                if ((indiaCovidData.getActiveCases() + indiaCovidData.getRecovered() + indiaCovidData.getDeaths()) == indiaPrevTotalCases) {
                    Toast.makeText(context, "The widget already has the latest numbers", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return fetchSuccess[0];
    }

    public void loadNextItem(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager) {

        //Increment the current widget item's position
        advanceToNextItemPosition();

        //Update the data
        addDataToWidget(appWidgetIds, context, appWidgetManager, false);


    }

    public void loadPrevItem(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager) {
        //Increment the current widget item's position
        if(currentWidgetItemPosition == 0) {
            currentWidgetItemPosition = covidWidgetIndiaDataItemClasses.size() - 1;
        } else {
            currentWidgetItemPosition = currentWidgetItemPosition - 1;
        }

        //Update the data
        addDataToWidget(appWidgetIds, context, appWidgetManager, false);
        //TODO: How to make widget work for multiple phones?
    }


     public void addDataToWidget(int[] appWidgetIds, Context context, AppWidgetManager appWidgetManager, boolean loadingNewDataSet) {
        for( int i=0 ; i<appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            finalUpdateToTheWidget(rv, context, appWidgetId, appWidgetManager, loadingNewDataSet);
        }
    }

    public static void finalUpdateToTheWidget(RemoteViews rv, Context context, int appWidgetId, AppWidgetManager
                                      appWidgetManager, boolean loadingNewSet) {


        //Initialize the data object in config, in case the config was not called.
        //Set data
        addDataToWidgetFields(rv, context, loadingNewSet);

        //PROVIDE INTERACTIVITY TO THE WIDGET
        //======================================================================================
        //Creating a templateIntent to tell what we do on a widget click
        // Create a Pending Intent template to provide interactivity to
        // each item displayed within the collection View.

        //widget detail button intent to take us to detail activity. Binding to MainActivity for now
        Intent clickWidgetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mohfw.gov.in/"));
        PendingIntent clickWidgetPendingIntent = PendingIntent.getActivity(context, 0, clickWidgetIntent, 0);
        rv.setOnClickPendingIntent(R.id.widget_india_flag_id, clickWidgetPendingIntent);

        //widget next button click register
        Intent nextButtonIntent = new Intent(context, CovidWidgetProvider.class);
        nextButtonIntent.setAction(nextButtonOnClickAction);
        PendingIntent nextButtonPendingIntent = PendingIntent.getBroadcast(context, 0, nextButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_next_item_id, nextButtonPendingIntent);

        //widget previous button click register
        Intent prevButtonIntent = new Intent(context, CovidWidgetProvider.class);
        prevButtonIntent.setAction(prevButtonOnClickAction);
        PendingIntent prevButtonPendingIntent = PendingIntent.getBroadcast(context, 0, prevButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_previous_item_id, prevButtonPendingIntent);

        //widget refresh button click register
        //refresh widget button to handle the case when user has had phone out of coverage for many days and wants to see the latest data
        Intent refreshButtonIntent = new Intent(context, CovidWidgetProvider.class);
        refreshButtonIntent.setAction(loadIndiaDataIntoAllWidgetInstances);
        refreshButtonIntent.putExtra(context.getString(R.string.refresh_action_intent), true);
        PendingIntent refreshButtonPendingIntent = PendingIntent.getBroadcast(context, 0, refreshButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_refresh_item_id, refreshButtonPendingIntent);

        //widget addState button click register
        //addState button to add open the config activity from the widget and add new states
        Intent addStateButtonIntent = new Intent(context, CovidWidgetConfig.class);
        addStateButtonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent addStateButtonPendingIntent = PendingIntent.getActivity(context, 0, addStateButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_add_state_item_id, addStateButtonPendingIntent);

        if(covidWidgetIndiaDataItemClasses.size() == 1) {
            rv.setViewVisibility(R.id.widget_next_item_id, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_previous_item_id, View.INVISIBLE);
        } else {
            rv.setViewVisibility(R.id.widget_next_item_id, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_previous_item_id, View.VISIBLE);
        }
        //======================================================================================
        // Notify the App Widget Manager to update the widget using
        // the modified remote view.
        appWidgetManager.updateAppWidget(appWidgetId, rv);

    }

    public static void addDataToWidgetFields(RemoteViews rv, Context context, boolean loadingNewDataSet) {
        if((indiaCovidData != null && indiaCovidData.regionData.size() != 0)) {

            int indiaNewCases = (indiaCovidData.getActiveCasesNew()
                                + indiaCovidData.getDeathsNew()
                                + indiaCovidData.getRecoveredNew());
            rv.setCharSequence(R.id.widget_country_state_name_id, "setText", "India");
            rv.setCharSequence(R.id.widget_total_cases_value_id, "setText", "" + convertNumberToIndiaFormat(indiaCovidData.getTotalCases()));
            rv.setCharSequence(R.id.widget_total_deaths_value_id, "setText", "" + convertNumberToIndiaFormat(indiaCovidData.getDeaths()));

            rv.setCharSequence(R.id.widget_new_cases_value_id, "setText", "" + convertNumberToIndiaFormat(indiaNewCases));
            rv.setCharSequence(R.id.widget_new_deaths_value_id, "setText", "" + convertNumberToIndiaFormat(indiaCovidData.getDeathsNew()));

        }

        if(indiaCovidData == null) {
            if(debug) {
                Log.d(TAG, "india data is empty");
            }
        }
        if((covidWidgetIndiaDataItemClasses != null) && (covidWidgetIndiaDataItemClasses.size() != 0)) {
            if(loadingNewDataSet) {
                currentWidgetItemPosition = 0;
            }
            if (covidWidgetIndiaDataItemClasses.size() > 0) {
                rv.setCharSequence(R.id.widget_country_state_name_state_id, "setText", covidWidgetIndiaDataItemClasses.get(currentWidgetItemPosition).getRegion());
                rv.setCharSequence(R.id.widget_total_cases_value_state_id, "setText", "" + 0);
                rv.setCharSequence(R.id.widget_total_cases_value_state_id, "setText", "" + convertNumberToIndiaFormat(covidWidgetIndiaDataItemClasses.get(currentWidgetItemPosition).getTotalCases()));
                rv.setCharSequence(R.id.widget_total_deaths_value_state_id, "setText", "" + convertNumberToIndiaFormat(covidWidgetIndiaDataItemClasses.get(currentWidgetItemPosition).getTotalDeaths()));
                rv.setCharSequence(R.id.widget_new_cases_value_state_id, "setText", "" + convertNumberToIndiaFormat(covidWidgetIndiaDataItemClasses.get(currentWidgetItemPosition).getNewCases()));
                rv.setCharSequence(R.id.widget_new_deaths_value_state_id, "setText", "" + convertNumberToIndiaFormat(covidWidgetIndiaDataItemClasses.get(currentWidgetItemPosition).getNewDeaths()));
            }

        } else {
            if(debug) {
                Log.d(TAG, "covidWidgetIndiaDataItemClasses is empty");
            }
        }
    }

    //TODO: Tell people how to add a new state to the list


    public static String convertNumberToIndiaFormat(int inNum) {
        String outNumStr="";
        int digitCntr = 0;
        if(inNum == 0) {
            return (""+inNum);
        } else {
            while (inNum != 0) {
                outNumStr = (inNum % 10) + (((digitCntr == 3) || (digitCntr == 5) || (digitCntr == 7) || (digitCntr == 9)) ? "," : "") + outNumStr;
                inNum = inNum / 10;
                digitCntr++;
            }

            return outNumStr;
        }
    }
    public static void updateIndiaDataIntoIndiaDateItem(ArrayList<String> stateList) {
            //clear the india state list with old states
        covidWidgetIndiaDataItemClasses.clear();

        if((stateList != null) && (stateList.size() != 0)) {
            //add the new selected items
            for(int i = 0; i < stateList.size(); i++) {
                for (int indiaStateDataIndex = 0; indiaStateDataIndex < indiaCovidData.regionData.size(); indiaStateDataIndex++) {
                    if(stateList.get(i).equals(indiaCovidData.regionData.get(indiaStateDataIndex).getRegion())) {
                        CovidWidgetDataItemClass funcCovidWidgetDataItemClass = new CovidWidgetDataItemClass();
                        funcCovidWidgetDataItemClass.setRegion(indiaCovidData.regionData.get(indiaStateDataIndex).getRegion());
                        funcCovidWidgetDataItemClass.setTotalDeaths(indiaCovidData.regionData.get(indiaStateDataIndex).getDeceased());
                        funcCovidWidgetDataItemClass.setNewDeaths(indiaCovidData.regionData.get(indiaStateDataIndex).getNewDeceased());
                        funcCovidWidgetDataItemClass.setTotalCases(indiaCovidData.regionData.get(indiaStateDataIndex).getTotalInfected()
                                + indiaCovidData.regionData.get(indiaStateDataIndex).getRecovered()
                                + indiaCovidData.regionData.get(indiaStateDataIndex).getDeceased());
                        funcCovidWidgetDataItemClass.setNewCases(indiaCovidData.regionData.get(indiaStateDataIndex).getNewRecovered()
                                + indiaCovidData.regionData.get(indiaStateDataIndex).getNewDeceased()
                                + indiaCovidData.regionData.get(indiaStateDataIndex).getNewInfected());

                        covidWidgetIndiaDataItemClasses.add(funcCovidWidgetDataItemClass);
                    }
                }
            }
        } else {
            if(debug) {
                Log.d(TAG, "State List empty \n");
            }
        }

    }

    @Override
    public void onEnabled(Context context) {
        //Only called when the first ever widget is put on homescreen
        //It is not called for any subsequent widget instances

        if(debug) {
            Log.d(TAG, "jassi Called onEnabled");
        }
        setCountryDataAndAlarmForWidget(context, setIndiaData);
        super.onEnabled(context);
    }

    public static void setCountryDataAndAlarmForWidget(Context context, String inputAction) {
        Intent fetchDataIntent = new Intent(context, CovidWidgetProvider.class);
        fetchDataIntent.setAction(inputAction);
        context.sendBroadcast(fetchDataIntent);
    }

    public void setAlarmToUpdateCountryData(Context context
            , int destDataFetchHour
            , int destDataFetchMin
            , int destDataFetchSec
            , String inputAction
            , String inputCountryName
            , boolean cancelAlarm) {

        //==========================================================================================
        //Calculate the time zone offset in seconds from local to destination timeZone
        int destinationOffsetInSec = calculateLocalToDestCountyTimeOffset(context, inputCountryName);
        //If somehow, the country passed in is not found, default to India
        if(destinationOffsetInSec == CountryOffsetNullValue) {
            inputCountryName = context.getString(R.string.country_india);
            destinationOffsetInSec = calculateLocalToDestCountyTimeOffset(context, inputCountryName);
        }

        //==========================================================================================
        //Save the destination time in seconds.
        long dataRefreshAlarmTimeInSec = (destDataFetchHour*3600) + (destDataFetchMin*60) + destDataFetchSec;

        if(debug) {
            Log.d(TAG, "refresh time before zone adjustment " + dataRefreshAlarmTimeInSec);
        }
        //Add/subtract the offset from it, to get the local time second you want to set the alarm to
        dataRefreshAlarmTimeInSec +=destinationOffsetInSec;
        if(debug) {
            Log.d(TAG, "refresh time after zone offset is " + dataRefreshAlarmTimeInSec);
        }


        //==========================================================================================

        int totalSecInDay = 24*3600;
        if(dataRefreshAlarmTimeInSec < 0) {
            //if time is negative, it means that we are falling in the previous day in the local timezone.
            // So calculate the time of the previous day
            dataRefreshAlarmTimeInSec = totalSecInDay + dataRefreshAlarmTimeInSec;
        } else if(dataRefreshAlarmTimeInSec > totalSecInDay){
            //if time is more that totalSecInDay, it is falling to the next day.
            dataRefreshAlarmTimeInSec = dataRefreshAlarmTimeInSec - totalSecInDay;
        }

        if(debug) {
            Log.d(TAG, "refresh time is " + dataRefreshAlarmTimeInSec);
        }
        //==========================================================================================
        //Calendar for current time
        Calendar currentCal = Calendar.getInstance();
        //Calendar for setting the alarm
        Calendar alarmCal=Calendar.getInstance();
        alarmCal.set(Calendar.HOUR_OF_DAY, (int) (dataRefreshAlarmTimeInSec/3600));
        alarmCal.set(Calendar.MINUTE, (int) ((dataRefreshAlarmTimeInSec%3600)/ 60));
        alarmCal.set(Calendar.SECOND,0);

        //Log.d(TAG, "alarm calendar minute is "+alarmCalMin);
        //Log.d(TAG, "alarm calendar hour is" + alarmCal.get(Calendar.HOUR_OF_DAY));

        // Any alarm set for a time in the past is triggered immediately
        //if the calculated refresh time is behind the current time, set the alarm for the next day.
        if(alarmCal.getTimeInMillis() <= currentCal.getTimeInMillis()) {
            alarmCal.add(Calendar.DATE,1);
        }

        if(debug) {
            Log.d(TAG, "Setting next refresh alarm for " + alarmCal.getTime());
        }
        //==========================================================================================


        //Setup the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent contentChangeAlarmIntent = new Intent(context, CovidWidgetProvider.class);
        contentChangeAlarmIntent.setAction(inputAction);
        PendingIntent pendingContentChangeAlarmIntent = PendingIntent.getBroadcast(context, 0, contentChangeAlarmIntent, 0);

        if(!cancelAlarm) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), pendingContentChangeAlarmIntent);
        } else {
            alarmManager.cancel(pendingContentChangeAlarmIntent);
        }
    }

    public int calculateLocalToDestCountyTimeOffset(Context context, String destinationCountry) {

        if(context.getString(R.string.country_india).equals(destinationCountry)) {
            //Set the exact repeating alarm here to fetch the new data every day
            //The mohfw.gov.in gets updated at 8am everyday IST. Setting the alarm to fetch daily data at 8.05am IST


            // get the supported ids for GMT+05:30 (Pacific Standard Time)
            //time at UTC+/-0
            LocalDateTime now = LocalDateTime.now();
            //Set zone to india timezone
            ZoneId zone = ZoneId.of("Asia/Calcutta");
            //get offset of india from UTC+/-0
            ZoneOffset indiaZoneOffset = zone.getRules().getOffset(now);

            //Get current timezone's offset
            OffsetDateTime odt = OffsetDateTime.now ();
            ZoneOffset localOffset = odt.getOffset ();

            //offset of local zone to India
            //minimum difference between two adjacent timezones is 15 min. Maximum difference is 26 hours
            int totalOffsetFromLocalToIndia = (localOffset.getTotalSeconds() - indiaZoneOffset.getTotalSeconds());

            if(debug) {
                //int totalOffsetFromLocalToIndiaInMin = totalOffsetFromLocalToIndia/60;
                Log.d(TAG, "offset from local to india" + totalOffsetFromLocalToIndia);
            }
            return totalOffsetFromLocalToIndia;
        } else {
            return CountryOffsetNullValue;
        }

    }

    @Override
    public void onDisabled(Context context) {

        //VIceversa, onDisabled() is only called when we delete our last widget from homescreen
        /*
        Intent contentChangeCancelAlarmIntent = new Intent(context, CovidWidgetProvider.class);
        PendingIntent pendingContentChangeCancelAlarmIntent = PendingIntent.getBroadcast(context, 0, contentChangeCancelAlarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingContentChangeCancelAlarmIntent);

         */
        setAlarmToUpdateCountryData(context, IndiaDataRefreshHourOfDay, IndiaDataRefreshMinute,
                IndiaDataRefreshSecond, setIndiaData,
                context.getString(R.string.country_india), true
        );
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // deactivate the alarm that moves the widget content

        super.onDeleted(context, appWidgetIds);
    }



    //TODO in future: Make a github repository and/or youtube video for network connection class based on API-29 Connectivity Manager and registerDefaultNetworkCallback()
    //https://gist.github.com/Abhinav1217/0ff6b39e70fa38379d61e85e09b49fe7
}
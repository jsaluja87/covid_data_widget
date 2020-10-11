package com.saluja_apps.covid_widget.widgets;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.saluja_apps.covid_widget.GlobalVariables;
import com.saluja_apps.covid_widget.InternetChecker.CheckInternetClass;
import com.saluja_apps.covid_widget.MainActivity;
import com.saluja_apps.covid_widget.R;
import com.saluja_apps.covid_widget.model.ConfigListViewItem;

import java.util.ArrayList;

import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.debug;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.fetchIndiaDataFromApi;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.finalUpdateToTheWidget;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.indiaCovidData;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.indiaStateListFromConfig;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.loadIndiaDataIntoAllWidgetInstances;
import static com.saluja_apps.covid_widget.widgets.CovidWidgetProvider.updateIndiaDataIntoIndiaDateItem;

public class CovidWidgetConfig extends AppCompatActivity {


    private static final String TAG = "Widget Config";

    //for talking to the widget
    public static final String SHARED_PREF = "com.saluja_apps.covid_widget.widgets.CovidWidgetConfig.sharedPrefs";
    public static final String SAVE_BUTTON_SPINNER_TEXT = "com.saluja_apps.covid_widget.widgets.CovidWidgetConfig.saveButtonSpinnerText";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static final String WIDGET_CONFIG_CALLED = "com.saluja_apps.covid_widget.widgets.CovidWidgetConfig.widgetConfigCalledString";


    ListView listView;
    CustomListViewAdapter customListViewAdapter;
    static ArrayList<ConfigListViewItem> customListViewAdapterArray = new ArrayList();

    //list to store the list of states stored by user in previous widget instance
    ArrayList<String> savedStateList = new ArrayList<>();

    public static ArrayList<String> indiaStateList = new ArrayList<>();


    Button saveButton;

    CheckInternetClass checkInternetClass;


    //Class with data
   // ArrayList<WidgetOutlookDataItemClass> widgetOutlookDataClass = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        saveButton = findViewById(R.id.widget_config_save_button_id);


        //Register the NetWorkCallback when first widget is added
        checkInternetClass = new CheckInternetClass(CovidWidgetConfig.this);
        //Check if there is internet
        checkInternetClass.registerDefaultNetworkCallback();

        populateDataInListViewArray();
        initListView();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmConfig();
            }
        });
        //The configuration activity started when we place our widget on to the home screen.
        // So we want to retrieve the ID of that widget. We get the intent below
        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if(extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //In case the user exits without setting the preferences. Generally not required, but launcher crashes on some
        //phones if we end the activity without sending the appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        //Finish the activity if no ID retrieved(Something went wrong)
        if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

    }

    public void populateDataInListViewArray() {
        //Don't load the data if the list is already populated. This is needed when second and more instance of widgets are loaded
        if(customListViewAdapterArray.size() == 0) {
            //fetch the data for India
            boolean fetchSuccessful = fetchIndiaDataFromApi(this, false);
            if (fetchSuccessful) {
                //Construct the state array from the API
                for (int i = 0; i < indiaCovidData.regionData.size(); i++) {
                    ConfigListViewItem configListViewItem = new ConfigListViewItem();
                    configListViewItem.setStateClickedState(false);
                    configListViewItem.setStateName(indiaCovidData.regionData.get(i).getRegion());
                    customListViewAdapterArray.add(configListViewItem);
                }
            } else {
                //use the default array
                String[] indiaStates = getResources().getStringArray(R.array.india_predefined_state_array);
                for (int i = 0; i < indiaStates.length; i++) {
                    ConfigListViewItem configListViewItem = new ConfigListViewItem();
                    configListViewItem.setStateClickedState(false);
                    configListViewItem.setStateName(indiaStates[i]);
                    customListViewAdapterArray.add(configListViewItem);
                }
            }
            //Fetch the persisted state list
            SharedPreferences mSharedPref =   PreferenceManager.getDefaultSharedPreferences(this);
            savedStateList.clear();
            int size = mSharedPref.getInt("State_List_size", 0);
            for(int i=0;i<size;i++) {
                String stateEntry = mSharedPref.getString("State_" + i, null);
                if(stateEntry != null) {
                    if(!stateEntry.equals("")) {
                        savedStateList.add(stateEntry);
                    }
                }
            }

            //Change the Clicked state for entries which were previous selected by user
            if(savedStateList.size() > 0) {
                for (int i = 0; i < customListViewAdapterArray.size(); i++) {
                    for (int s = 0; s < savedStateList.size(); s++) {
                        if (customListViewAdapterArray.get(i).getStateName().equals(savedStateList.get(s))) {
                            customListViewAdapterArray.get(i).setStateClickedState(true);
                            break;
                        }
                    }
                }
            }

        }
    }

    public void initListView() {
        listView = findViewById(R.id.config_list_view_id);
        customListViewAdapter = new CustomListViewAdapter(customListViewAdapterArray, this);
        listView.setAdapter(customListViewAdapter);
    }

    public void confirmConfig() {

        if(!GlobalVariables.isNetworkConnected) {
            popAlertDialogue("Oops!! Please check your Internet Connection!");
        } else {
            //go through the states to make sure atleast one state is selected
            boolean anyStateSelected = false;
            for (int i = 0; i < customListViewAdapterArray.size(); i++) {
                if(customListViewAdapterArray.get(i).isStateClickedState()) {
                    anyStateSelected = true;
                    break;
                }
            }
            if(!anyStateSelected) {
                popAlertDialogue("Please select atleast one state");
            } else {

                //Bridge our app to widget using remote view
                RemoteViews rv = new RemoteViews(this.getPackageName(), R.layout.widget_layout);

                //Config Button save Intent for click on listener

                if(debug) {
                    Log.d(TAG, "jassi Called confirmConfig \n");
                }
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                Intent saveIntent = new Intent(this, MainActivity.class);
                PendingIntent savePendingIntent = PendingIntent.getActivity(this, 0, saveIntent, 0);
                rv.setOnClickPendingIntent(R.id.widget_config_save_button_id, savePendingIntent);

                //==========================================================================================

                //Set the components in the widget
                //If we want to change something related to a view or textview, button etc in the widget. We use rv.setString/Int/etc
                //based on the attribute, use id of the view, literally write the java function name as a string and pass the value

                //first, clear the old state list
                indiaStateList.clear();

                //go through the items to get the checked item state
                for (int i = 0; i < customListViewAdapterArray.size(); i++) {
                    if (customListViewAdapterArray.get(i).isStateClickedState()) {
                        indiaStateList.add(customListViewAdapterArray.get(i).getStateName());
                    }
                }
                if(debug) {
                    Log.d(TAG, "jassi selected state entries are \n");
                }
                printStatesSelectedFromConfig();

                //Persist the indiaStateFromConfig
                persistStateList(indiaStateList);
                //======================================================================================


                //Update the data into the covid india array for the widget
                updateIndiaDataIntoIndiaDateItem(indiaStateList);

                //update the widget
                finalUpdateToTheWidget(rv, this, appWidgetId, appWidgetManager, true);

                //Force a data refresh of all widget instances
                Intent fetchDataIntent = new Intent(this, CovidWidgetProvider.class);
                fetchDataIntent.setAction(loadIndiaDataIntoAllWidgetInstances);
                this.sendBroadcast(fetchDataIntent);

                //finish the activity
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        }
    }

    private void popAlertDialogue(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CovidWidgetConfig.this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void persistStateList(ArrayList<String> stateList) {

        //SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sp =   PreferenceManager.getDefaultSharedPreferences(CovidWidgetConfig.this);
        SharedPreferences.Editor mEdit1 = sp.edit();
        /* sKey is an array */
        mEdit1.putInt("State_List_size", stateList.size());

        for(int i=0;i<stateList.size();i++) {
            mEdit1.remove("State_" + i);
            mEdit1.putString("State_" + i, stateList.get(i));
        }
        mEdit1.commit();
    }

    public static void updateListViewItemState(boolean itemChecked, int itemPosition) {
        customListViewAdapterArray.get(itemPosition).setStateClickedState(itemChecked);
    }

    public static void printStatesSelectedFromConfig() {
        for(int i = 0; i < indiaStateListFromConfig.size(); i++) {
            if(debug) {
                Log.d(TAG, "jassi " + indiaStateListFromConfig.get(i));
            }
        }
    }


}
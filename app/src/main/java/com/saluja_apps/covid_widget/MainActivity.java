package com.saluja_apps.covid_widget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.saluja_apps.covid_widget.R;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_REFRESH = "RefreshWidget";
    public static final String TAG = "MainActivity";
    TextView srcText;
    TextView widgetInstallText;
    TextView widgetMainText;
    Button widgetMainDoneButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        widgetMainText = findViewById(R.id.mainWidgetTextViewId);
        widgetInstallText = findViewById(R.id.mainWidgetInstallTextViewId);
        srcText = findViewById(R.id.mainSourceTextViewId);
        widgetMainDoneButton = findViewById(R.id.mainDoneButtonId);

        widgetMainText.setText("Get the latest data for the covid cases and deaths for India and the Indian states. Click the flag anytime to go to https://www.mohfw.gov.in/ \n");
        widgetInstallText.setText("    \u2022 Touch and hold on the home screen, then tap Widgets option on the screen. \n    \u2022 Scroll down to \"covid widget\" and drag it on the home screen. \n    \u2022 Select the Indian States of your choice. \n    \u2022 See the latest data on the widget everyday. \n");
        srcText.setText("Data based on Ministry of Healthcare and Family welfare website of Indian Government.");

        widgetMainDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish the activity
                Intent resultValue = new Intent();
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }
}

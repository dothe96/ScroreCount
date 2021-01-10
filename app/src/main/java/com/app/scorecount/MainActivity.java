package com.app.scorecount;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.app.scorecount.database.AppDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        AppWidgetManager appWidgetManager =
                this.getSystemService(AppWidgetManager.class);
        ComponentName myProvider =
                new ComponentName(this, HandleWidget.class);

        if (appWidgetManager.isRequestPinAppWidgetSupported()) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null);
            Log.d("Main", "isRequestPinAppWidgetSupported: true");
        } else {
            Log.d("Main", "isRequestPinAppWidgetSupported: false");
        }
    }
}
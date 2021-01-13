package com.app.scorecount;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.room.Room;

import com.app.scorecount.database.AppDatabase;
import com.app.scorecount.database.StoreLevelsCount;
import com.app.scorecount.database.StoreLevelsCountDAO;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HandleWidget extends AppWidgetProvider {

    private final static String TAG = "HandleWidget";
    private final static String DOWN_CLICK = "HandleWidget.DOWN_CLICK";
    private final static String NORMAL_CLICK = "HandleWidget.NORMAL_CLICK";
    private final static String UP_CLICK = "HandleWidget.UP_CLICK";
    private final static String PREFS_NAME = "CHECK_CLICKED";
    private final static String SETTINGS_TAG = "HandleWidget.CheckClicked";
    private final static int DOWN = -1;
    private final static int NORMAL = 0;
    private final static int UP = 1;

    private AppDatabase appDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(DOWN_CLICK) || intent.getAction().equals(NORMAL_CLICK) || intent.getAction().equals(UP_CLICK)) {
            buttonClick(context, intent.getAction());
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        StoreLevelsCountDAO storeLevelsCountDAO = getAppDatabase(context).getStoreLevelsCountDAO();
        List<StoreLevelsCount> allDaysRecord = storeLevelsCountDAO.getAll();
        int levels = 0;
        int progress = 0;
        boolean isRatedTime = isRatedTime(context);
        boolean isClickedToday = getValuesSettings(SETTINGS_TAG, getSettings(context));
        levels = calculateLevels(allDaysRecord);
        progress = calculateProgress(levels);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        if (isRatedTime && isClickedToday) {
            views.setViewVisibility(R.id.btn_range, View.VISIBLE);
            views.setOnClickPendingIntent(R.id.btn_down, getPendingIntent(DOWN, context));
            views.setOnClickPendingIntent(R.id.btn_normal, getPendingIntent(NORMAL, context));
            views.setOnClickPendingIntent(R.id.btn_up, getPendingIntent(UP, context));
        } else {
            views.setViewVisibility(R.id.btn_range, View.GONE);
        }
        updateText(views, context, isRatedTime, isClickedToday, levels);
        views.setProgressBar(R.id.activeProgress, 100, progress, false);
        Log.d(TAG, "onUpdate(): levels = " + levels + " progress = " + progress +
                " isRatedTime = " + isRatedTime + " isClickedToday = " + isClickedToday);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private PendingIntent getPendingIntent(int type, Context context) {
        Intent intent = new Intent(context, getClass());
        if (type == DOWN) {
            intent.setAction(DOWN_CLICK);
        } else if (type == NORMAL) {
            intent.setAction(NORMAL_CLICK);
        } else if (type == UP) {
            intent.setAction(UP_CLICK);
        }
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private int calculateLevels(List<StoreLevelsCount> allDaysRecord) {
        int levels = 0;
        for (StoreLevelsCount count : allDaysRecord) {
            if(count.getState() == 1) {
                levels++;
            }
            if(count.getState() == -1) {
                levels--;
            }
        }
        return levels;
    }

    private void buttonClick(Context context, String typeClick) {
        if (typeClick.equals(DOWN_CLICK)) {
            Log.d(TAG, "buttonClick: " + DOWN_CLICK);
            StoreLevelsCount storeLevelsCount = new StoreLevelsCount();
            storeLevelsCount.setName("down");
            storeLevelsCount.setState(DOWN);
            storeLevelsCount.setDate(Calendar.getInstance().getTime().toString());
            StoreLevelsCountDAO storeLevelsCountDAO = getAppDatabase(context).getStoreLevelsCountDAO();
            storeLevelsCountDAO.insert(storeLevelsCount);
            commitValuesSettings(SETTINGS_TAG, false, getSettings(context));
        } else if (typeClick.equals(NORMAL_CLICK)) {
            Log.d(TAG, "buttonClick: " + NORMAL_CLICK);
            StoreLevelsCount storeLevelsCount = new StoreLevelsCount();
            storeLevelsCount.setName("normal");
            storeLevelsCount.setState(NORMAL);
            storeLevelsCount.setDate(Calendar.getInstance().getTime().toString());
            StoreLevelsCountDAO storeLevelsCountDAO = getAppDatabase(context).getStoreLevelsCountDAO();
            storeLevelsCountDAO.insert(storeLevelsCount);
            commitValuesSettings(SETTINGS_TAG, false, getSettings(context));

        } else if (typeClick.equals(UP_CLICK)) {
            Log.d(TAG, "buttonClick: " + UP_CLICK);
            StoreLevelsCount storeLevelsCount = new StoreLevelsCount();
            storeLevelsCount.setName("up");
            storeLevelsCount.setState(UP);
            storeLevelsCount.setDate(Calendar.getInstance().getTime().toString());
            StoreLevelsCountDAO storeLevelsCountDAO = getAppDatabase(context).getStoreLevelsCountDAO();
            storeLevelsCountDAO.insert(storeLevelsCount);
            commitValuesSettings(SETTINGS_TAG, false, getSettings(context));
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, HandleWidget.class));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void updateText(RemoteViews views, Context context, boolean isRatedTime, boolean isClickedToday, int levels) {
        views.setTextViewText(R.id.levels_text, "Levels " + levels);
        views.setTextViewText(R.id.updated_text, getRandomText(context, isRatedTime, isClickedToday));
    }

    private int calculateProgress(int levels) {
        return (int)(((float) (levels / 365f /*1 year for better*/)) * 100f);
    }

    private String getRandomText(Context context, boolean isRatedTime, boolean isClickedToday) {
        String text;
        String[] array;
        if (isRatedTime && isClickedToday) {
            array = context.getResources().getStringArray(R.array.rated_time);
        } else {
            array = context.getResources().getStringArray(R.array.normal_time);
        }
        text = array[new Random().nextInt(array.length)];
        return text;
    }

    private AppDatabase getAppDatabase(Context context) {
        if (appDatabase == null) {
            appDatabase = setupDatabase(context);
        }
        return  appDatabase;
    }

    private AppDatabase setupDatabase(Context context) {
        AppDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "LevelsCount").allowMainThreadQueries()
                .fallbackToDestructiveMigration().build();
        return db;
    }

    private boolean isRatedTime(Context context) {
        int start = 21;
        int end = 24;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, start);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long currentHourMilli = System.currentTimeMillis();
        long startHourMilli = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, end - start);
        long endHourMilli = cal.getTimeInMillis();

        if (currentHourMilli > endHourMilli) {
            // reset clicked for new day
            commitValuesSettings(SETTINGS_TAG, true, getSettings(context));
        }

        return currentHourMilli > startHourMilli && currentHourMilli < endHourMilli;
    }

    private SharedPreferences getSettings(Context context) {
        return  context.getSharedPreferences(PREFS_NAME, 0 );
    }

    private void commitValuesSettings(String tag, boolean value, SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(tag, value);
        editor.commit();
    }

    private boolean getValuesSettings(String tag, SharedPreferences settings) {
        return settings.getBoolean(tag, false);
    }
}
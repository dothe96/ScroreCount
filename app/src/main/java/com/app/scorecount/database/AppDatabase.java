package com.app.scorecount.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {StoreLevelsCount.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract StoreLevelsCountDAO getStoreLevelsCountDAO();
}

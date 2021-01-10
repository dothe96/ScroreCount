package com.app.scorecount.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StoreLevelsCountDAO {
    @Insert
    public void insert(StoreLevelsCount... storeLevelsCounts);

    @Update
    public void update(StoreLevelsCount... storeLevelsCounts);

    @Delete
    public void delete(StoreLevelsCount storeLevelsCount);

    @Query("SELECT * FROM LevelsCount")
    public List<StoreLevelsCount> getAll();
}

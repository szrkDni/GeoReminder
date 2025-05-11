package com.example.gmapapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GMapDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertItem(GMapItem item);

    @Query("SELECT * FROM GMDB")
    public LiveData<GMapItem> getItem();

    @Query("DELETE FROM GMDB")
    public void clearDB();



}

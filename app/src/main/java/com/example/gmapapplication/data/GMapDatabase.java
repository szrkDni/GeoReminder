package com.example.gmapapplication.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {GMapItem.class}, version = 1, exportSchema = false)
public abstract class GMapDatabase extends RoomDatabase {
    public abstract GMapDAO gmapDAO();

}

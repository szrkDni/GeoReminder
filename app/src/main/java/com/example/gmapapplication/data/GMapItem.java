package com.example.gmapapplication.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "GMDB")
public class GMapItem {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String city;

    @Override
    public String toString() {
        return id + " - " + city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

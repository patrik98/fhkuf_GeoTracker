package com.example.geotracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities =  {GeoPosition.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract GeoPositionDAO getGeoPositionDAO();
}

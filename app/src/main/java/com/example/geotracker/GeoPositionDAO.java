package com.example.geotracker;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;

@Dao
public interface GeoPositionDAO
{
    @Query("SELECT * FROM geoposition ORDER BY timestamp ASC")
    List<GeoPosition> getAllGeoPositions();

    @Insert
    void insertGeoPosition (GeoPosition geoPosition);

    @Delete
    void deleteGeoPosition (GeoPosition geoPosition);


}

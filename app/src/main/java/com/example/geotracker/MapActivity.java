package com.example.geotracker;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    MapView map = null;
    AppDatabase database;
    GeoPositionDAO geoPositionDAO;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = Room.databaseBuilder(this, AppDatabase.class, "geoposition_db")
                .allowMainThreadQueries()
                .build();

        geoPositionDAO = database.getGeoPositionDAO();

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        map = (MapView) findViewById(R.id.map);
        //Verwendung von Mapnik
        map.setTileSource(TileSourceFactory.MAPNIK);
        //Standard Zoom-Stufe festlegen
        map.getController().setZoom(19f);
        //Benutzung mit zwei Fingern erlauben
        map.setMultiTouchControls(true);

        //Alle GeoPositions aus der Room-DB werden in eine Liste gespeichert
        ArrayList<GeoPosition> geoPositions = new ArrayList<>();
        geoPositions.addAll(geoPositionDAO.getAllGeoPositions());

        //Neue Polyline wird erstellt
        Polyline line = new Polyline();

        //Startpunkt wird festgelegt (aus erstem GeoPoint in der Liste geoPositions)
        double startLat = geoPositions.get(0).getLatitude();
        double startLong = geoPositions.get(0).getLongitude();
        GeoPoint startPoint = new GeoPoint(startLat, startLong);

        //Endpunkt wird festgelegt (aus letztem GeoPoint in der Liste geoPositions)
        double endLat = geoPositions.get(geoPositions.size()-1).getLatitude();
        double endLong = geoPositions.get(geoPositions.size()-1).getLongitude();
        GeoPoint endPoint = new GeoPoint(endLat, endLong);

        //Da Polyline nur mit GeoPoints und nicht unseren GeoPositions arbeiten kann, muss
        //man GeoPoints aus den GeoPositions in der Liste geoPositions machen
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();

        for (GeoPosition g : geoPositions){
            GeoPoint geoPoint = new GeoPoint(g.getLatitude(), g.getLongitude());
            geoPoints.add(geoPoint);
        }

        line.setPoints(geoPoints);
        map.getOverlayManager().add(line);

        //Marker für Start- und Endpunkt setzen mit Titel
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Startpunkt");
        map.getOverlays().add(startMarker);

        Marker endMarker = new Marker(map);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle("Endpunkt");
        map.getOverlays().add(endMarker);

        //Zentrierung der Map auf den ersten Punkt unserer Track bzw. Polyline
        map.getController().setCenter(startPoint);

    }

    public void onResume(){
        super.onResume();

        map.onResume();
    }

    public void onPause(){
        super.onPause();

        map.onPause();
    }
}

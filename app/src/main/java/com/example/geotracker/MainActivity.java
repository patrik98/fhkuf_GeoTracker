package com.example.geotracker;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    AppDatabase database;
    GeoPositionDAO geoPositionDAO;
    GeoPosition geoPosition = new GeoPosition();

    LocationManager locationManager;
    LocationListener locationListener;

    java.util.Date today;
    java.sql.Timestamp ts1;

    Button btnStart;
    Button btnShow;
    TextView coords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialisierung der Datenbank
        database = Room.databaseBuilder(this, AppDatabase.class, "geoposition_db")
                .allowMainThreadQueries()
                .build();

        btnStart = findViewById(R.id.btnStart);
        btnShow = findViewById(R.id.btnShow);
        coords = findViewById(R.id.txtK);

        btnStart.setTag("isReady");

        btnStart.setOnClickListener(this);
        btnShow.setOnClickListener(this);

        geoPositionDAO = database.getGeoPositionDAO();

        //Prüfe, ob bereits GeoPoints in der Tabelle existieren
        if (geoPositionDAO.getAllGeoPositions().get(0) != null){
            btnShow.setEnabled(true);
        } else {
            btnShow.setEnabled(false);
        }

        //Initialisierung des SystemServices LOCATION_SERVICE über den LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Abfrage der GPS Permission während der Laufzeit
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == btnStart.getId()) {
            if (v.getTag() == "isReady") {

                //Leeren aller Tabellen in der TB, damit immer nur eine Track gespeichert wird
                database.clearAllTables();

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        //Generierung eines Timestamps
                        today = new java.util.Date();
                        ts1 = new java.sql.Timestamp(today.getTime());
                        long timestamp;
                        timestamp = ts1.getTime();

                        geoPosition.setLatitude(location.getLatitude());
                        geoPosition.setLongitude(location.getLongitude());
                        geoPosition.setTimestamp(timestamp);

                        //Speicherung der Koordinaten in die DB
                        geoPositionDAO.insertGeoPosition(geoPosition);

                        //TextView coords zeigt die aktuellen Koordinaten
                        coords.setText("Letzte Position: " + location.getLatitude() + " / " + location.getLongitude());
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        //Wenn GPS aus ist, wird entsprechend reagiert
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                };

                try {
                    //Alle 5 Sekunden (5000 ms) wird über den locationListener ein Standortupdate abgefragt
                    locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                } catch (SecurityException ex){
                    Toast toast = Toast.makeText(this, ""+ex.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }

                Toast toast = Toast.makeText( this, "Tracking gestartet", Toast.LENGTH_SHORT);
                toast.show();

                //Jetzt ist der Button, nicht mehr "isReady" sondern "isTracking", der Text des Buttons ändert sich
                btnStart.setText("Tracking stoppen");
                btnStart.setTag("isTracking");
                btnShow.setEnabled(false);
            }

            else {
                btnStart.setText("Tracking starten");
                coords.setText("");
                btnStart.setTag("isReady");
                //Wenn das Tracking gestoppt wird, sendet der locationListener keine Standortupdates mehr
                locationManager.removeUpdates(locationListener);
                btnShow.setEnabled(true);

                Toast toast = Toast.makeText( this, "Tracking gestoppt", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if (v.getId() == btnShow.getId()){
            startActivity(new Intent(this, MapActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_quit) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Beenden")
                    .setMessage("Wollen Sie die App beenden?")
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("Nein", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}

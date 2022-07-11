package com.daffahaidar.mapsview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MapEventsReceiver {

    private MapView map = null;
    Button load, save;
    TextView longitude, latitude, location;
    EditText etInput, lonInput, latInput;
    String filename = "";
    String filepath = "";
    String fileContent = "";
    String default_title = "Binus Bandung";

    Double default_longitude = 107.5932937;
    Double default_latitude = -6.9157785;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);
        map.setTileSource(TileSourceFactory.MAPNIK);
        String[] permissionStrings = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissionsIfNecessary(permissionStrings);
        map.getController().setZoom(5);
        GeoPoint g = new GeoPoint(4.00,110.00,0);
        map.getController().setCenter(g);

        MapsLocation();

        load = findViewById(R.id.LoadMaps);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        location = findViewById(R.id.location);
        etInput = findViewById(R.id.inputLocation);
        lonInput = findViewById(R.id.inputLongitude);
        latInput = findViewById(R.id.inputLatitude);
        save = findViewById(R.id.btnSave);
        filename = "maps.txt";
        filepath = "MyFileDir";
        longitude.setText("Default Longitude: " + default_longitude.toString());
        latitude.setText("Default Latitude: " + default_latitude.toString());
        location.setText("Default Location: " + default_title);

        if(!isExternalStorageAvailableForRW()){
            save.setEnabled(false);
        }

        saveFile();
        loadFile();
    }


    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void loadFile(){
        load.setOnClickListener(view -> {
            FileReader fr;
            File myExternalFile = new File(getExternalFilesDir(filepath), filename);
            StringBuilder stringBuilder = new StringBuilder();
            try{
                fr = new FileReader(myExternalFile);
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null){
                    stringBuilder.append(line).append("\n");
                    line = br.readLine();
                }
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                String fileContent = stringBuilder.toString();

                String[] parts = fileContent.split(",");
                default_title = parts[0];

                default_longitude = Double.parseDouble(parts[1]);
                default_latitude = Double.parseDouble(parts[2]);

                longitude.setText("Current Longitude: " + default_longitude.toString());
                latitude.setText("Current Latitude: " + default_latitude.toString());
                location.setText("Current Location: " + default_title);

                MapsLocation();
            }
        });
    }

    public void saveFile(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileContent = etInput.getText().toString() + "," + lonInput.getText().toString() + "," + latInput.getText().toString();
                if(!fileContent.equals("")){
                    File myExternalFile = new File(getExternalFilesDir(filepath), filename);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(myExternalFile);
                        fos.write(fileContent.getBytes());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    etInput.setText("");
                    lonInput.setText("");
                    latInput.setText("");
                    Toast.makeText(getApplicationContext(), "File saved to: " + myExternalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter some text", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isExternalStorageAvailableForRW(){
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public void MapsLocation(){
        GeoPoint startPoint = new GeoPoint(default_latitude, default_longitude,0);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(default_title);
        map.getController().setCenter(startPoint);
        map.getController().setZoom(19);
        map.getOverlays().add(startMarker);
        map.invalidate();
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
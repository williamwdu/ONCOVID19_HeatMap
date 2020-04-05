package com.example.on_covid_19;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.opencsv.CSVReader;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_");
    Date today = new Date();
    String datePrefix = dateFormat.format(today);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        Button buttonDeath = (Button) findViewById(R.id.button1);
        Button buttonTotal = (Button) findViewById(R.id.button2);
        Button buttonActive = (Button) findViewById(R.id.button3);

        buttonDeath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ff3600"));
                buttonTotal.setBackgroundColor(Color.parseColor("#ffc300"));
                buttonActive.setBackgroundColor(Color.parseColor("#ffc300"));
                mOverlay.remove();
                addHeatMap(3);
            }
        });

        buttonActive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ff3600"));
                buttonTotal.setBackgroundColor(Color.parseColor("#ffc300"));
                buttonDeath.setBackgroundColor(Color.parseColor("#ffc300"));
                mOverlay.remove();
                addHeatMap(2);
            }
        });

        buttonTotal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ff3600"));
                buttonDeath.setBackgroundColor(Color.parseColor("#ffc300"));
                buttonActive.setBackgroundColor(Color.parseColor("#ffc300"));
                mOverlay.remove();
                addHeatMap(1);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                return;
            case 2:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                return;
            default:
                return;
        }
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        downloadcsv();

        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(43.6532, -79.3832))      // Sets the center of the map to Mountain View
                .zoom(10)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                mProvider.setRadius((int)cameraPosition.zoom*28);
                mOverlay.clearTileCache();
            }
        });

        addHeatMap(2);

    }
    private void downloadcsv(){
        //check if data already downloaded for today

        File check = new File(URI.create("file://" + Environment.getExternalStorageDirectory() + "/oncovid19" + "/" + datePrefix + "conposcovidloc.csv"));
        if(check.canRead()){
            //skip download
        }else {
            DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse("https://data.ontario.ca/dataset/f4112442-bdc8-45d2-be3c-12efae72fb27/resource/455fd63b-603d-4608-8216-7d8647f43350/download/conposcovidloc.csv");

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("My File");
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(false);
            request.setDestinationUri(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/oncovid19" + "/" + datePrefix + "conposcovidloc.csv"));

            downloadmanager.enqueue(request);
        }
    }

    private void addHeatMap(int type) {
        mMap.clear();
        List<COVIDCase> covidlist = null;

        // Get the data: latitude/longitude positions of police stations.
        covidlist = readItems(type);
        List<LatLng> list = new ArrayList<>();
        covidlist.forEach(x->list.add(x.coor));

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        CameraPosition cameraPosition = mMap.getCameraPosition();
        mProvider.setRadius((int)cameraPosition.zoom*28);
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.clearTileCache();
        addPins(covidlist);
    }

    private void addPins(List<COVIDCase> list){
        Map<String, Long> counted = list.stream().collect(Collectors.groupingBy(x->x.name, Collectors.counting()));
        HashMap<String, LatLng> institutions = new HashMap<>();

        list.stream().forEach(x->institutions.put(x.name,x.coor));

        counted.forEach((k,v)->{
            LatLng pin = institutions.get(k);
            mMap.addMarker(new MarkerOptions().position(pin)
                    .title(k+" Cases:"+v));

        });
    }
    private ArrayList<COVIDCase> readItems(int type)  {
        ArrayList<COVIDCase> totallist = new ArrayList<>();
        ArrayList<COVIDCase> result = new ArrayList<>();
        try {
            File csvfile = new File(Environment.getExternalStorageDirectory() + "/oncovid19/" + datePrefix + "conposcovidloc.csv");
            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                //og.d("ss",nextLine[4] + nextLine[5] + nextLine[11] + nextLine[12]+ "...etc...");
                double lat = Double.parseDouble(nextLine[11]);
                double lng = Double.parseDouble(nextLine[12]);
                totallist.add(new COVIDCase(new LatLng(lat, lng),nextLine[5],nextLine[4],nextLine[6]));
            }
        } catch (IOException e) {

        }
        if(type==1) { //total
            totallist.stream().forEach(x->result.add(x));
        }
        if(type==2) { //active
            totallist.stream().filter(x->x.status.equals("Not Resolved")).forEach(x->result.add(x));
        }
        if(type==3) { //death
            totallist.stream().filter(x->x.status.equals("Fatal")).forEach(x->result.add(x));
        }
        return result;
    }



}

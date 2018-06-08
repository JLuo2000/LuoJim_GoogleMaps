package com.example.jimlu.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText locationSearch;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private LocationManager locationManager;
    private static final long MIN_TIME_BW_UPDATES = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private boolean notTrackingMyLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 20;
    private boolean satellite = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng DC = new LatLng(39,-77);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Add a merkaer at your place of birth and move teh camera it it.
        // When the marker is tapped, display "Born Here."

        mMap.addMarker(new MarkerOptions().position(DC).title("Born Here."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(DC));

        /*if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d("MyMapsApp", "Failed Permission Check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d("MyMapsApp", "Failed COARSE Permission Check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},2);
        }
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED))
        {
            //(ActivityCompat.checkSelfPermission(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},2));
            mMap.setMyLocationEnabled(true);
        }*/

        locationSearch = (EditText) findViewById(R.id.editText_addr);

        gotMyLocationOneTime = false;
        getLocation();
    }
    public void  onSearch (View v){

        String location = locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use LocationManager for user location
        //implement the LocationListener interface to setup location services

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);


        Log.d("MyMapsApp", "onSearch: Location = " + location);
        Log.d("MyMapsApp", "onSearch: provider: " + provider);

        LatLng userLocation = null;

        try{
            if(service!= null){
                Log.d("MyMapsApp", "onSearch: LocationManager is not null");

                if((myLocation = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))!= null){
                    userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(),Toast.LENGTH_SHORT);
                }
                else if((myLocation = service.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(),Toast.LENGTH_SHORT);

                }
                else{
                    Log.d("MyMapsApp", "onSearch: myLocation is null from getLastKnownLocation");

                }
            }
        } catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "onSearch: Exception getLastKnownLocation");
            Toast.makeText(this, "onSearch: Exception getLastKnownLocation",Toast.LENGTH_SHORT);

        }

        if(!location.matches("")){
            Log.d("MyMapsApp", "onSearch: location field is populated");

            Geocoder geocoder = new Geocoder(this, Locale.US);

            try{
                //Get a list of the addresses
                addressList = geocoder.getFromLocationName(location,200,
                        userLocation.latitude - (5.0/60),
                        userLocation.longitude - (5.0/60),
                        userLocation.latitude + (5.0/60),
                        userLocation.longitude + (5.0/60));
                Log.d("MyMapsApp", "onSearch: addressList is created");
            } catch (IOException e){
                e.printStackTrace();
            }
            if(!addressList.isEmpty()){
                    Log.d("MyMapsApp", "onSearch: AddressList size is " + addressList.size());
                    for(int i = 0; i<addressList.size(); i++){
                        Address address = addressList.get(i);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                        //place a marker on the map
                        mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


                }
            }
        }
    }

    public void getLocation(){

        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //Get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: Network is enabled");
            }
            if(!isGPSEnabled&&!isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: no provider enabled");
            }
            else{
                if(isNetworkEnabled){
                    //Log.d("MyMapsApp", "getLocation: GPS is enabled");
                    if((ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    && (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if(isGPSEnabled){
                    //Log.d("MyMapsApp", "getLocation: GPS is enabled");
                    if((ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            && (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
            }
        } catch(Exception e){
            Log.d("MyMapsApp","getLocation: Exception in getLocation");
            e.printStackTrace();
        }
    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.NETWORK_PROVIDER);

            if(!gotMyLocationOneTime){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }
            else{
                if((ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        && (ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
            if(isGPSEnabled){

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp","LocationListenerNetwork: status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);
            if(!gotMyLocationOneTime){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }
            else{

            }

        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle extras) {
            Log.d("MyMapsApp","LocationListenerGPS: status change");
            //switch status (i)
                //case LocationProvider.AVAILABLE
                //Print out log.d and/or toast message
                //break
                //case LocationProvider.OUT_OF_SERVICE
                //enable network updates
                //break
                //case LocationProvider.TEMPORARILY_UNAVAILABLE;
                // enable both network and GPS
                //break;
                //default

            switch (i){
                case (LocationProvider.AVAILABLE):
                    Log.d("MyMapsApp", "locationListenerGPS: Status changed");
                break;
                case LocationProvider.OUT_OF_SERVICE:
                    isNetworkEnabled = true;
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    isGPSEnabled = true;
                    isNetworkEnabled = true;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAmarker(String provider){

        //if(locationManager != null)
        //  if(checkSelfPermission fails)
            //return
        //  myLocation = locationManager.getLastKnownLocation(provider)
        //LatLng userLocation = null;
        //if(myLocation = null) print log or toast messages
        //else
        //  userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        //  CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
        //  if(provider == LocationManager.GPS_PROVIDER)
        //      add circle for the marker with 2 outer rings
        //      mMap.addCircle(new CircleOptions()).center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(1).fillColor(Color.RED);
        //  else add circle for teh marker with 2 outer rings, but blue
        //  mMap.animateCamera(update)
        if(locationManager != null){
            if((ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )){
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);

        }
        LatLng userLocation = null;
        if(myLocation == null){
            Log.d("MyMapsApp", "dropAmarker: myLocation is null");

        }
        else{
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            
            if(provider == LocationManager.GPS_PROVIDER){
                mMap.addCircle(new CircleOptions().center(userLocation).radius(5.0).strokeColor(Color.RED).strokeWidth(1).fillColor(Color.RED));
            }
            else{
                mMap.addCircle(new CircleOptions().center(userLocation).radius(5.0).strokeColor(Color.BLUE).strokeWidth(1).fillColor(Color.BLUE));
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));
    }

    public void trackMyLocation(View view){
        //kick off the location tracker using getLocation to start the LocationL
        //if(notTrackingMyLocation) getLocation(); notTrackingMyLocation = false;
        //else(removeUpdates for both network and gPS; notTrackingMyLocation = true;

        if(notTrackingMyLocation){
            getLocation();
            notTrackingMyLocation = false;}

        else{
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            notTrackingMyLocation = true;
        }

    }

    public void changeView(View view){
        if(satellite){
            satellite = !satellite;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else{
            satellite = !satellite;
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }
}

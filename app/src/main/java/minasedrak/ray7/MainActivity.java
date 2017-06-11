package minasedrak.ray7;


import android.Manifest;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


// OnMapReadyCallBack returns Google Map
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {

    GoogleMap mGoogleMap;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    PlaceAutocompleteFragment fromAutoCompleteFragment;
    PlaceAutocompleteFragment toAutoCompleteFragment;

    Marker mSourceMarker;
    Marker mDestinationMarker;
    Marker raye7_marker;  // raye7 office Marker  ^_^

    Polyline routePaths;

    Button routesBtn;
    Button trafficBtn;
    Button dateAndTimeBtn;

    DateTime mDateTime; // Object to store chosen Date and time

    boolean isTrafficOn;

    private static final String LOG_PLACE_LISTENER = "PlaceSelectionListener";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (googlePlayServicesIsAvailable()) {
            Toast.makeText(this, "Successfully connected to google play services", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main);
            if(isOnline()){
                initMap();

                // MainFunctionality(1) From - To Fragments For Searching Selected Place
                fromAutoCompleteFragment = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.fromPlaceFragment);
                fromAutoCompleteFragment.setHint("From Location");
                fromAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        removeRaye7Marker();

                        // delete route if exists
                        removeRoutePathsPolyLine();

                        if(mSourceMarker != null){
                            mSourceMarker.remove();
                            mSourceMarker = null;
                        }
                        createSourceMarker(place.getLatLng(), place.getAddress().toString());
                        goToLocation(place.getLatLng(),true);

                    }

                    @Override
                    public void onError(Status status) {
                        Log.e(LOG_PLACE_LISTENER, "onError: Status = " + status.toString());
                        Toast.makeText(MainActivity.this, "Place selection failed: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });

                // Override build in Clear button on click listener
                // To delete Map Source marker  , Route too if it exists
                fromAutoCompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mSourceMarker.remove();
                                mSourceMarker = null;

                                // delete route if exists
                                removeRoutePathsPolyLine();

                                fromAutoCompleteFragment.setText("");
                                v.setVisibility(View.GONE);

                            }
                        });



                toAutoCompleteFragment = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.toPlaceFragment);
                toAutoCompleteFragment.setHint("To Location");
                toAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        removeRaye7Marker();

                        // delete route if exists
                        removeRoutePathsPolyLine();

                        if(mDestinationMarker != null){
                            mDestinationMarker.remove();
                            mDestinationMarker = null;
                        }
                        createDestinationMarker(place.getLatLng(), place.getAddress().toString());
                        goToLocation(place.getLatLng(), true);

                    }

                    @Override
                    public void onError(Status status) {
                        Log.e(LOG_PLACE_LISTENER, "onError: Status = " + status.toString());
                        Toast.makeText(MainActivity.this, "Place selection failed: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });

                // Override build in AutoPlaceFragment Clear button on click listener
                // To delete Map Destination marker  , Route too if it exists
                    toAutoCompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    mDestinationMarker.remove();
                                    mDestinationMarker = null;

                                    // delete route if exists
                                    removeRoutePathsPolyLine();

                                    toAutoCompleteFragment.setText("");
                                    v.setVisibility(View.GONE);

                                }
                            });


                // ********************************************************************* end Main Func 1


                //MainFunctionality(4) Date and Time Picker
                dateAndTimeBtn = (Button) findViewById(R.id.showDateAndTimeBtn);
                dateAndTimeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(mSourceMarker != null && mDestinationMarker != null){
                            // Create new instance of DateTime
                            mDateTime =  new DateTime();

                            // Create DatePickerFragment
                            DialogFragment dateFragment = new DatePickerFragment();
                            dateFragment.show(getSupportFragmentManager(), "Date Picker");

                        }else {
                            Toast.makeText(MainActivity.this, "Date can't be set without selecting Source and Destination", Toast.LENGTH_LONG).show();
                        }


                    }
                });



                // MainFunctionality(5) show route
                routesBtn = (Button) findViewById(R.id.showRouteBtn);
                routesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Verify that user has checked source and destination
                        if(mSourceMarker != null && mDestinationMarker != null){
                           showRoutes(mSourceMarker.getPosition(), mDestinationMarker.getPosition());
                        }else {
                            Toast.makeText(MainActivity.this, "Source and Destination must be selected", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Feature(1) Traffic Data
                trafficBtn = (Button) findViewById(R.id.showTrafficBtn);
                isTrafficOn = false;
                trafficBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showHideTrafficData();
                    }
                });



            }else {

                Toast.makeText(this, "Check internet connection !", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Displaying layout for announcing user to install google play services
            setContentView(R.layout.no_google_play_services);
        }



    }



    // Checking if google play services available on the running device or not
    private boolean googlePlayServicesIsAvailable() {

        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        // if user already has google play services but maybe not updated or got any error
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Error! ,can't connect to google play services", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    // Startup methods
    // initialize Google Map Fragment
    private void initMap() {
        MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMapFragment);
        mMapFragment.getMapAsync(this);
    }

    // Callback of Google Map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        onDestinationLongClick();

        // Default Location Raye7 Office ^_^
        goToLocation(30.0753349, 31.306726, true, 15);
        MarkerOptions raye7Marker = new MarkerOptions()
                                    .title("Raye7 Office , Heliopolis, Cairo")
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.rayh))
                                    .position(new LatLng(30.0753349,31.306726));
        raye7_marker    =            mGoogleMap.addMarker(raye7Marker);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        onCurrentPlaceButton();


    }
    // ******************************************************************* End Startup methods





    // MainFunctionality(2) Current Location button
    public void onCurrentPlaceButton() {
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if(isOnline()){
                    buildGoogleApiClient();
                }else {
                    Toast.makeText(MainActivity.this, "Check internet connection !", Toast.LENGTH_SHORT).show();
                }


                return false;
            }
        });
    }

    // Creating Google Api Client
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }

    // Google Api Client Connected to LocationServices API
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        buildLocationRequest();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    // Creating LocationRequest
    private  void buildLocationRequest() {
        mLocationRequest = new LocationRequest().create();

        // Get Current Place Depending on Battery Power ( For Battery Consumption )
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    // It works after Pressing on Current Location Button & FusedLocationApi is called
    @Override
    public void onLocationChanged(Location location) {

        if(location == null){
            Toast.makeText(this, "check gps , can't get current location", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Current location Changed", Toast.LENGTH_SHORT).show();
            // GeoCoder gets a list of possible nearest addresses of a specific latitude and longitude
            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            try {
                List<android.location.Address> listOfAddresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                // Log.i("Destination JSON", location.toString());
                // Log.i("Address Line Length", String.valueOf(location.get(0).getMaxAddressLineIndex()));

                   /* GeoCoder returns full address in ( AddressLine Array )
                    * locality = null , AdminArea = null , SubAdminArea = null
                    *  So Loop on AddressLine Array and Concatenate full address in one String */

                String fullAddress = "";

                for(int i=0; i < listOfAddresses.get(0).getMaxAddressLineIndex(); i++){
                    //Log.i("fulladdress", fullAddress);
                    fullAddress += listOfAddresses.get(0).getAddressLine(i) + " ,";
                }

                fromAutoCompleteFragment.setText(fullAddress + listOfAddresses.get(0).getCountryName());

                removeRaye7Marker();

                // delete route if exists
                removeRoutePathsPolyLine();

                // Delete Old Source Marker if exists
                if(mSourceMarker != null){
                    mSourceMarker.remove();
                    mSourceMarker = null;
                }
                createSourceMarker(location.getLatitude(), location.getLongitude(), fullAddress + listOfAddresses.get(0).getCountryName());

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
    // ***************************************************************************** End Main Func 2






    // MainFunctionality(3) on Long Click Listener
    private void onDestinationLongClick() {

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if(isOnline()) {
                    Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<android.location.Address> location = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        // Log.i("Destination JSON", location.toString());
                        // Log.i("Address Line Length", String.valueOf(location.get(0).getMaxAddressLineIndex()));

                   /* GeoCoder returns full address in ( AddressLine Array )
                    * locality = null , AdminArea = null , SubAdminArea = null
                    *  So Loop on AddressLine Array and Concatenate full address in one String */

                        String fullAddress = "";

                        for (int i = 0; i < location.get(0).getMaxAddressLineIndex(); i++) {
                            fullAddress += location.get(0).getAddressLine(i) + " ,";
                        }


                        toAutoCompleteFragment.setText(fullAddress + location.get(0).getCountryName());

                        removeRaye7Marker();

                        // delete route if exists
                        removeRoutePathsPolyLine();

                        //Delete Old Destination Marker if exists
                        if (mDestinationMarker != null) {
                            mDestinationMarker.remove();
                            mDestinationMarker = null;
                        }
                        createDestinationMarker(latLng, fullAddress + location.get(0).getCountryName());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Check internet connection !", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    // ******************************************************* End Main Func 3




    // MainFunctionality(4) this method is called From TimePickerFragment
    public void showDateAndTime(){
        if(mDateTime.isDateHasBeenSetSuccessfully() && mDateTime.isTimeHasBeenSetSuccessfully()){
            int year = mDateTime.getYear();
            int month = mDateTime.getMonth();
            int day = mDateTime.getDay();
            int hour = mDateTime.getHour();
            int minute = mDateTime.getMinute();

            Toast.makeText(MainActivity.this, "Date & Time has been set successfully \n"
                            + "Date: " + day + " " + month + " " + year + " \n"
                            + "Time: " + hour + " : " +minute
                    , Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(MainActivity.this, "Date and Time has not been set successfully", Toast.LENGTH_LONG).show();
        }
    }
    // ******************************************************* End Main 4








    // MainFunctionality(5) show possible routes between selected source and destination
    private void showRoutes(LatLng source, LatLng destination) {
        if(isOnline()){
            Routing routing = new Routing.Builder()
                    .withListener(this)
                    .waypoints(source,destination)
                    .build();
            routing.execute();
        }else{
            Toast.makeText(MainActivity.this, "Check internet connection !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {

        PolylineOptions options = new PolylineOptions()
                .width(5)
                .color(R.color.routeColor)
                .geodesic(true);

        for (Route route : routes){

            List<LatLng> latLngList = route.getPoints();


            for (int i=0;  i< latLngList.size(); i++){

                LatLng latLng = latLngList.get(i);
                options.add(latLng);
            }

        }

        removeRoutePathsPolyLine();

        routePaths =  mGoogleMap.addPolyline(options);

    }
    // ************************************************************************ End Main Func 5





    // Feature(1) Show/Hide Traffic Data
    private void showHideTrafficData(){

        if(isOnline()){
            if(isTrafficOn){
                isTrafficOn = false;
                mGoogleMap.setTrafficEnabled(false);
                trafficBtn.setTextColor(Color.WHITE);
            }else {
                isTrafficOn = true;
                mGoogleMap.setTrafficEnabled(true);
                trafficBtn.setTextColor(Color.GREEN);
            }
        }else {
            Toast.makeText(this, "Check internet connection !", Toast.LENGTH_SHORT).show();
        }


    }




    // Camera Movement methods
    // Move Map to a specific location using it's latitude , longitude
    private void goToLocation(LatLng ll, boolean animated) {

        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        if(animated){
            mGoogleMap.animateCamera(update);
        } else {
            mGoogleMap.moveCamera(update);
        }
    }

    // Move Map to a specific location using it's latitude , longitude (Including Zooming option)
    private void goToLocation(double lat, double lng,boolean animated, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        if(animated){
            mGoogleMap.animateCamera(update);
        }else {
            mGoogleMap.moveCamera(update);
        }
    }

    // ****************************************** //





    // Marker Methods
    // Creating Source Marker
    private void createSourceMarker(Double lat, Double lng, String address) {

        MarkerOptions options = new MarkerOptions()
                                .title(address)
                                .position(new LatLng(lat, lng));

        mSourceMarker = mGoogleMap.addMarker(options);
    }

    // overloading Source Marker
    private void createSourceMarker(LatLng latlng, String address) {

        MarkerOptions options = new MarkerOptions()
                .title(address)
                .position(latlng);
        mSourceMarker = mGoogleMap.addMarker(options);
    }

    //Creating Destination Marker
    private void createDestinationMarker(LatLng latlng, String address){
        MarkerOptions options = new MarkerOptions()
                .title(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .position(latlng);
        mDestinationMarker = mGoogleMap.addMarker(options);

    }

    // Remove routePaths polyline
    private void removeRoutePathsPolyLine(){
        if(routePaths != null){
            routePaths.remove();
        }
    }

    // Remove ray7 marker
    private void removeRaye7Marker(){
        if(raye7_marker != null){
            raye7_marker.remove();
        }
    }
    // ********************************************** //


    //Checking Network & Internet Connection Availability
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return( netInfo != null && netInfo.isConnectedOrConnecting());
    }





    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingCancelled() {

    }
}

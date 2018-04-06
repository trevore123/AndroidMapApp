package com.example.mapapp;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.Tag;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.android.volley.Response.*;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

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
    public void onMapReady(GoogleMap googleMap){
        // Get the message from the intent
        Intent intent = getIntent();
        String location = intent.getStringExtra(MainActivity.LOCATION);
        String mode = intent.getStringExtra(MainActivity.MODE);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try{
            addresses = geocoder.getFromLocationName(location, 1);
        }
        catch(IOException exception) { //any I/O issues
            System.out.println("IOException");
        } catch (IllegalArgumentException exception) { //invalid latitude or longitude values
            System.out.println("IllegalArgumentException");
        }
        Address address = addresses.get(0);
        mMap = googleMap;

        // Add a marker in the entered location and move the camera
        LatLng marker = new LatLng(address.getLatitude(), address.getLongitude());
        String markerText = "Zipcode: "+address.getPostalCode();
        mMap.addMarker(new MarkerOptions().position(marker).title(markerText));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

//        final TextView mTextView = (TextView) findViewById(R.id.text);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/directions/json";
        url = url + "?origin=" + Double.toString(address.getLatitude())+","+Double.toString(address.getLongitude());
        url = url + "&destination=McKetta+Department+of+Chemical+Engineering";
        url = url + "&key=AIzaSyA8x3nwHN3lJP7wckZs3Ax23Sea6B786DQ";
        url = url + "&mode=" + mode;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);

                            JSONArray routes = reader.getJSONArray("routes");
                            JSONObject first_route = routes.getJSONObject(0);

                            JSONObject polyline = first_route.getJSONObject("overview_polyline");
                            String overview_polyline = polyline.getString("points");

                            JSONArray legs = first_route.getJSONArray("legs");
                            JSONObject first_leg = legs.getJSONObject(0);
                            String distance = first_leg.getJSONObject("distance").getString("text");
                            String duration = first_leg.getJSONObject("duration").getString("text");


                            //System.out.println("Response is: "+ response);
                            System.out.println(distance + " " + duration);
                            PolylineOptions opts = new PolylineOptions();
                            opts.addAll(decodePoly(overview_polyline));
                            mMap.addPolyline(opts);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                            alertDialogBuilder.setMessage("CPE is " + distance +
                                    " away and will take " + duration + ".");

                            // set dialog message
                            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();

                            System.out.println(response);

                        } catch (JSONException exception){
//                            //dont do anything
                        }
                    }
                }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}

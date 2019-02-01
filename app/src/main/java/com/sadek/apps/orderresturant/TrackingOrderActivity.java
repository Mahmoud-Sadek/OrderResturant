package com.sadek.apps.orderresturant;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sadek.apps.orderresturant.common.Common;
import com.sadek.apps.orderresturant.helper.DirectionJSONParser;
import com.sadek.apps.orderresturant.model.Request;
import com.sadek.apps.orderresturant.model.ShippingInformation;
import com.sadek.apps.orderresturant.remote.IGoogleService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference requests, shippingOrder;

    Request currentOrder;

    IGoogleService mService;

    Marker shippingMarker;

    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        shippingOrder = database.getReference("ShippingOrders");

        shippingOrder.addValueEventListener(this);

        mService = Common.getGoogleMapApi();
    }

    @Override
    protected void onStop() {
        shippingOrder.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        trackingLocation();

    }

    private void trackingLocation() {
        requests.child(Common.currentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentOrder = dataSnapshot.getValue(Request.class);

                        // order has latlng
                        if (currentOrder.getLatlng() != null && !currentOrder.getLatlng().isEmpty()) {
                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?latlng=")
                                    .append(currentOrder.getLatlng()).toString()+"&key=AIzaSyDaRn9gxgYgtA4tOmduQcAoaNtiL7Dm3Pw")
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(response.body());

                                                String lat = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();

                                                LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location).title("Order Destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                // set Shipper location
                                                shippingOrder.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.getValue() == null){
                                                                    Toast.makeText(TrackingOrderActivity.this, "Not shipped yet", Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }
                                                                ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                                                                LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                                if (shippingMarker == null) {
                                                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shipper);
                                                                    bitmap = Common.scaleBitmap(bitmap, 70, 70);
                                                                    shippingMarker = mMap.addMarker(new MarkerOptions().position(shipperLocation)
                                                                            .title("Shipper # " + shippingInformation.getOrderId())
                                                                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

                                                                } else {
                                                                    shippingMarker.setPosition(shipperLocation);
                                                                }

                                                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();

                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                //draw routs
                                                                if (polyline != null) {
                                                                    polyline.remove();polyline = null;}
                                                                mService.getDirections(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                        currentOrder.getLatlng(),
                                                                        "AIzaSyDaRn9gxgYgtA4tOmduQcAoaNtiL7Dm3Pw")
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {
                                                                                new ParserTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                Toast.makeText(TrackingOrderActivity.this, "Error, Can't get Location ", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(TrackingOrderActivity.this, "Error In Api", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        } else
                            //if order has address
                            if (currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty()) {
                                mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?address=")
                                        .append(currentOrder.getAddress()).toString()+"&key=AIzaSyDaRn9gxgYgtA4tOmduQcAoaNtiL7Dm3Pw")
                                        .enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(response.body());

                                                    String lat = ((JSONArray) jsonObject.get("results"))
                                                            .getJSONObject(0)
                                                            .getJSONObject("geometry")
                                                            .getJSONObject("location")
                                                            .get("lat").toString();

                                                    String lng = ((JSONArray) jsonObject.get("results"))
                                                            .getJSONObject(0)
                                                            .getJSONObject("geometry")
                                                            .getJSONObject("location")
                                                            .get("lng").toString();

                                                    LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                                    mMap.addMarker(new MarkerOptions().position(location).title("Order Destination")
                                                            .icon(BitmapDescriptorFactory.defaultMarker()));

                                                    // set Shipper location
                                                    shippingOrder.child(Common.currentKey)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                                                                    LatLng shipperLocation = new LatLng(shippingInformation.getLat(), shippingInformation.getLng());

                                                                    if (shippingMarker == null) {
                                                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shipper);
                                                                        bitmap = Common.scaleBitmap(bitmap, 70, 70);
                                                                        shippingMarker = mMap.addMarker(new MarkerOptions().position(shipperLocation)
                                                                                .title("Shipper # " + shippingInformation.getOrderId())
                                                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

                                                                    } else {
                                                                        shippingMarker.setPosition(shipperLocation);
                                                                    }

                                                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                            .target(shipperLocation)
                                                                            .zoom(16)
                                                                            .bearing(0)
                                                                            .tilt(45)
                                                                            .build();

                                                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                    //draw routs
                                                                    if (polyline != null)
                                                                        polyline.remove();
                                                                    mService.getDirections(shipperLocation.latitude + "," + shipperLocation.longitude,
                                                                            currentOrder.getAddress(),
                                                                            "AIzaSyDaRn9gxgYgtA4tOmduQcAoaNtiL7Dm3Pw")
                                                                            .enqueue(new Callback<String>() {
                                                                                @Override
                                                                                public void onResponse(Call<String> call, Response<String> response) {
                                                                                    new ParserTask().execute(response.body().toString());
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<String> call, Throwable t) {

                                                                                }
                                                                            });

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {

                                            }
                                        });

                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        trackingLocation();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        AlertDialog mDialog = new SpotsDialog(TrackingOrderActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mDialog.setMessage("Please Waiting ...");
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            }
            if(polyline != null){
                polyline.remove();
            }
            polyline = mMap.addPolyline(polylineOptions);

        }
    }
}

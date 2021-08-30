package com.aftab.rec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DecimalFormat;
import java.util.HashMap;

import util.DirectionAsync;
import util.LocationShareService;
import util.SessionManager;
import util.ToastMessage;

public class RouteActivity extends AppCompatActivity  implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, ResultCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {



    FirebaseAuth auth;
    GoogleApiClient client;
    boolean driver_profile = false;
    HashMap<String, Marker> hashMap;
    LatLng latLngCurrentuserLocation;
    GoogleMap mMap;
    DatabaseReference referenceDrivers;
    DatabaseReference referenceUsers;
    LocationRequest request;
    RequestQueue requestQueue;
    DatabaseReference scheduleReference;
    TextView textEmail;
    TextView textName;
    LatLng updateLatLng;
    boolean user_profile = false;
    private ImageView option_menu;

    private ImageView role,option,logout;
    private Button shareLocation,stopLocation;
    private TextView name;
    private Switch switch1;


    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    public void onConnectionSuspended(int i) {
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        role = (ImageView)findViewById(R.id.role);
        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    new ToastMessage(getApplicationContext()).showSmallCustomToast("Location Sharing..");
                    startService(new Intent(RouteActivity.this, LocationShareService.class));
                }else {
                    new ToastMessage(getApplicationContext()).showSmallCustomToast("Location Stop Sharing ...");
                    stopService(new Intent(RouteActivity.this, LocationShareService.class));
                }
            }
        });




        name = (TextView)findViewById(R.id.name);
        // logout = (ImageView)findViewById(R.id.logout);
        name.setText(""+new SessionManager(getApplicationContext()).getKeyUserName());
        shareLocation = (Button)findViewById(R.id.shareLocation);
        shareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ToastMessage(getApplicationContext()).showSmallCustomToast("Location Sharing..");
                startService(new Intent(RouteActivity.this, LocationShareService.class));
            }
        });

        stopLocation = (Button) findViewById(R.id.stopLocation);
        stopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ToastMessage(getApplicationContext()).showSmallCustomToast("Location Stop Sharing ...");
                stopService(new Intent(RouteActivity.this, LocationShareService.class));

            }
        });

        this.auth = FirebaseAuth.getInstance();
        this.requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth != null) {
                    auth.signOut();
                    finish();

                    new SessionManager(getApplicationContext()).setLogin("","","");
                    finish();
                    startActivity(new Intent(RouteActivity.this, MainActivity.class));
                    new ToastMessage(getApplicationContext()).showSmallCustomToast("Logout Successfully ...");
                    stopService(new Intent(RouteActivity.this, LocationShareService.class));

                }
            }
        });




        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        this.referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        this.referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        this.scheduleReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");
        this.hashMap = new HashMap();
        this.referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = RouteActivity.this.auth.getCurrentUser();
                if (dataSnapshot.child(currentUser.getUid()).child("lat").exists()) {
                    RouteActivity.this.driver_profile = true;
                    String str = (String) dataSnapshot.child(currentUser.getUid()).child("name").getValue(String.class);
                    String str2 = (String) dataSnapshot.child(currentUser.getUid()).child("email").getValue(String.class);
                    RouteActivity.this.name.setText("Type   : Driver "+"\n"+"Name : "+getNull(str)+"\n"+"E-mail : "+getNull(str2));
                    /*stopLocation.setVisibility(View.VISIBLE);
                    shareLocation.setVisibility(View.VISIBLE);*/
                    switch1.setVisibility(View.VISIBLE);
                    role.setImageDrawable(getDrawable(R.drawable.ic_driver));
                    //RouteActivity.this.textEmail.setText(str2);
                    return;
                }
                RouteActivity navigationActivity = RouteActivity.this;
                navigationActivity.user_profile = true;
                navigationActivity.referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseUser currentUser = RouteActivity.this.auth.getCurrentUser();
                        String str = (String) dataSnapshot.child(currentUser.getUid()).child("name").getValue(String.class);
                        String str2 = (String) dataSnapshot.child(currentUser.getUid()).child("email").getValue(String.class);
                        //FirebaseMessaging.getInstance().subscribeToTopic("news");
                        RouteActivity.this.name.setText("Type  : Student " +"\n"+"Name : "+getNull(str)+" \n"+"E-mail : "+getNull(str2));
                        role.setImageDrawable(getDrawable(R.drawable.ic_reading));
                       FirebaseMessaging.getInstance();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RouteActivity.this.getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RouteActivity.this.getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        this.referenceDrivers.addChildEventListener(new ChildEventListener() {
            public void onCancelled(DatabaseError databaseError) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String str) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildAdded(DataSnapshot dataSnapshot, String str) {
                try {
                    str = (String) dataSnapshot.child("name").getValue(String.class);
                    String str2 = (String) dataSnapshot.child("lat").getValue(String.class);
                    String str3 = (String) dataSnapshot.child("lng").getValue(String.class);
                    String str4 = (String) dataSnapshot.child("vehiclenumber").getValue(String.class);
                    LatLng latLng = new LatLng(Double.parseDouble(str2), Double.parseDouble(str3));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(str);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Van number: ");
                    stringBuilder.append(str4);
                    markerOptions.snippet(stringBuilder.toString());
                    markerOptions.position(latLng);
                    markerOptions.icon(bitmapDescriptorFromVector(RouteActivity.this, R.drawable.ic_bus));
                    Marker addMarker = RouteActivity.this.mMap.addMarker(markerOptions);
                    RouteActivity.this.hashMap.put(addMarker.getTitle(), addMarker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String str) {
                try {
                    str = dataSnapshot.child("name").getValue().toString();
                    String obj = dataSnapshot.child("lat").getValue().toString();
                    String obj2 = dataSnapshot.child("lng").getValue().toString();
                    RouteActivity.this.updateLatLng = new LatLng(Double.parseDouble(obj), Double.parseDouble(obj2));
                    Marker marker = (Marker) RouteActivity.this.hashMap.get(str);
                    if (marker != null) {
                        marker.setPosition(RouteActivity.this.updateLatLng);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



/*

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                mMap.addMarker(new MarkerOptions().position(sydney).title("Jamshedpur"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            }
        });

*/



    }










    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.mMap.setOnMarkerClickListener(this);
        this.client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addOnConnectionFailedListener(this).addConnectionCallbacks(this).build();
        this.client.connect();
    }

    public boolean onMarkerClick(Marker marker) {
        LatLng position = marker.getPosition();
        String format = new DecimalFormat("#.##").format(CalculationByDistance(this.latLngCurrentuserLocation, position));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(format);
        stringBuilder.append(" KM Distance.");
        new ToastMessage(this).showSmallCustomToast(""+stringBuilder.toString());
        //  Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_LONG).show();
        Object[] objArr = new Object[5];
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("https://maps.googleapis.com/maps/api/directions/json?");
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("origin=");
        stringBuilder3.append(position.latitude);
        String str = ",";
        stringBuilder3.append(str);
        stringBuilder3.append(position.longitude);
        stringBuilder2.append(stringBuilder3.toString());
        stringBuilder3 = new StringBuilder();
        stringBuilder3.append("&destination=");
        stringBuilder3.append(this.latLngCurrentuserLocation.latitude);
        stringBuilder3.append(str);
        stringBuilder3.append(this.latLngCurrentuserLocation.longitude);
        stringBuilder2.append(stringBuilder3.toString());
        stringBuilder2.append("&key=AIzaSyBAoSx5j9CDHxTtUqOYSgA527a3blYq280-x0");
        DirectionAsync directionAsync = new DirectionAsync(getApplicationContext());
        objArr[0] = this.mMap;
        objArr[1] = stringBuilder2.toString();
        objArr[2] = new LatLng(position.latitude, position.longitude);
        objArr[3] = new LatLng(this.latLngCurrentuserLocation.latitude, this.latLngCurrentuserLocation.longitude);
        objArr[4] = marker;
        directionAsync.execute(objArr);
        return true;
    }

    private double CalculationByDistance(LatLng latLng, LatLng latLng2) {
        double d = latLng.latitude;
        double d2 = latLng2.latitude;
        double d3 = latLng.longitude;
        double d4 = latLng2.longitude;
        double toRadians = Math.toRadians(d2 - d);
        toRadians /= 2.0d;
        d4 = Math.toRadians(d4 - d3) / 2.0d;
        d4 = Math.asin(Math.sqrt((Math.sin(toRadians) * Math.sin(toRadians)) + (((Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(d2))) * Math.sin(d4)) * Math.sin(d4)))) * 2.0d;
        d = (double) 6371;
        Double.isNaN(d);
        d *= d4;
        d4 = d / 1.0d;
        DecimalFormat decimalFormat = new DecimalFormat("####");
        Integer.valueOf(decimalFormat.format(d4)).intValue();
        d %= 1000.0d;
        Integer.valueOf(decimalFormat.format(d)).intValue();
        return d;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }




    @SuppressLint({"RestrictedApi"})
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        this.request = LocationRequest.create();
        this.request.setPriority(100);
        this.request.setInterval(5000);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            LocationSettingsRequest.Builder addLocationRequest = new LocationSettingsRequest.Builder().addLocationRequest(this.request);
            addLocationRequest.setAlwaysShow(true);
            LocationServices.SettingsApi.checkLocationSettings(this.client, addLocationRequest.build()).setResultCallback(this);
            LocationServices.FusedLocationApi.requestLocationUpdates(this.client, this.request, this);
        }
    }



    @Override
    public void onResult(@NonNull Result result) {

        Status status = result.getStatus();
        int statusCode = status.getStatusCode();
        if (statusCode != 0 && statusCode == 6) {
            try {
                status.startResolutionForResult(this, 202);
            } catch (IntentSender.SendIntentException unused) {
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.client, this);
        if (location == null) {
            new ToastMessage(this).showSmallCustomToast("Could not find location");
            //  Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_LONG).show();
            return;
        }
        this.latLngCurrentuserLocation = new LatLng(location.getLatitude(), location.getLongitude());
        this.mMap.addMarker(new MarkerOptions().position(this.latLngCurrentuserLocation).icon(bitmapDescriptorFromVector(RouteActivity.this, R.drawable.ic_marker))).setVisible(true);
        //   this.mMap.addMarker(new MarkerOptions().position(this.latLngCurrentuserLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))).setVisible(true);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.latLngCurrentuserLocation, 15.0f));

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public String getNull(String st){

        String str="";
        if (st.equals(null)){
            return str
                    ;
        }else {
            str =st;
        }
        return st;
    }
}




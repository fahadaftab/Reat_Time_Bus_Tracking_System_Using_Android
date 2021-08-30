package util;

import android.app.Notification.Builder;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationShareService extends Service implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    FirebaseAuth auth;
    Builder builder;
    GoogleApiClient client;
    LatLng latLngCurrent;

    DatabaseReference reference;
    LocationRequest request;

    FirebaseUser user;

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onConnectionSuspended(int i) {
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        this.reference = FirebaseDatabase.getInstance().getReference().child("Drivers");
        this.auth = FirebaseAuth.getInstance();
        this.user = this.auth.getCurrentUser();
        this.client = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        this.client.connect();
    }

    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        this.request = LocationRequest.create();
        this.request.setPriority(100);
        this.request.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            LocationServices.FusedLocationApi.requestLocationUpdates(this.client, this.request, this);
          //  showNotifications();
        }
    }



    public void onLocationChanged(Location location) {
        this.latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());
        shareLocation();
    }

    public void shareLocation() {
        try {
            this.reference.child(this.user.getUid()).child("lat").setValue(String.valueOf(this.latLngCurrent.latitude));
            this.reference.child(this.user.getUid()).child("lng").setValue(String.valueOf(this.latLngCurrent.longitude)).addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LocationShareService.this.getApplicationContext(), "Could not share Location.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Only drivers can share their location", Toast.LENGTH_LONG).show();
        }
    }

    public void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.client, this);
        this.client.disconnect();
    }
}

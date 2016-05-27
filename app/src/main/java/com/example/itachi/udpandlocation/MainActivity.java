package com.example.itachi.udpandlocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends Activity implements LocationListener {
    LocationManager locationManager;
    TextView txtLat;
    Switch onLocation;
    DatagramSocket datagramSocket;
    InetAddress inetAddress;
    Boolean isSendLocation = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            datagramSocket = new DatagramSocket(1991);
            inetAddress = InetAddress.getByName("192.168.1.40");
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        onLocation = (Switch) findViewById(R.id.switch1);
        onLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    isSendLocation = true;
                }else{
                    isSendLocation = false;
                }

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        txtLat = (TextView) findViewById(R.id.textView2);
        txtLat.setText(String.format("Latitude:%s, Longitude:%s", location.getLatitude(), location.getLongitude()));
        if (isSendLocation) {
            UdpClientTask udpClientTask = new UdpClientTask();
            udpClientTask.execute(location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    class UdpClientTask extends AsyncTask<Location, Void, Void> {
        @Override protected Void doInBackground(Location... params) {
            try {
                String s = String.format("%f,%f", params[0].getLatitude(), params[0].getLongitude());
                byte[] sendData = s.getBytes();
                DatagramPacket p = new DatagramPacket(sendData, s.length(), inetAddress,
                        1991);
                datagramSocket.send(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

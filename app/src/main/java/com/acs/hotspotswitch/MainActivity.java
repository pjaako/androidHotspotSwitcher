package com.acs.hotspotswitch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HotSpotSwitch";
    private static final String KEY = "HotSpotSwitch";
    private WifiManager wifiManager;
    private boolean localHotSpotStarted = false;
    WifiConfiguration currentConfig;
    WifiManager.LocalOnlyHotspotReservation hotspotReservation;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager  = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.activity_main);
    }

    public void onButtonCheckHotspotStateClick(View view) {
        ((TextView)findViewById(R.id.textViewHotspotInfo)).setText(ApStatus());
    }


    public void onButtonShowStandardDialogClick(View view) {
        sendTetheringIntent();
    }


    public void onButtonStartLocalOnlyHotspotClick(View view) {
        if (localHotSpotStarted){
            ((TextView)findViewById(R.id.textViewHotspotInfo)).setText("Hotspot can be started only once");
            return;
        }
        StartLocalOnlyHotspot();
    }

    public void onButtonStopLocalOnlyHotspotClick(View view) {
        if (hotspotReservation == null){
            ((TextView)findViewById(R.id.textViewHotspotInfo)).setText("No active Local Hotspot to stop");
            return;
        }
        hotspotReservation=null;
        StopLocalOnlyHotspot();
        ((TextView)findViewById(R.id.textViewHotspotInfo)).setText("Local Hotspot stopped");
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            ((TextView)findViewById(R.id.textViewHotspotInfo)).setText(msg.getData().getString(KEY));
            return true;
        }
    });


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void StartLocalOnlyHotspot() {
        int wifi_state = checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE);
        int fine_location = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (wifi_state != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CHANGE_WIFI_STATE);
        }
        if (fine_location != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty())
        {
            requestPermissions(listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),1);
            return;
        }



        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotReservation = reservation;
                currentConfig = hotspotReservation.getWifiConfiguration();
                localHotSpotStarted = true;
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(KEY, "Local Hotspot created, SSID: " + currentConfig.SSID + " password is " + currentConfig.preSharedKey);
                msg.setData(bundle);
                handler.sendMessage(msg);

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(KEY, "Hotspot stopped");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(KEY, "Hotspot stopped");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }, handler);
    }

    public void StopLocalOnlyHotspot() {
        if (hotspotReservation != null){
            hotspotReservation.close();
        }
        //hotspotReservation=null;
    }





    private void sendTetheringIntent() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");  //opens theher settings, universal
        startActivity(tetherSettings);

    }



    private String ApStatus() {
        int wifi_state = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
        if (wifi_state != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE},1);
            return "Wifi state access is required, grant it and try again.";
        }
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try {
            final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            return apState == 13 ? "Personal hotspot is ON" : "Personal hotspot is OFF";



        } catch (IllegalAccessException e) {
            return "IllegalAccessException";
        } catch (InvocationTargetException e) {
            return "InvocationTargetException, no permission?";
        } catch (NoSuchMethodException e) {
            return "NoSuchMethodException";
        }


    }
}






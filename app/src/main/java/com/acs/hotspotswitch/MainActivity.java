package com.acs.hotspotswitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonManageHotspotClick(View view) {
        sendTetheringIntent();
    }

    public void onButtonCheckApStateClick(View view) {
        Toast.makeText(this, "HotSpot is " + ( isApOn() ? "enabled":"disabled"), Toast.LENGTH_SHORT).show();
    }


    private void sendTetheringIntent() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");  //opens theher settings, universal
        startActivity(tetherSettings);

    }

    private boolean isApOn() {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        boolean ApOn = false;
        try {
            final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            ApOn = apState == 13;


        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return ApOn;

    }
}






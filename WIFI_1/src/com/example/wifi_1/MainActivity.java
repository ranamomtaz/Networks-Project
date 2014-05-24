package com.example.wifi_1;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mainText = (TextView) findViewById(R.id.mainText);

		// Initiate wifi service manager
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Check for wifi is disabled
		if (mainWifi.isWifiEnabled() == false) {
			// If wifi disabled then enable it
			Toast.makeText(getApplicationContext(),
					"wifi is disabled..making it enabled", Toast.LENGTH_LONG)
					.show();

			mainWifi.setWifiEnabled(true);
		}

		// wifi scaned value broadcast receiver
		receiverWifi = new WifiReceiver();

		// Register broadcast receiver
		// Broacast receiver will automatically call when number of wifi
		// connections changed
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		mainText.setText("Starting Scan...");

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mainWifi.startScan();
		mainText.setText("Starting Scan");
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	// Broadcast receiver class called its receive method
	// when number of wifi connections changed

	class WifiReceiver extends BroadcastReceiver {

		// This method call when number of wifi connections changed
		public void onReceive(Context c, Intent intent) {

			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
			sb.append("Number Of Wifi connections :"
					+ wifiList.size() + "\n\n");

			for (int i = 0; i < wifiList.size(); i++) {

				sb.append(new Integer(i + 1).toString() + ". ");
				ScanResult temp = wifiList.get(i);
				sb.append("Name: " + temp.SSID + "\n");
				sb.append("Mac Address: " + temp.BSSID + "\n");
				sb.append("Strength: " + temp.level + "\n");
				sb.append("---------------------------------------\n");
			}

			mainText.setText(sb);
			mainWifi.startScan();
		}

	}
}
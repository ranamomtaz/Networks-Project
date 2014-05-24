package com.example.networksproject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import java.io.*;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	Bitmap backGround;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	String AP1_MAC, AP2_MAC; 
	double[][] db_list;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		backGround = BitmapFactory.decodeResource(getResources(),
				R.drawable.map);

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

		try {
			readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mainWifi.startScan();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mainWifi.startScan();
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

			wifiList = mainWifi.getScanResults();
			int AP1_level = 0, AP2_level = 0;
			for (int i = 0; i < wifiList.size(); i++) {
				ScanResult temp = wifiList.get(i);
				String MAC = temp.BSSID;

				if (MAC.equalsIgnoreCase(AP1_MAC)) {
					AP1_level = temp.level;
				} else if (MAC.equalsIgnoreCase(AP2_MAC)) {
					AP2_level = temp.level;
				}
			}

			Log.d("debug", "ap1 = "+AP1_level+" ap2= "+AP2_level);
			double[] location = findLocation(AP1_level, AP2_level);
			drawCircle(location[0], location[1]);

			mainWifi.startScan();
		}

	}

	private void readFile() throws IOException {
		String str = "";
		InputStream is = this.getResources().openRawResource(R.raw.db);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		int location = -1;

		if (is != null) {
			while ((str = reader.readLine()) != null) {
				if (location == -1) {
					db_list = new double[Integer.parseInt(str)][4];

				} else if (location == 0) {
					AP1_MAC = str.substring(str.indexOf("=") + 1,
							str.indexOf("MAC2") - 1);
					
					AP2_MAC = str.substring(str.lastIndexOf("=") + 1);
				} else {
					
					String[] array = str.split(",");
					for (int i = 0; i < array.length; i++) {
						db_list[location-1][i] = Double.parseDouble(array[i]);
					}

				}
				location++;
			}
		}

		is.close();

	}

	private double eucDist(double value1, double reading1, double value2, double reading2) {
		double Sum = 0.0;

		Sum = Math.pow((value1 - reading1), 2.0)
				+ Math.pow((value2 - reading2), 2.0);

		return Math.sqrt(Sum);
	}

	private double[] findLocation(int read1, int read2) {
		int location = 0; // location of minimum distance
		double minDist = eucDist(db_list[location][0], read1,
				db_list[location][1], read2);
		for (int i = 1; i < db_list.length; i++) {
			double currDist = eucDist(db_list[i][0], read1, db_list[i][1],
					read2);
			if (currDist < minDist) {
				minDist = currDist;
				location = i;
			}
		}
		double[] loc = { db_list[location][2], db_list[location][3] };

		return loc;

	}

	private void drawCircle(double x, double y) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		paint.setColor(Color.parseColor("#0000FF"));
		Bitmap bg = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		canvas.drawBitmap(backGround, 0, 0, paint);

		canvas.drawCircle((int)x, (int)y, 20, paint);

		// .drawArc(new RectF(x,y,x+50,y+50), 0, 360, false, paint);
		LinearLayout ll = (LinearLayout) findViewById(R.id.layout);

		ll.setBackgroundDrawable(new BitmapDrawable(bg));

	}
}
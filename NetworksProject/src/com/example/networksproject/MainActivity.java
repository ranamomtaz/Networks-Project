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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

/*
 * please check http://stackoverflow.com/questions/18741034/how-to-get-available-wifi-networks-and-display-them-in-a-list-in-android
 */

public class MainActivity extends Activity {

	Bitmap backGround;
	WifiManager wifiMan;
	WifiReceiver wifiReceiver;
	List<ScanResult> wifiList;
	String AP1_MAC, AP2_MAC; 
	double[][] db_list;
	final int LOC_INDICATOR_RADIUS = 10;
	// TODO find them programatically [[fakss99]]
	// dimensions of the floorplan image used 
	final int MAP_WIDTH = 500;
	final int MAP_HEIGHT = 770;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		backGround = BitmapFactory.decodeResource(getResources(),
				R.drawable.map);

		// Initiate wifi service manager
		wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Check if wifi is disabled
		if (wifiMan.isWifiEnabled() == false) {
			// If wifi is disabled .. enable it
			Toast.makeText(getApplicationContext(), "WiFi is disabled..enabling WiFi", Toast.LENGTH_LONG).show();
			wifiMan.setWifiEnabled(true);
		}

		// wifi scanned value broadcast receiver
		wifiReceiver = new WifiReceiver();

		// Register broadcast receiver
		// BroadcastReceiver will be automatically called when the number of wifi connections changes
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		try {
			readFingerprintFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		wifiMan.startScan();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		wifiMan.startScan();
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(wifiReceiver);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	
	
	
	// Broadcast receiver class calls its receive method
	// when number of wifi connections changes

	class WifiReceiver extends BroadcastReceiver {

		// called when number of wifi connection changes 
		public void onReceive(Context c, Intent intent) {

			wifiList = wifiMan.getScanResults();
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

			Log.d("debug", "AP1 = "+AP1_level+"\\t AP2 = "+AP2_level);
			double[] location = findLocation(AP1_level, AP2_level);
			drawLocation((int) location[0], (int)location[1]);
			wifiMan.startScan();
		}

	}

	private void readFingerprintFile() throws IOException {
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
						db_list[location-1][i] = Double.parseDouble(array[i].trim());
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

	/**
	 * @param read1 wifi signal strength of AP1
	 * @param read2 wifi signal strength of AP2
	 * @return the estimated distance of current user location
	 */
	private double[] findLocation(int read1, int read2) {
		int location = 0; // location index of minimum distance
		
		double minDist = eucDist(db_list[location][0], read1, db_list[location][1], read2);
		
		for (int i = 1; i < db_list.length; i++) {
			double currDist = eucDist(db_list[i][0], read1, db_list[i][1], read2);
			if (currDist < minDist) {
				minDist = currDist;
				location = i;
			}
		}
		
		// TODO 
		// index db_list[:][2],db_list[:][3] are the absolute postions of location on the image
		double[] loc = { db_list[location][2], db_list[location][3] };

		return loc;

	}

	
	private void drawDebugLocations(Canvas canvas, Paint paint, double scaleX, double scaleY){
		canvas.drawCircle((int) (404*scaleX), (int) (247*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (416*scaleX), (int) (402*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (355*scaleX), (int) (402*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (208*scaleX), (int) (247*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (207*scaleX), (int) (398*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (288*scaleX), (int) (323*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (320*scaleX), (int) (439*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (411*scaleX), (int) (573*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (276*scaleX), (int) (439*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (276*scaleX), (int) (579*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (309*scaleX), (int) (659*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) ( 98*scaleX), (int) (614*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) ( 97*scaleX), (int) (711*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (223*scaleX), (int) (194*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
		canvas.drawCircle((int) (253*scaleX), (int) (81*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
	}
	
	
	private void drawLocation(int x, int y){
		// http://stackoverflow.com/questions/18520287/draw-a-circle-on-an-existing-image
		ImageView imageView = (ImageView)findViewById(R.id.floorplan);
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setColor(Color.BLUE);
	    paint.setStrokeWidth(10);

	    
	    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
	    Bitmap mutableBitmap = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true);
	    Canvas canvas = new Canvas(mutableBitmap);
	    
	    int currentWidth = imageView.getWidth(); // after android auto scaling
	    int currentHeight = imageView.getHeight(); // after android auto scaling
	    
	    double scaleX = 1d*currentWidth/MAP_WIDTH;
	    double scaleY = 1d*currentHeight/MAP_HEIGHT;
	    
	    canvas.drawCircle((int) (x*scaleX), (int) (y*scaleY), 2*LOC_INDICATOR_RADIUS, paint);
//	    drawDebugLocations(canvas, paint, scaleX, scaleY);
	    imageView.setImageBitmap(mutableBitmap);
	}
	
	}
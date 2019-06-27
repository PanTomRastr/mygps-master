package ru.startandroid.develop.p1381location;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.net.HttpURLConnection;

import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
public class MainActivity extends Activity {

	private WifiManager wifiManager;
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    //wifiConfiguration.SSID = "MyDummySSID";


	TextView tvEnabledGPS;
	TextView tvStatusGPS;
	TextView tvLocationGPS;
	TextView tvEnabledNet;
	TextView tvStatusNet;
	TextView tvLocationNet;
    Location mylocation;
	private LocationManager locationManager;
	StringBuilder sbGPS = new StringBuilder();
	StringBuilder sbNet = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
		tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
		tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
		tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
		tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
		tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		wifiManager= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		//wifiManager.setWifiEnabled(true);
        enableWIfi();
        //WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "MyDummySSID";
        changeStateWifiAp();
    }
    public void enableWIfi() {
	    wifiManager.setWifiEnabled(true);
    }

    @SuppressLint("PrivateApi")
    private void changeStateWifiAp() {
        Method method;
        try {
            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.invoke(wifiManager, wifiConfiguration, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000 * 10, 1, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000 * 10, 1,
				locationListener);
		checkEnabled();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			showLocation(location);

		}

		@Override
		public void onProviderDisabled(String provider) {
			checkEnabled();
		}

		@Override
		public void onProviderEnabled(String provider) {
			checkEnabled();
			showLocation(locationManager.getLastKnownLocation(provider));
		}

		@SuppressLint("SetTextI18n")
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				tvStatusGPS.setText("Status: " + String.valueOf(status));
			} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				tvStatusNet.setText("Status: " + String.valueOf(status));
			}
		}
	};

	private void showLocation(Location location) {
		if (location == null)
			return;
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			tvLocationGPS.setText(formatLocation(location));
			mylocation=location;
			sendPost();
		} else if (location.getProvider().equals(
				LocationManager.NETWORK_PROVIDER)) {
			mylocation=location;
			sendPost();
			tvLocationNet.setText(formatLocation(location));
		}
	}

	@SuppressLint("DefaultLocale")
	private String formatLocation(Location location) {
		if (location == null){

			return "";}
			else {
			    //sendPost();
				return String.format(
				"Coordinates: lat = %1$.9f, lon = %2$.9f, time = %3$tF %3$tT",
				location.getLatitude(), location.getLongitude(), new Date(
						location.getTime()));}
	}

	@SuppressLint("SetTextI18n")
	private void checkEnabled() {
		tvEnabledGPS.setText("Enabled: "
				+ locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER));
		tvEnabledNet.setText("Enabled: "
				+ locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
	}

	public void onClickLocationSettings(View view) {
		startActivity(new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	};
	public void sendPost() {
		Thread thread = new Thread(new Runnable() {

			//@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void run() {
				try {
                    changeStateWifiAp();
					URL url = new URL("http://lab.dltc.spbu.ru:8088/container_100/geolocation/");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					//conn.setRequestProperty("Accept","application/json");
					conn.setDoOutput(true);
					conn.setDoInput(true);

					JSONObject jsonParam = new JSONObject();
					TimeUnit.SECONDS.sleep(10);
					if (mylocation != null) {
                        String s = Long.toString(mylocation.getTime());
                        jsonParam.put("time", mylocation.getTime());
						jsonParam.put("latitude", mylocation.getLatitude());
						jsonParam.put("longitude", mylocation.getLongitude());
					}
					//jsonParam.put("uname", message.getUser());
					//jsonParam.put("message", message.getMessage());1


					Log.i("JSON", jsonParam.toString());
					DataOutputStream os = new DataOutputStream(conn.getOutputStream());
					//os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
					os.writeBytes(jsonParam.toString());
					//OutputStream out = new BufferedOutputStream(conn.getOutputStream());
					//JSONArray arr = JSONArray(jsonParam);
					//out.write(arr.toString().getBytes());

					os.flush();
					os.close();

					Log.i("STATUS", String.valueOf(conn.getResponseCode()));
					Log.i("MSG", conn.getResponseMessage());

					conn.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();



     }
}
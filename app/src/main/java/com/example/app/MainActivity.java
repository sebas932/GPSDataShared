package com.example.app;

import android.annotation.TargetApi;
import android.location.Location;
import android.location.LocationListener;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.ViewGroup;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity{

    TextView txtlat,txtlng,txtalt,txtpre,txtpro,txttiempo,txterror,txtCount;
    EditText hostService;
    RadioButton bstatus;
    Button startButton,stopButton;
    Switch sgps,snet,shttp;
    int count = 0;
    // GPSTracker class
    GPSTracker gps;

    LocationListener locationListener;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtlat = (TextView)findViewById(R.id.textView3);
        txtlng = (TextView)findViewById(R.id.textView4);
        txtalt = (TextView)findViewById(R.id.textView9);
        txtpre = (TextView)findViewById(R.id.textView10);
        txtpro = (TextView)findViewById(R.id.textView12);
        txtCount = (TextView)findViewById(R.id.textView13);
        txttiempo = (TextView)findViewById(R.id.textView11);
        bstatus = (RadioButton)findViewById(R.id.status);
        startButton = (Button)findViewById(R.id.button);
        stopButton = (Button)findViewById(R.id.button2);
        sgps = (Switch)findViewById(R.id.switch1);
        snet = (Switch)findViewById(R.id.switch2);
        shttp = (Switch)findViewById(R.id.switch3);

        // create class object
        gps = new GPSTracker(MainActivity.this);

        sgps.setChecked(gps.isGPSEnabled);
        snet.setChecked(gps.isNetworkEnabled);
        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            setLocationText(latitude,longitude,0,0,"gps",0);
            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            gps.showSettingsAlert();
        }


    }
    public void onClick(View view) {
        if (view.getId() == startButton.getId()){

            if(gps.canGetLocation()){
                bstatus.setChecked(true);
                locationListener = new GPSTracker(MainActivity.this) {
                    @Override
                    public void onLocationChanged(Location location) {
                        setLocationText(location.getLatitude(),location.getLongitude(),location.getAltitude(),location.getAccuracy(),location.getProvider(),location.getTime());
                        if(shttp.isChecked())sendData(location.getLatitude(),location.getLongitude(),location.getAltitude(),location.getAccuracy(),location.getProvider(),location.getTime());
                    }
                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {}
                    @Override
                    public void onProviderEnabled(String s) {}
                    @Override
                    public void onProviderDisabled(String s) {}
                };
            }else{
                gps.showSettingsAlert();
            }


        }if (view.getId() == stopButton.getId()){
            gps.stopUsingGPS();
            bstatus.setChecked(false);
        }
    }

    public void setLocationText(double pLat,double pLng,double pAlt,float pre,String prov,long lTime){
        txtlat.setText(Double.toString(pLat));
        txtlng.setText(Double.toString(pLng));
        txtalt.setText(Double.toString(pAlt));
        txtpre.setText(Float.toString(pre));
        txtpro.setText(prov);
        txttiempo.setText(Long.toString(lTime));
    }

    public void logError(String er) {
        txterror = (TextView)findViewById(R.id.error);
        txterror.setText(er);
    }

    public void sendData(double plat,double plng,double palt,float pre,String prov,long tiempo){
        // Create a new HttpClient and Post Header
        hostService = (EditText)findViewById(R.id.editText);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(String.valueOf(hostService.getText()));
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        nameValuePairs.add(new BasicNameValuePair("context", "gpsdata"));
        nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(plat)));
        nameValuePairs.add(new BasicNameValuePair("lng", Double.toString(plng)));
        nameValuePairs.add(new BasicNameValuePair("tiempo", Long.toString(tiempo)));
        nameValuePairs.add(new BasicNameValuePair("precision", Float.toString(pre)));
        nameValuePairs.add(new BasicNameValuePair("proveedor", prov ));

        // Url Encoding the POST parameters
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }
        // Making HTTP Request
        try {
            HttpResponse response = httpclient.execute(httppost);
            // writing response to log
            Toast.makeText(getApplicationContext(), "Enviando", Toast.LENGTH_LONG).show();
            logError(response.toString());
            count++;
            txtCount.setText(Integer.toString(count));
            //Log.d("Http Response:", response.toString());
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public PlaceholderFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}

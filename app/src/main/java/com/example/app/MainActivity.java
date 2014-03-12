package com.example.app;

import android.annotation.TargetApi;
import android.location.Location;
import android.location.LocationListener;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.ViewGroup;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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

    TextView txtlat,txtlng,txtalt,txtpre,txtpro,txttiempo,txterror,txtCount,txtUpdates;
    EditText hostService,pathService;
    RadioButton bstatus;
    Button startButton,stopButton;
    Switch sgps,snet,shttp,slocal;
    public Long sessionId;
    int count,countUpdates = 0;
    // GPSTracker class
    GPSTracker gps;
    String HTTPurl;
    HttpClient httpclient;
    HttpPost httppost;
    HttpResponse response;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostService = (EditText)findViewById(R.id.editText2);
        pathService = (EditText)findViewById(R.id.editText);
        txterror = (TextView)findViewById(R.id.error);
        txtlat = (TextView)findViewById(R.id.textView3);
        txtlng = (TextView)findViewById(R.id.textView4);
        txtalt = (TextView)findViewById(R.id.textView9);
        txtpre = (TextView)findViewById(R.id.textView10);
        txtpro = (TextView)findViewById(R.id.textView12);
        txtCount = (TextView)findViewById(R.id.textView13);
        txtUpdates = (TextView)findViewById(R.id.textView18);
        txttiempo = (TextView)findViewById(R.id.textView11);
        bstatus = (RadioButton)findViewById(R.id.status);
        startButton = (Button)findViewById(R.id.button);
        stopButton = (Button)findViewById(R.id.button2);
        sgps = (Switch)findViewById(R.id.switch1);
        snet = (Switch)findViewById(R.id.switch2);
        shttp = (Switch)findViewById(R.id.switch3);
        slocal = (Switch)findViewById(R.id.switch4);

        stopButton.setEnabled(false);
        sessionId = System.currentTimeMillis()/1000;
        // create class object
        gps = new GPSTracker(MainActivity.this);

        sgps.setChecked(gps.isGPSEnabled);
        snet.setChecked(gps.isNetworkEnabled);
        // check if GPS enabled
        if(gps.canGetLocation()){
            setLocationText(gps.getLatitude(),gps.getLongitude(),0,0,"Ultima posicion",0);
        }else{
            gps.showSettingsAlert();
        }
        HTTPurl = String.valueOf(hostService.getText())+String.valueOf(pathService.getText());


    }
    public void onClick(View view) {
        if (view.getId() == startButton.getId()){

            if(gps.canGetLocation()){
                    bstatus.setChecked(true);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    gps = new GPSTracker(MainActivity.this) {
                        @Override
                        public void onLocationChanged(Location location) {
                            setLocationText(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy(), location.getProvider(), location.getTime());
                            if (shttp.isChecked())
                                sendData(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy(), location.getProvider(), location.getTime());
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
                    };

            }else{
                gps.showSettingsAlert();
            }
        }
        if (view.getId() == stopButton.getId()){
            gps.stopUsingGPS();
            bstatus.setChecked(false);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            shttp.setChecked(false);
        }
        if (view.getId() == slocal.getId()){
            if (slocal.isChecked()){
                logError("local mode on");
            }else {
                logError("local mode off");
            }
        }
    }

    public void setLocationText(double pLat,double pLng,double pAlt,float pre,String prov,long lTime){
        txtlat.setText(Double.toString(pLat));
        txtlng.setText(Double.toString(pLng));
        txtalt.setText(Double.toString(pAlt));
        txtpre.setText(Float.toString(pre));
        txtpro.setText(prov);
        txttiempo.setText(Long.toString(lTime));

        countUpdates++;
        txtUpdates.setText(Integer.toString(countUpdates));
    }

    public void logError(String er) {
        txterror.setText(er);
    }

    public void sendData(double plat,double plng,double palt,float pre,String prov,long tiempo){
        // Add your data
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(HTTPurl);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
        nameValuePairs.add(new BasicNameValuePair("context", "gpsdata"));
        nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(plat)));
        nameValuePairs.add(new BasicNameValuePair("lng", Double.toString(plng)));
        nameValuePairs.add(new BasicNameValuePair("tiempo", Long.toString(tiempo)));
        nameValuePairs.add(new BasicNameValuePair("precision", Float.toString(pre)));
        nameValuePairs.add(new BasicNameValuePair("proveedor", prov ));
        nameValuePairs.add(new BasicNameValuePair("sessionId", Long.toString(sessionId) ));
        // Url Encoding the POST parameters
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }
        // Making HTTP Request
        try {
            response = httpclient.execute(httppost);
            // writing response to log
            //Toast.makeText(getApplicationContext(), "Enviando", Toast.LENGTH_SHORT).show();
            logError(response.toString());
            count++;
            txtCount.setText(Integer.toString(count));
            //Log.d("Http Response:", response.toString());
        } catch (ClientProtocolException e) {
            // writing exception to log
            logError(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            logError(e.toString());
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

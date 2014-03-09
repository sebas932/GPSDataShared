package com.example.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

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

    private TextView txtlat,txtlng,txtalt,txtpre,txtpro,txttiempo,txterror;
    private EditText hostService;
    private RadioButton bstatus;


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager lm =(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LocationListener ll = new myLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setLastLocationText(location);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }
    class myLocationListener implements LocationListener{
        public void onLocationChanged(Location location) {
            if(location != null){
                double pLong = location.getLongitude();
                double plat = location.getLatitude();
                double palt = location.getAltitude();
                float pre = location.getAccuracy();
                String pro = location.getProvider();
                long tiempo = location.getTime();
                setLocationText(plat, pLong, palt, pre, pro, tiempo);
                sendData(plat, pLong, palt, pre, pro, tiempo);
            }
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
            logError("Provider "+provider+" Status: " + status);
        }
        public void onProviderEnabled(String provider) {
            bstatus = (RadioButton)findViewById(R.id.status);
            bstatus.setChecked(true);
        }
        public void onProviderDisabled(String provider) {
            bstatus = (RadioButton)findViewById(R.id.status);
            bstatus.setChecked(false);
        }

    }
    //@Override
    public void onClick(View view) {
        if (view.getId() == findViewById(R.id.button).getId()){
            setLocationText(0,0,0,0,"",0);
            sendData(0,0,0,0,"",0);
        }
    }

    public void setLastLocationText(Location location){
        if(location != null){
            double pLong = location.getLongitude();
            double plat = location.getLatitude();
            double palt = location.getAltitude();
            float pre = location.getAccuracy();
            String pro = location.getProvider();
            long tiempo = location.getTime();
            //setLocationText(plat, pLong, palt, pre, pro, tiempo);
           // logError(pro);
        }
    }
    public void setLocationText(double plat,double plng,double palt,float pre,String prov,long tiempo){
        txtlat = (TextView)findViewById(R.id.textView3);
        txtlng = (TextView)findViewById(R.id.textView4);
        txtalt = (TextView)findViewById(R.id.textView9);
        txtpre = (TextView)findViewById(R.id.textView10);
        txtpro = (TextView)findViewById(R.id.textView12);
        txttiempo = (TextView)findViewById(R.id.textView11);

        txtlat.setText(Double.toString(plat));
        txtlng.setText(Double.toString(plng));
        txtalt.setText(Double.toString(palt));
        txtpre.setText(Float.toString(pre));
        txtpro.setText(prov);
        txttiempo.setText(Long.toString(tiempo));
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
            logError(response.toString());
            Log.d("Http Response:", response.toString());
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

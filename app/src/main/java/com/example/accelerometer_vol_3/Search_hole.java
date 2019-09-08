package com.example.accelerometer_vol_3;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class Search_hole extends AppCompatActivity implements SensorEventListener {
    public String l;
    public long curTime;

    public String s = "INCNRI3P8I02K5GK";
    public String choice = null;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private LocationManager locationManager;
    private long lastUpdate = 0;
    private float g, x, y, z;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    public ProgressDialog progressDialog;


    public long curTime1;
    public double xgps, ygps;
    private LocationListener listener;
    public ArrayList<Float> TotG = new ArrayList<>(30);
    public List<Float> AccX = new ArrayList<>(30);
    public List<Float> AccY = new ArrayList<>(30);
    public List<Float> AccZ = new ArrayList<>(30);
    public int flag;
    public int flag2;
    public int flag3;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.hole_search);
        //For gps permissions
        requestPermission();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        senSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                xgps = location.getLatitude();
                ygps = location.getLongitude();
                if (locationManager != null) {
                    locationManager.removeUpdates(this);
                }
                Log.w("ELa", String.valueOf(xgps));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }

        };

        new Description().execute();

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void onBackPressed() {
        super.onBackPressed();
        senSensorManager.unregisterListener(this);
        System.exit(0);

        Toast.makeText(Search_hole.this, "Hole Type", Toast.LENGTH_SHORT).show();
    }


    @Override

    public void onSensorChanged(SensorEvent sensorEvent) {


        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 10) {//<--Refresh Rate (millis)
                lastUpdate = curTime;


                final float alpha = (float) 0.8;

                //Remove Gravity Filter
                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
                linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
                linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
                linear_acceleration[2] = sensorEvent.values[2] - gravity[2];


                //Values of three axis on user's screen
                x = linear_acceleration[0];
                y = linear_acceleration[1];
                z = linear_acceleration[2];


                if (curTime - curTime1 > 15000) {//<--Start g calculation after 15 secs


                    g = (float) Math.sqrt(linear_acceleration[0] * linear_acceleration[0] + //<--calculate the average of acceleration
                            linear_acceleration[1] * linear_acceleration[1] +               //<--calculate the average of acceleration
                            linear_acceleration[2] * linear_acceleration[2]);               //<--calculate the average of acceleration

                    //Put values to the array list
                    if (g > 0.2) {

                        //check if buffer is full and if there is a value bigger than 0.8
                        if (TotG.size() >= 29 && flag == 1) {
                            senSensorManager.unregisterListener(Search_hole.this);
                            flag2 = 1;

                        } else if (TotG.size() >= 29 && flag == 0) {//if buffer is full and none of the values is not bigger than 0.6
                            TotG.clear();
                            AccX.clear();
                            AccY.clear();
                            AccZ.clear();
                        }
                        // Rounding with three digits
                        float R = (float) (Math.round(g * 10000.0) / 10000.0);

                        float R1 = (float) (Math.round(x * 10000.0) / 10000.0);

                        float R2 = (float) (Math.round(y * 10000.0) / 10000.0);

                        float R3 = (float) (Math.round(z * 10000.0) / 10000.0);

                        TotG.add(R);
                        AccX.add(R1);
                        AccY.add(R2);
                        AccZ.add(R3);


                        if (g > 0.8 && flag3 == 0) {
                            try {
                                //Notification sound
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplication(), notification);
                                r.play();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            flag3 = 1;
                            xgps = 0;
                            ygps = 0;
                            flag2 = 1;
                            flag = 1;
                            progressDialog.dismiss();
                            popupmenu();//<--Open menu

                            //Take cordinates
                            if (ActivityCompat.checkSelfPermission(Search_hole.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Search_hole.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                                            , 10);
                                }
                                return;
                            }
                            // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
                            //noinspection MissingPermission
                            ;
                            locationManager.requestSingleUpdate("gps", listener, Looper.myLooper());


                        }
                    }
                }

            }
        }
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void popupmenu() {

        //create and show the menu
        PopUp_Menu menu = new PopUp_Menu();
        menu.setCancelable(false);
        menu.show(getSupportFragmentManager(), "multi_holes");


    }


    public void sensorRegistration() {


        curTime1 = System.currentTimeMillis();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }


    public class Description extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            curTime1 = 0;
            TotG.clear();
            AccX.clear();
            AccY.clear();
            AccZ.clear();
            choice = null;
            g = 0;

            flag = 0;
            flag2 = 0;
            flag3 = 0;
            progressDialog = ProgressDialog.show(Search_hole.this, "",
                    "Getting Ready...", true);

        }


        @Override
        protected Void doInBackground(Void... params) {

            sensorRegistration();


            do {
                if (flag2 == 1) {
                    progressDialog.dismiss();
                }

            } while (flag2 == 0);

            do {
                choice = PopUp_Menu.getValue();
            } while (choice == null);


            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Upload_data();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void result) {


            choice = PopUp_Menu.getValue();

            if (choice == null) {


                //progressDialog.dismiss();
                new Description().execute();


            } else {
                PopUp_Menu.delete();



                // progressDialog.dismiss();
                new Description().execute();

            }


        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void Upload_data() throws IOException, JSONException {
        List<String> Time = new ArrayList<>(30);
        OkHttpClient okHttpClient = new OkHttpClient();

        JSONObject obj=new JSONObject();

        JSONArray arr = new JSONArray();
        HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();

        try {
            long remindDate = System.currentTimeMillis();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T' HH:mm:ss+03:00");
            final Calendar calendar = Calendar.getInstance();

            Date current;

            calendar.setTimeInMillis(remindDate);


            for(int i = 0 ;i<TotG.size();i++) {
                JSONObject json = new JSONObject();
                calendar.add(Calendar.SECOND, 1);


                current=calendar.getTime();
                String aa=sdf.format(current);

                Log.e("datara",aa);
                Time.add(aa);

                json.put("created_at", sdf.format(current) );
                json.put("field1", TotG.get(i));
                json.put("field2", AccX.get(i));
                json.put("field3", AccY.get(i));
                json.put("field4", AccZ.get(i));
                json.put("field5", xgps);
                json.put("field6", ygps);
                json.put("field7", choice);

                map.put("json" + i, json);
                arr.put(map.get("json" + i));


            }
        } catch (Exception e) {
            e.printStackTrace();


        }
        Log.e("re", String.valueOf(arr));
        int j;
        for(j=0;j<Time.size();j++){

            Log.e("Date", Time.get(j));


        }

        try{



            obj.put("write_api_key",s);
            obj.put("updates",arr);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("bulk_update.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(String.valueOf(obj));
            outputStreamWriter.close();

        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        //Read the file
        try {

            FileInputStream fileInputStream = openFileInput("bulk_update.json");

            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();

            String lines;
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines).append("\n");
            }
            Log.e("Json file(Read mode)", String.valueOf(stringBuffer));

        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }




        //set up JSON object to upload

        String json = obj.toString();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request.Builder()
                .url("https://api.thingspeak.com/channels/737400/bulk_update.json")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                updateView("Error - " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    try {
                        updateView(response.body().string());



                    } catch (IOException e) {
                        e.printStackTrace();
                        updateView("Error - " + e.getMessage());


                    }
                } else {
                    updateView("Not Success - code : " + response.code());

                }
            }

            public void updateView(final String strResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("after post",strResult);


                    }
                });
            }
        });


    }


}



package com.example.accelerometer_vol_3;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

public class Upload_arduino_data extends Activity {
    public final String ACTION_USB_PERMISSION = "com.example.parsefiles.USB_PERMISSION";
    ArrayList<String> list = new ArrayList<String>();
    public ProgressDialog progressDialog;
    int doin;
    StringBuffer s = new StringBuffer();

    int p = 0;
    JSONObject obj = new JSONObject();
    boolean flag1 = true;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    float curTime1, curTime2;
    UsbSerialInterface.UsbReadCallback mCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        curTime1 = System.currentTimeMillis();
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        Start();
        new Description().execute();
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            Toast.makeText(context, "Serial Connection Opened!", Toast.LENGTH_SHORT).show();


                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            }
        }


    };


    {
        mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
            @Override
            public void onReceivedData(byte[] arg0) {

                String data = null;

                try {
                    data = new String(arg0, "UTF-8");
                    s.append(data);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        };
    }


    public void Start() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }

    //Create a Async task. The Async task will read the text file from arduino
    public class Description extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(Upload_arduino_data.this, "",
                    "Uploading Data...", true);


        }


        @Override
        protected Void doInBackground(Void... params) {
            float totalTime;
            do {
                curTime2 = System.currentTimeMillis();

                totalTime = curTime2 - curTime1;


            } while (totalTime < 30000);


            int o = -1;
            JSONArray arr = new JSONArray();
            HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
            int i;
            String seco = s.toString();


            for (i = 0; i < seco.length(); i++) {
                list.add(String.valueOf(seco.charAt(i)));

            }


            String str[] = new String[list.size()];


            for (i = 0; i < list.size(); i++) {
                str[i] = list.get(i);


            }


            String Date = "";
            String G = "";
            String X = "";
            String Y = "";
            String Z = "";

            //An algorith that convert the data from txt file to json object
            //Must read every character from file. When a character is D or X or Y or Z that means the values after this letter are the values for this field
            //If letter is D the next 19 character are the TimeStamp
            //if letter is X the next values are the acceleration on x axis
            for (i = 0; i < str.length; i++) {

                String b = list.get(i);
                int y;
                if (str[i].equals("D")) {
                    p++;
                    for (y = 2; y < 21; y++) {

                        Date += str[i + y];

                    }
                    flag1 = true;
                    Date = Date + "+03:00";

                }

                if (str[i].equals("G")) {
                    for (y = 2; y < 8; y++) {
                        G += (str[i + y]);

                    }

                }
                if (str[i].equals("X")) {

                    if (str[i + 2].equals("-")) {
                        for (y = 2; y < 9; y++) {
                            X += str[i + y];
                        }


                    } else {

                        for (y = 2; y < 8; y++) {
                            X += str[i + y];
                        }

                    }

                }
                if (str[i].equals("Y")) {

                    if (str[i + 2].equals("-")) {

                        for (y = 2; y < 9; y++) {
                            Y += str[i + y];
                        }


                    } else {
                        for (y = 2; y < 8; y++) {
                            Y += str[i + y];
                        }
                    }

                }
                if (str[i].equals("Z")) {
                    o++;

                    if (str[i + 2].equals("-")) {
                        for (y = 2; y < 9; y++) {
                            Z += str[i + y];
                        }

                    } else {

                        for (y = 2; y < 8; y++) {
                            Z += str[i + y];
                        }

                    }

                    //put the values that we read from txt.file to a json object
                    try {
                        JSONObject json = new JSONObject();
                        json.put("created_at", Date);
                        json.put("field1", G);
                        json.put("field2", X);
                        json.put("field3", Y);

                        json.put("field4", Z);
                        map.put("json" + o, json);
                        arr.put(map.get("json" + o));
                        G = "";
                        X = "";
                        Y = "";
                        Z = "";
                        Date = "";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //The 940 is the limit for free users from ThinkSpeak platform
                    if (p == 940) {

                        break;
                    }

                }


            }

            //Create a json file that contains all the data from arduino sd card
            // The json file must have a specific
            try {

                obj.put("write_api_key", "RWW547NCB5QCI86C");
                obj.put("updates", arr);


            } catch (JSONException e) {
                e.printStackTrace();

            }

            String json = obj.toString();

            // Request to the ThingSpeak server
            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), json);

            //We use bulk update method to upload many entites in only one request. The limit for the entities is 940 per request
            Request request = new Request.Builder()
                    .url("https://api.thingspeak.com/channels/814792/bulk_update.json")
                    .post(body)
                    .build();

            // check if data have gone online
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
                            flag1 = false;
                            Toast.makeText(Upload_arduino_data.this, "Succesfull", Toast.LENGTH_SHORT).show();

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
                            Log.e("after post", strResult);


                        }
                    });
                }
            });


            Log.e("Json", String.valueOf(obj));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //OkHttpClient okHttpClient = new OkHttpClient();
            do {

            } while (flag1 == true);
            progressDialog.dismiss();
            finish();

        }
    }


}

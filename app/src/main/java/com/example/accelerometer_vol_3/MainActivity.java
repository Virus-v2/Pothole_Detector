package com.example.accelerometer_vol_3;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    UsbManager usbManager;

    public final String ACTION_USB_PERMISSION = "com.example.accelerometer_vol_3.USB_PERMISSION";



    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("SERIAL", "PERM NOT GRANTED");
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                Log.d("SERIAL", "PERM NOT GRANTED");
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Toast.makeText(getApplicationContext(),"usb is connected",Toast.LENGTH_SHORT).show();
                // final ImageButton upload_butt=findViewById(R.id.arduino_upload);
                openUpload_arduino_data();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Toast.makeText(getApplicationContext(),"usb is not connected",Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        //check if device is connected to a network
        haveNetworkConnection();
        final ImageButton search_butt = findViewById(R.id.search_button);
        final ImageButton info_butt = findViewById(R.id.info);
        final ImageButton upload_butt=findViewById(R.id.arduino_upload);
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        // upload_butt.setEnabled(false);

        IntentFilter filter = new IntentFilter();

        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        upload_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.upload_title)
                        .setMessage(R.string.upload_info)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ;
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.

                        .setCancelable(false)
                        .show();
            }
        });


        //set a click listener to info_butt. When user click info_butt device will vibrate for a wile and after will open a Dialog box which contains some infosmation about the app
        info_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.info_title)
                        .setMessage(R.string.info)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ;
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.

                        .setCancelable(false)
                        .show();
            }
        });


        //set a click listener to search_butt. When user click search_but device will vibrate for a wile and call the openSearchHole method
        search_butt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                openSearchHole();
            }
        });





    }

    // openSearchHole method containts an intent that starts a new activity. That activity will start searching for a pothole
    public void openSearchHole() {

        Intent intent = new Intent(this, Search_hole.class);
        startActivity(intent);

    }
    //openUpload_arduino_data will start a new activity that will upload all the data from arduino to ThinkSpeak platform
    public void openUpload_arduino_data(){
        Intent intent = new Intent(this,Upload_arduino_data.class);
        startActivity (intent);


    }




    // The below method check if the device is connected to a network. If not will appear a Message that from the user to connect the devide to a network.
    // Network connection is  necessary
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        if (haveConnectedMobile == false && haveConnectedWifi == false) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.internet_check_title)
                    .setMessage(R.string.internet_check_)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.

                    .setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
                    .show();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


}

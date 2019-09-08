package com.example.accelerometer_vol_3;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;


public class PopUp_Menu extends DialogFragment {


    public static String users_selection;
    public static String selection_send;

    @Override
    @NonNull


    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final String[] items = getResources().getStringArray(R.array.hole_type);
        users_selection = null;
        selection_send = null;
        //Creat a Alert Dialog to display the choises
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setTitle("Choose").setSingleChoiceItems(R.array.hole_type, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                users_selection = items[which];


            }


        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                selection_send = users_selection;
                if (users_selection == null) {

                    selection_send = "nothing";
                }


                Toast.makeText(getActivity(), "You pressed: " + selection_send, Toast.LENGTH_SHORT).show();
                //When user press back the screen will display the begging screen
            }
        }).setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                System.exit(0);

            }
        });

        return builder.create();

    }


    public static String getValue() {

        return selection_send;


    }

    //Must set the choise of the user null for next evaluation
    public static void delete() {

        users_selection = null;
        selection_send = null;


    }


}
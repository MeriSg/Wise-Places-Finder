package com.meri_sg.places_finder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;


 // Created on 12-Aug-16.

public class MyBatteryReceiver extends BroadcastReceiver {

    public enum e_trace {
        INITIAL_DEFAULT_VAL ()
        ,BATTERY_PLUGGED_AC__MSG_SENT ()
        ,BATTERY_PLUGGED_USB__MSG_SENT ()
        ,BATTERY_UNPLUGGED__MSG_SENT ()
        ,LAST_MSG__AC ()
        ,LAST_MSG__USB ()
        ,LAST_MSG__UNPLUGGED ()
    }

    static e_trace trace=e_trace.INITIAL_DEFAULT_VAL;

    @Override
    public void onReceive(Context context, Intent intent) {
        int plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        //check the plugged status
        switch(plugged){
            case BatteryManager.BATTERY_PLUGGED_AC:
                if (trace==e_trace.LAST_MSG__UNPLUGGED||trace==e_trace.LAST_MSG__USB||trace==e_trace.INITIAL_DEFAULT_VAL)
                    trace=e_trace.BATTERY_PLUGGED_AC__MSG_SENT;
                break;

            case BatteryManager.BATTERY_PLUGGED_USB:
                if (trace==e_trace.LAST_MSG__UNPLUGGED||trace==e_trace.LAST_MSG__AC||trace==e_trace.INITIAL_DEFAULT_VAL)
                    trace=e_trace.BATTERY_PLUGGED_USB__MSG_SENT;
                break;

            default:
                if (trace==e_trace.LAST_MSG__USB||trace==e_trace.LAST_MSG__AC||trace==e_trace.INITIAL_DEFAULT_VAL)
                    trace=e_trace.BATTERY_UNPLUGGED__MSG_SENT;
                break;
        }

        //make sure each message shows only ONCE when something changed
        if (trace==e_trace.BATTERY_PLUGGED_AC__MSG_SENT){
            Toast.makeText(context, R.string.plugged_AC, Toast.LENGTH_SHORT).show();
            trace=e_trace.LAST_MSG__AC;
        }else if (trace==e_trace.BATTERY_PLUGGED_USB__MSG_SENT){
            Toast.makeText(context, R.string.plugged_USB, Toast.LENGTH_SHORT).show();
            trace=e_trace.LAST_MSG__USB;
        }else if (trace==e_trace.BATTERY_UNPLUGGED__MSG_SENT){
            Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
            trace=e_trace.LAST_MSG__UNPLUGGED;
        }


    }//end of onReceive

}//end MyBatteryReceiver

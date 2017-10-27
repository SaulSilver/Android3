package a2dv606.com.dv606hh222ixassignment3.IncomingCallHistory2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import java.io.FileOutputStream;
import java.io.IOException;

import static a2dv606.com.dv606hh222ixassignment3.IncomingCallHistory2.IncCallHistoryActivityList.FILE_NAME;
import static a2dv606.com.dv606hh222ixassignment3.IncomingCallHistory2.IncCallHistoryActivityList.oldState;

/**
 * A class to receive the broadcast fired by an Incoming call
 *
 * Created by hatem on 2017-08-03.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Bundle extras = intent.getExtras();

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            if ("RINGING".equals(state)) {
                if (!state.equals(oldState)) {
                    oldState = state;
                    String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    savePhoneNumber(phoneNumber);
                    System.out.println("Ringing");
                }
            } else oldState = state;
        }
    }

    /**
     * A method to save the incoming call phone number in the call history file
     */
    private void savePhoneNumber(String phoneNumber) {
        if (!phoneNumber.equals("")) {
            try {
                FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
                fos.write((phoneNumber + "\n").getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
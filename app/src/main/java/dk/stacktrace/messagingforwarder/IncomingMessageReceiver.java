package dk.stacktrace.messagingforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

public class IncomingMessageReceiver extends BroadcastReceiver {
    private static final String TAG = IncomingMessageReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Handling message for forwarding");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.contains("phone_number")) {
            Log.w(TAG, "Phone number to forward from not set. Will not forward any messages");
            return;
        }
        if (!preferences.contains("target_URL")) {
            Log.w(TAG, "URL to forward to not set. Will not forward any messages");
            return;
        }
        if (!preferences.contains("client_secret")) {
            Log.w(TAG, "Client Secret not set. Will not forward any messages");
            return;
        }
        String phone_number = preferences.getString("phone_number", "");
        String slot = Integer.toString(Objects.requireNonNull(intent.getExtras()).getInt("slot", -1));
        String secret = preferences.getString("client_secret", "");
        URL target_url;
        try {
            target_url = new URL(preferences.getString("target_URL", ""));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Unable to parse URL: " + e.getMessage());
            return;
        }

        // Retrieves a map of extended data from the intent.
        Log.i("Recieving SIM slot: ", slot);





        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage message : messages) {
            if (PhoneNumberUtils.compare(message.getDisplayOriginatingAddress(), phone_number)) {
                String msg = message.getDisplayMessageBody();
                String timestamp = Long.toString(message.getTimestampMillis());
                Log.i(TAG, "Starting forwarding of message from " + phone_number);
                new Thread(new HttpPostThread(target_url, msg, slot, timestamp, secret)).start();
            }
        }
    }
}

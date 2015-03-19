package jp.nemustech.adrop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            boolean enable = sharedPref.getBoolean(SettingsActivity.ENABLE, true);
            if (enable) {
                Log.d(TAG, "Starting aDrop service.");
                Intent serviceIntent = new Intent(context, aDropService.class);
                context.startService(serviceIntent);
            } else {
                Log.d(TAG, "aDrop service is not enabled");
            }
        }
    }
}

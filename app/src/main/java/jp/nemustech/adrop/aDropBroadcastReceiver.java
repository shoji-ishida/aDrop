package jp.nemustech.adrop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;


/**
 * Created by ishida on 15/02/15.
 */
public class aDropBroadcastReceiver extends BroadcastReceiver {

    static private final String TAG = "aDropBroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private Context context;

    public aDropBroadcastReceiver(WifiP2pManager manager, Channel channel, Context context) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (manager == null) {
            return;
        }
        String action = intent.getAction();

        switch (action) {
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                break;
            case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION:
                handleDiscoveryChangedAction(intent);
                break;
            default:
                Log.d(TAG, "Unhandled Broadcast intent: " + action);
        }
    }

    private void handleConnectionChangedAction(Intent intent) {

    }

    private void handleThisDeviceChangedAction(Intent intent) {

    }
    
    private void handleDiscoveryChangedAction(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
        switch (state) {
            case WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED:
                Log.d(TAG, "P2P discovery started");
                break;
            case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                Log.d(TAG, "P2P discovery stopped");
                break;
            default:
                Log.d(TAG, "P2P discovery unknown state");
        }
    }
}

package jp.nemustech.adrop;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class aDropService extends Service implements ChannelListener, ConnectionInfoListener {
    private static final String TAG = "aDropService";
    private String userName;
    private IntentFilter intentFilter;
    private WifiP2pManager manager;
    private Channel channel;
    private WifiP2pDnsSdServiceInfo service;
    private BroadcastReceiver receiver;

    public aDropService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // open Cursor for Profile
        Cursor mCursor = getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI, null, null, null, null);

        mCursor.moveToFirst();

        // retrieve UserName
        int nameIndex = mCursor
                .getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
        userName = mCursor.getString(nameIndex);
        mCursor.close();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), this);

        // registering multiple receivers causes multiple Intents to be
        // dispatched.
        // make sure single receiver is registered at a time
        if (receiver == null) {
            Log.d(TAG, "Broadcast receiver registered");
            receiver = new aDropBroadcastReceiver(manager, channel, this);
            registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        removeLocalService();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onChannelDisconnected() {
        Log.d(TAG, "Channel disconnected");
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "Connection Info available: " + info);
    }

    private void addLocalService() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(aDropDnsService.TXTRECORD_PROP_AVAILABLE, "visible");
        record.put(aDropDnsService.USER_NAME, userName);

        service = WifiP2pDnsSdServiceInfo.newInstance(
                aDropDnsService.SERVICE_INSTANCE,
                aDropDnsService.SERVICE_REG_TYPE, record);

        // make sure no service previously started
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added Local Service: " + service);
                    }

                    @Override
                    public void onFailure(int error) {
                        Log.d(TAG, "Failed to add a service: " + error);
                    }
                });
            }

            @Override
            public void onFailure(int error) {
                Log.d(TAG, "Failed to clear a service: " + error);
            }
        });
    }

    private void removeLocalService() {
        if (service == null) {
            return;
        }

        manager.removeLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Removed Local Service");
                service = null;
            }

            @Override
            public void onFailure(int error) {
                Log.d(TAG, "Failed to remove a service: " + error);
            }
        });
    }
}

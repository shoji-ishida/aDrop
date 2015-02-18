package jp.nemustech.adrop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;


public class aDropActivity extends Activity implements WifiP2pManager.ChannelListener {
    private static final String TAG = "aDropActivity";
    public static final String FRAGMENT_TAG = "devices";

    private WifiP2pDnsSdServiceRequest serviceRequest;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private aDropDnsServicesList servicesList;

    private Map<String, String> profiles = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "On Create");
        if (manager == null) {
            Log.d(TAG, "Get WifiP2pManager");
            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        }
        if (channel == null) {
            Log.d(TAG, "Initialize Channel");
            channel = manager.initialize(this, getMainLooper(), this);
        }
        initDnsSd();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "On Start");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "On Restart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "On Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "On Destroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "On Pause");
        //stopDiscovery();
        servicesList.listAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        servicesList = new aDropDnsServicesList();
        getFragmentManager().beginTransaction()
                .add(R.id.container, servicesList, FRAGMENT_TAG).commit();

        startDiscovery();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initDnsSd() {
        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName
                                .equalsIgnoreCase(aDropDnsService.SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.
                            aDropDnsServicesList fragment = (aDropDnsServicesList) getFragmentManager()
                                    .findFragmentByTag(FRAGMENT_TAG);
                            if (fragment != null) {
                                aDropDnsServicesList.WiFiDevicesAdapter adapter = ((aDropDnsServicesList.WiFiDevicesAdapter) fragment
                                        .getListAdapter());
                                aDropDnsService service = new aDropDnsService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;
                                Log.d(TAG, "recrod=" + profiles.toString());
                                String userName = profiles
                                        .get(srcDevice.deviceName);
                                if (userName != null) {
                                    service.userName = userName;
                                }
                                adapter.add(service);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "onBonjourServiceAvailable "
                                        + instanceName);
                            } else {
                                Log.d(TAG, "?? no fragment to add device");
                            }
                        }

                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG, "TxtRecord");
                        Log.d(TAG,
                                device.deviceName
                                        + " is "
                                        + record.get(aDropDnsService.TXTRECORD_PROP_AVAILABLE));
                        String userName = record
                                .get(aDropDnsService.USER_NAME);
                        Log.d(TAG, "user = " + userName);
                        if (userName != null) {
                            profiles.put(device.deviceName, userName);
                        }
                        Log.d(TAG, "recrod=" + record.toString());
                    }
                });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(TAG, "Failed adding service discovery request");
                    }
                });

    }

    private void startDiscovery() {
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(TAG, "Service discovery failed");

            }
        });
    }

    private void stopDiscovery() {
        if (serviceRequest != null) {
            manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Stop Service discovery.");
                }

                @Override
                public void onFailure(int arg0) {
                    Log.d(TAG, "Failed to Stop Service discovery.");
                }
            });
        }
    }

    @Override
    public void onChannelDisconnected() {
        Log.d(TAG, "Channel disconnected");
    }
}

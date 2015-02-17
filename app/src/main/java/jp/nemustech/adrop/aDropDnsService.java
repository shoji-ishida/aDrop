package jp.nemustech.adrop;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by ishida on 15/02/15.
 */
public class aDropDnsService {
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String USER_NAME = "userName";
    public static final String SERVICE_INSTANCE = "aDrop";
    public static final String SERVICE_REG_TYPE = "_adrop._tcp";

    WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;
    String userName = "No name";
}

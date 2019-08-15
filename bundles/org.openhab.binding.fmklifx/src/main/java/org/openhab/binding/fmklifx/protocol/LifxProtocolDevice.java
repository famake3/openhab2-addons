package org.openhab.binding.fmklifx.protocol;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LifxProtocolDevice {

    final long id;

    final Map<Byte, RequestResponseHandler> requestResponseHandlers;
    byte sequenceNumber;

    InetAddress ipAddress;
    final public ArrayList<DeviceListener> deviceListeners = new ArrayList<DeviceListener>();

    public interface CallDeviceListener {
        public void call(DeviceListener dl);
    }

    public LifxProtocolDevice(long id) {
        this.requestResponseHandlers = new HashMap<>();
        this.id = id;
    }

    public String idString() {
        return idString(id);
    }

    public static String idString(long id) {
        return Long.toHexString(id);
    }

    public synchronized void addDeviceListener(DeviceListener dl) {
        deviceListeners.add(dl);
    }

    public synchronized boolean removeDeviceListener(DeviceListener dl) {
        deviceListeners.remove(dl);
        return deviceListeners.isEmpty();
    }

    public synchronized void callDeviceListeners(CallDeviceListener callable) {
        ArrayList<DeviceListener> a = new ArrayList<>(deviceListeners); // Make a copy, as the original
                                                                        // may be changed
        for (DeviceListener dl : a) {
            callable.call(dl);
        }
    }

}

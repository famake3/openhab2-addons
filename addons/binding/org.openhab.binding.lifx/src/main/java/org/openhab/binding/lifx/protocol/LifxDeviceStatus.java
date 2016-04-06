package org.openhab.binding.lifx.protocol;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class LifxDeviceStatus {

    final long id;

    final Map<Byte, RequestResponseHandler> requestResponseHandlers;
    byte sequenceNumber;

    InetAddress ipAddress;
    final DeviceListener deviceListener;

    public LifxDeviceStatus(long id, DeviceListener dl) {
        this.deviceListener = dl;
        this.requestResponseHandlers = new HashMap<>();
        this.id = id;
    }

    public String idString() {
        return idString(id);
    }

    public static String idString(long id) {
        return Long.toHexString(id);
    }

}

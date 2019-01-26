package org.openhab.binding.fmklifx.protocol;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class LifxProtocolDevice {

    final long id;

    final Map<Byte, RequestResponseHandler> requestResponseHandlers;
    byte sequenceNumber;

    InetAddress ipAddress;
    final DeviceListener deviceListener;

    public LifxProtocolDevice(long id, DeviceListener dl) {
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

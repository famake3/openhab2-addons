package org.openhab.binding.lifx.protocol;

import java.net.InetAddress;

public class LifxDeviceStatus {

    // Response / ack tracking
    boolean pendingResponse = false, superseded = false;
    short expectedResponseType;
    LanProtocolPacket response;
    byte sequenceNumber;

    InetAddress ipAddress;
    final DeviceListener deviceListener;

    LifxDeviceStatus(DeviceListener dl) {
        this.deviceListener = dl;
    }

}

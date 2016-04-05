package org.openhab.binding.lifx.protocol;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LifxDeviceStatus {

    final long id;

    final List<LanProtocolPacket> responses;
    final Set<Byte> expectedSequenceNumbers;
    byte sequenceNumber;

    InetAddress ipAddress;
    final DeviceListener deviceListener;

    public LifxDeviceStatus(long id, DeviceListener dl) {
        this.deviceListener = dl;
        this.expectedSequenceNumbers = new HashSet<>();
        this.responses = new LinkedList<>();
        this.id = id;
    }

    public String idString() {
        return Long.toHexString(id);
    }

}

package org.openhab.binding.lifx.protocol;

import java.io.IOException;
import java.net.InetAddress;

interface PacketSender {

    // Note: will send broadcast if destination is null
    void send(InetAddress destination, LanProtocolPacket packet) throws IOException;

    void sendDiscoveryPacket() throws IOException;

}

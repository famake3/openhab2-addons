package org.openhab.binding.lifx.protocol;

import java.net.InetAddress;

interface PacketSender {

    void send(InetAddress destination, LanProtocolPacket packet);

}

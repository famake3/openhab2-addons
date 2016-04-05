package org.openhab.binding.lifx.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestResponseHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(LanProtocolService.class);

    private static final long TIMEOUT = 1000;
    private static final int IP_ATTEMPTS = 2, BCAST_ATTEMPTS = 2;

    private final LanProtocolPacket requestPacket;
    private final LifxDeviceStatus device;
    private final PacketSender packetSender;
    private boolean responsePacketReceived;

    public RequestResponseHandler(LifxDeviceStatus device, LanProtocolPacket packet, PacketSender packetSender) {
        this.requestPacket = packet;
        this.device = device;
        this.packetSender = packetSender;
    }

    @Override
    public synchronized void run() {
        for (int i = 0; i < (IP_ATTEMPTS + BCAST_ATTEMPTS) && responsePacketReceived; ++i) {
            long nextTimeout = System.currentTimeMillis() + TIMEOUT;
            long remain = System.currentTimeMillis() - nextTimeout;
            while (responsePacketReceived && remain > 0) {
                try {
                    wait(remain);
                } catch (InterruptedException e) {
                    logger.error("The reliable delivery thread was interrupted!", e);
                    return;
                }
            }
            if (responsePacketReceived) { // Timeout! Re-send.
                logger.debug("No response from bulb " + device.idString() + ", resending...");
                if (i > IP_ATTEMPTS) {
                    device.ipAddress = null;
                }
                if (device.ipAddress == null) {
                    packetSender.sendBroadcast(requestPacket);
                } else {
                    packetSender.send(device.ipAddress, requestPacket);
                }
            }
        }
        if (responsePacketReceived) {
            device.deviceListener.timeout();
        } else {
            logger.info("No response from bulb " + device.idString() + ", giving up.");
        }
    }

    public synchronized void packet() {
        responsePacketReceived = true;
        notifyAll();
    }

}

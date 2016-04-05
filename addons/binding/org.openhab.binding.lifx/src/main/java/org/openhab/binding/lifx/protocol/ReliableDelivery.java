package org.openhab.binding.lifx.protocol;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReliableDelivery implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(LanProtocolService.class);

    private static final long TIMEOUT = 1000;
    private static final int ATTEMPTS = 3;

    private final LanProtocolPacket packet;
    private final LifxDeviceStatus device;
    private final PacketSender packetSender;

    public ReliableDelivery(LifxDeviceStatus device, LanProtocolPacket packet, PacketSender packetSender) {
        this.packet = packet;
        this.device = device;
        this.packetSender = packetSender;

        device.expectedSequenceNumbers.add(packet.getSequence());
    }

    @Override
    public void run() {
        synchronized (device) {
            try {
                boolean foundReply = false;
                for (int i = 0; i < ATTEMPTS && !foundReply; ++i) {
                    long nextTimeout = System.currentTimeMillis() + TIMEOUT;
                    long remain = System.currentTimeMillis() - nextTimeout;
                    while (!foundReply && remain > 0) {

                        if (checkForCompletion()) {
                            foundReply = true;
                        } else {
                            try {
                                device.wait(remain);
                            } catch (InterruptedException e) {
                                logger.error("The reliable delivery thread was interrupted!", e);
                                return;
                            }
                        }
                    }
                    if (!foundReply) { // Timeout! Re-send.
                        logger.debug("No response from bulb " + device.idString() + ", resending...");

                    }
                }
            } finally {
                device.expectedSequenceNumbers.remove(packet.getSequence());
            }
        }
    }

    private boolean checkForCompletion() {
        for (Iterator<LanProtocolPacket> iterator = device.responses.iterator(); iterator.hasNext();) {
            LanProtocolPacket rpacket = iterator.next();
            if (rpacket.getSequence() == packet.getSequence()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

}

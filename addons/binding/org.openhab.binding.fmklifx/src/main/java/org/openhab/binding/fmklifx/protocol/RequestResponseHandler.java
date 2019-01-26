package org.openhab.binding.fmklifx.protocol;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestResponseHandler implements Runnable {

    /**
     * Class for reliable message delivery, with timeout if no
     * reply is received.
     */

    private final Logger logger = LoggerFactory.getLogger(RequestResponseHandler.class);

    private static final long TIMEOUT = 2000;
    private static final int NUM_ATTEMPTS = 3;

    private static final int DISCOVERY_AFTER_ATTEMPTS = 1;

    private final Thread thread;
    private final LanProtocolPacket requestPacket;
    private final LifxProtocolDevice device;
    private final PacketSender packetSender;
    private volatile boolean okTerminate;

    public RequestResponseHandler(LifxProtocolDevice device, LanProtocolPacket packet, PacketSender packetSender) {
        this.requestPacket = packet;
        this.device = device;
        this.packetSender = packetSender;
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
    }

    public void start() {
        try {
            packetSender.send(device.ipAddress, requestPacket);
        } catch (IOException e) {
            logger.error("Exception while trying to send packet, will try again", e);
        }
        thread.start();
    }

    @Override
    public synchronized void run() {
        for (int i = 0; i < NUM_ATTEMPTS && (!okTerminate); ++i) {
            if (i == DISCOVERY_AFTER_ATTEMPTS) {
                try {
                    packetSender.sendDiscoveryPacket();
                } catch (IOException e) { // Best effort recovery; nothing we can do
                }
            }
            long nextTimeout = System.currentTimeMillis() + TIMEOUT;
            long remain = TIMEOUT;
            while (!okTerminate && remain > 0) {
                try {
                    wait(remain);
                    remain = nextTimeout - System.currentTimeMillis();
                } catch (InterruptedException e) {
                    logger.error("The reliable delivery thread was interrupted!", e);
                    return;
                }
            }
            if (!okTerminate) { // Timeout! Re-send.
                logger.debug("No response from bulb " + device.idString() + ", resending...");
                try {
                    packetSender.send(device.ipAddress, requestPacket);
                } catch (IOException e) {
                    logger.error("Exception while trying to send packet", e);
                }
            }
        }
        if (!okTerminate) {
            device.deviceListener.timeout();
            logger.info("No response from bulb " + device.idString() + ", giving up.");
        }
    }

    public synchronized void packet() {
        okTerminate = true;
        notifyAll();
    }

    public synchronized void superseded() {
        okTerminate = true;
        notifyAll();
    }

    public LanProtocolPacket getRequestPacket() {
        return requestPacket;
    }
}

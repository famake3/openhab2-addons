package org.openhab.binding.lifx.protocol;

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
    private static final int UNICAST_ATTEMPTS = 2, BCAST_ATTEMPTS = 2;

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
        for (int i = 0; i < (UNICAST_ATTEMPTS + BCAST_ATTEMPTS) && (!okTerminate); ++i) {
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
                if (i > UNICAST_ATTEMPTS) {
                    device.ipAddress = null;
                }
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

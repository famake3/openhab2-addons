package org.openhab.binding.lifx.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanProtocolService implements Runnable, PacketSender {

    /**
     * Singleton class to manage protocol I/O (main network loop,
     * packet encoding, etc.)
     *
     * LIFX protocol spec:
     * http://lan.developer.lifx.com/docs/introduction
     */

    private Logger logger = LoggerFactory.getLogger(LanProtocolService.class);

    private final static int LIFX_PORT = 56700;

    // Not all these types are requested, or used explicitly, but we
    // accept some extra ones in the hope to pick up traffic from other
    // clients (e.g. the app).
    private static final int TYPE_GET_SERVICE = 2, TYPE_STATE_SERVICE = 3;
    private static final int TYPE_GET_LABEL = 23, TYPE_STATE_LABEL = 25;
    private static final int TYPE_STATE_POWER = 22;
    private static final int TYPE_ACK = 45;
    private static final int TYPE_ECHO_RESPONSE = 59;
    private static final int LIGHT_GET = 101, LIGHT_SET_COLOR = 102, LIGHT_STATE = 107;
    private static final int LIGHT_SET_POWER = 117, LIGHT_STATE_POWER = 118;

    private static LanProtocolService instance;

    private final DatagramSocket socket;
    private final int clientId;

    private final Map<Long, LifxDeviceStatus> deviceMap;

    private LanProtocolService() throws SocketException {
        socket = new DatagramSocket(LIFX_PORT);
        socket.setBroadcast(true);

        clientId = (new Random()).nextInt();
        deviceMap = new HashMap<>();

        Thread receiveThread = new Thread(this);
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public synchronized static LanProtocolService getInstance() throws SocketException {
        if (instance == null) {
            instance = new LanProtocolService();
        }
        return instance;
    }

    @Override
    public void run() {
        // Background receiver thread
        while (true) {
            DatagramPacket p = new DatagramPacket(new byte[1500], 1500);
            try {
                socket.receive(p);
                LanProtocolPacket lpp = LanProtocolPacket.decode(p.getData(), p.getLength());

                LifxDeviceStatus status = null;
                synchronized (this) {
                    status = deviceMap.get(lpp.getTarget());
                    if (status != null) {
                        // Message from known device
                        deviceMessageReceived(status, p.getAddress(), lpp);
                    } else {
                        if (lpp.getMessageType() == TYPE_STATE_SERVICE) {
                            // Report discovery result
                        }
                    }
                }

            } catch (IOException e) {
                logger.error("Failed to receive packet, will try again in 60 seconds", e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                }
                continue;
            }
        }
    }

    private void deviceMessageReceived(LifxDeviceStatus status, InetAddress inetAddress, LanProtocolPacket lpp)
            throws PacketFormatException {
        synchronized (status) {
            status.ipAddress = inetAddress;
            if (status.expectedSequenceNumbers.contains(lpp.getSequence())) {
                status.responses.add(lpp);
                status.notifyAll();
            }
        }
        callDeviceEventListener(status, lpp);
    }

    private void callDeviceEventListener(LifxDeviceStatus status, LanProtocolPacket lpp) throws PacketFormatException {
        int type = lpp.getMessageType();
        if (type == TYPE_STATE_SERVICE || type == TYPE_ECHO_RESPONSE || type == TYPE_ACK) {
            status.deviceListener.ping();
        } else if (type == LIGHT_STATE) {
            status.deviceListener.color(LifxColor.decode(lpp.getPayload()));
        } else if (type == TYPE_STATE_POWER || type == LIGHT_STATE_POWER) {
            try {
                status.deviceListener.power(lpp.getPayload()[0] != 0);
            } catch (IndexOutOfBoundsException e) {
                throw new PacketFormatException("Too short", e);
            }
        } else if (type == TYPE_STATE_LABEL) {
            status.deviceListener.label(new String(lpp.getPayload()));
        }
    }

    public synchronized void registerDeviceListener(long id, DeviceListener dl) {
        deviceMap.put(id, new LifxDeviceStatus(id, dl));
    }

    public synchronized void unregisterDeviceListener(long id) {
        deviceMap.remove(id);
    }

    public void setColor(long id, LifxColor color) {

    }

    public void setPower(long id, LifxColor color) {

    }

    public void queryLightState(long id) {

    }

    public void startDiscovery() {

    }

    @Override
    public void send(InetAddress destination, LanProtocolPacket packet) {
        // TODO Auto-generated method stub

    }
}

package org.openhab.binding.lifx.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
    private static final short TYPE_GET_SERVICE = 2, TYPE_STATE_SERVICE = 3;
    private static final short TYPE_GET_LABEL = 23, TYPE_STATE_LABEL = 25;
    private static final short TYPE_STATE_POWER = 22;
    private static final short TYPE_ACK = 45;
    private static final short TYPE_ECHO_RESPONSE = 59;
    private static final short TYPE_LIGHT_GET = 101, TYPE_LIGHT_SET_COLOR = 102, LIGHT_STATE = 107;
    private static final short TYPE_LIGHT_SET_POWER = 117, LIGHT_STATE_POWER = 118;

    private static LanProtocolService instance;

    private final InetAddress broadcastAddress;

    private final DatagramSocket socket;
    private final int clientId;

    private final Map<Long, LifxDeviceStatus> deviceMap;

    private LanProtocolService() throws SocketException {
        socket = new DatagramSocket(LIFX_PORT);
        socket.setBroadcast(true);

        clientId = (new Random()).nextInt();
        deviceMap = new HashMap<>();

        try {
            broadcastAddress = InetAddress.getByAddress(new byte[] { -1, -1, -1, -1 });
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to instantiate the broadcast address!", e);
        }

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
                        status.ipAddress = p.getAddress();
                        RequestResponseHandler rrHandler = status.requestResponseHandlers.remove(lpp.getSequence());
                        if (rrHandler != null) {
                            rrHandler.packet();
                        }
                        // Note: calls device listener even if there is no request for this sequence number.
                        // Any data may be useful for staying up to date.
                        callDeviceEventListener(status, lpp);
                    } else {
                        if (lpp.getMessageType() == TYPE_STATE_SERVICE) {
                            // Report discovery result
                        }
                    }
                }

            } catch (IOException e) {
                logger.error("Failed to receive something from network, will try again in 60 seconds", e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                }
                continue;
            }
        }
    }

    static void callDeviceEventListener(LifxDeviceStatus status, LanProtocolPacket lpp) throws PacketFormatException {
        int type = lpp.getMessageType();
        if (type == TYPE_STATE_SERVICE || type == TYPE_ECHO_RESPONSE || type == TYPE_ACK) {
            status.deviceListener.ping();
        } else if (type == LIGHT_STATE) {
            ;
            processLightStateMessage(status.deviceListener, lpp);
        } else if (type == TYPE_STATE_POWER || type == LIGHT_STATE_POWER) {
            try {
                status.deviceListener.power(lpp.getPayload()[0] != 0);
            } catch (IndexOutOfBoundsException e) {
                throw new PacketFormatException("Too short", e);
            }
        } else if (type == TYPE_STATE_LABEL) {
            status.deviceListener.label(new String(lpp.getPayload()).trim());
        }
    }

    private static void processLightStateMessage(DeviceListener deviceListener, LanProtocolPacket lpp)
            throws PacketFormatException {
        ByteBuffer bb = ByteBuffer.wrap(lpp.getPayload());
        deviceListener.color(LifxColor.decodeFrom(bb));
        bb.getShort(); // Reserved
        deviceListener.power(bb.getShort() != 0);
        byte[] label = new byte[32];
        bb.get(label);
        deviceListener.label(new String(label));
    }

    public synchronized LifxDeviceStatus registerDeviceListener(long id, DeviceListener dl) {
        LifxDeviceStatus deviceStatus = new LifxDeviceStatus(id, dl);
        deviceMap.put(id, deviceStatus);
        return deviceStatus;
    }

    public synchronized void unregisterDeviceListener(long id) {
        deviceMap.remove(id);
    }

    public synchronized void setColor(LifxDeviceStatus deviceStatus, int duration, LifxColor color) {
        ByteBuffer payload = ByteBuffer.wrap(new byte[21]);
        payload.put((byte) 0);
        color.encodeInto(payload);
        payload.putInt(duration);
        sendAndExpectReply(deviceStatus, TYPE_LIGHT_SET_COLOR, true, payload.array());
    }

    public synchronized void setPower(LifxDeviceStatus deviceStatus, int duration, boolean power) {
        ByteBuffer payload = ByteBuffer.wrap(new byte[6]);
        payload.putShort(power ? (short) 0xFFFF : 0);
        payload.putInt(duration);
        sendAndExpectReply(deviceStatus, TYPE_LIGHT_SET_POWER, true, payload.array());
    }

    public synchronized void queryLightState(LifxDeviceStatus deviceStatus) {
        sendAndExpectReply(deviceStatus, TYPE_LIGHT_GET, false, new byte[0]);
    }

    public synchronized void queryLabel(LifxDeviceStatus deviceStatus) {
        sendAndExpectReply(deviceStatus, TYPE_GET_LABEL, false, new byte[0]);
    }

    private void sendAndExpectReply(LifxDeviceStatus deviceStatus, short type, boolean responseTypeAck,
            byte[] payload) {
        byte seq = deviceStatus.sequenceNumber++;
        LanProtocolPacket cmdPacket = new LanProtocolPacket(clientId, false, responseTypeAck, !responseTypeAck, seq,
                deviceStatus.id, type, payload);
        RequestResponseHandler rrHandler = new RequestResponseHandler(deviceStatus, cmdPacket, this);
        deviceStatus.requestResponseHandlers.put(seq, rrHandler);
    }

    public synchronized void startDiscovery() throws IOException {
        LanProtocolPacket discoveryPacket = new LanProtocolPacket(clientId, true, false, true, (byte) 0, 0,
                TYPE_GET_SERVICE, new byte[] {});
        send(null, discoveryPacket);
    }

    @Override
    public void send(InetAddress destination, LanProtocolPacket packet) throws IOException {
        byte[] packetData = packet.getData();
        DatagramPacket datagramPacket = new DatagramPacket(packetData, packetData.length);
        if (destination != null) {
            datagramPacket.setAddress(destination);
        } else {
            datagramPacket.setAddress(broadcastAddress);
        }
        socket.send(datagramPacket);
    }

}

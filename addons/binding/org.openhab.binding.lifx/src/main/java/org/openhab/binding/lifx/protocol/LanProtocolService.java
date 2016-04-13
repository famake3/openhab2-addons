package org.openhab.binding.lifx.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

    @SuppressWarnings("serial")
    public static final HashMap<Integer, String> LIFX_TYPE_DESCRIPTIONS = new HashMap<Integer, String>() {
        {
            put(1, "Original 1000");
            put(3, "Color 650");
            put(10, "White 800");
            put(11, "White 800");
            put(18, "White 900");
            put(20, "Color 1000");
            put(22, "Color 1000");
        }
    };
    public static final Set<Integer> LIFX_COLOR_TYPE_IDS = new HashSet<Integer>(Arrays.asList(1, 3, 20, 22));
    public static final Set<Integer> LIFX_WHITE_TYPE_IDS = new HashSet<Integer>(Arrays.asList(10, 11, 18));

    private Logger logger = LoggerFactory.getLogger(LanProtocolService.class);

    private final static int LIFX_PORT = 56700;

    // Not all these types are requested, or used explicitly, but we
    // accept some extra ones in the hope to pick up traffic from other
    // clients (e.g. the app).
    private static final short TYPE_GET_SERVICE = 2, TYPE_STATE_SERVICE = 3;
    private static final short TYPE_GET_LABEL = 23, TYPE_STATE_LABEL = 25;
    private static final short TYPE_STATE_POWER = 22;
    private static final short TYPE_GET_VERSION = 32, TYPE_STATE_VERSION = 33;
    private static final short TYPE_ACK = 45;
    private static final short TYPE_ECHO_RESPONSE = 59;
    private static final short TYPE_LIGHT_GET = 101, TYPE_LIGHT_SET_COLOR = 102, LIGHT_STATE = 107;
    private static final short TYPE_LIGHT_SET_POWER = 117, LIGHT_STATE_POWER = 118;

    private static LanProtocolService instance;

    private final InetAddress broadcastAddress;

    private final DatagramSocket socket;
    private final int clientId;

    private final Map<Long, LifxProtocolDevice> deviceMap;
    private final Set<LifxDiscoveryListener> discoveryListeners;

    private LanProtocolService() throws SocketException {
        socket = new DatagramSocket(LIFX_PORT);
        socket.setBroadcast(true);

        clientId = (new Random()).nextInt();
        deviceMap = new HashMap<>();
        discoveryListeners = new HashSet<>();

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

                LifxProtocolDevice device = null;
                synchronized (this) {
                    device = deviceMap.get(lpp.getTarget());
                    if (device != null) {
                        // Message from known device
                        device.ipAddress = p.getAddress();
                        RequestResponseHandler rrHandler = device.requestResponseHandlers.remove(lpp.getSequence());
                        if (rrHandler != null) {
                            rrHandler.packet();
                        }
                        // Note: calls device listener even if there is no request for this sequence number.
                        // Any data may be useful for staying up to date.
                        callDeviceEventListener(device, lpp);
                    } else {
                        if (lpp.getMessageType() == TYPE_STATE_SERVICE) {
                            // Report discovery result
                            for (LifxDiscoveryListener dl : discoveryListeners) {
                                dl.deviceDiscovered(lpp.getTarget());
                            }
                        }
                    }
                }

            } catch (IOException e) {
                logger.error("Failed to receive something from network, will try again in 60 seconds", e);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    break;
                }
                continue;
            }
        }
    }

    static void callDeviceEventListener(LifxProtocolDevice device, LanProtocolPacket lpp) throws PacketFormatException {
        int type = lpp.getMessageType();
        if (type == TYPE_STATE_SERVICE || type == TYPE_ECHO_RESPONSE || type == TYPE_ACK) {
            device.deviceListener.ping();
        } else if (type == LIGHT_STATE) {
            ;
            processLightStateMessage(device.deviceListener, lpp);
        } else if (type == TYPE_STATE_POWER || type == LIGHT_STATE_POWER) {
            try {
                device.deviceListener.power(lpp.getPayload()[0] != 0);
            } catch (IndexOutOfBoundsException e) {
                throw new PacketFormatException("Too short", e);
            }
        } else if (type == TYPE_STATE_LABEL) {
            device.deviceListener.label(new String(lpp.getPayload()).trim());
        } else if (type == TYPE_STATE_VERSION) {
            processVersionMessage(device.deviceListener, lpp);
        }
    }

    private static void processLightStateMessage(DeviceListener deviceListener, LanProtocolPacket lpp)
            throws PacketFormatException {
        ByteBuffer bb = ByteBuffer.wrap(lpp.getPayload());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        deviceListener.color(LifxColor.decodeFrom(bb));
        bb.getShort(); // Reserved
        deviceListener.power(bb.getShort() != 0);
        byte[] label = new byte[32];
        bb.get(label);
        deviceListener.label(new String(label));
    }

    private static void processVersionMessage(DeviceListener deviceListener, LanProtocolPacket lpp) {
        ByteBuffer bb = ByteBuffer.wrap(lpp.getPayload());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int vendor = bb.getInt();
        int product = bb.getInt();
        int version = bb.getInt();
        deviceListener.version(vendor, product, version);
    }

    public synchronized LifxProtocolDevice registerDeviceListener(long id, DeviceListener dl) {
        LifxProtocolDevice device = new LifxProtocolDevice(id, dl);
        deviceMap.put(id, device);
        return device;
    }

    public synchronized void unregisterDeviceListener(long id) {
        deviceMap.remove(id);
    }

    public synchronized void registerDiscoveryListener(LifxDiscoveryListener dl) {
        discoveryListeners.add(dl);
    }

    public synchronized void removeDiscoveryListener(LifxDiscoveryListener dl) {
        discoveryListeners.remove(dl);
    }

    public void supersedeCommand(LifxProtocolDevice device, short type) {
        for (Iterator<Map.Entry<Byte, RequestResponseHandler>> it = device.requestResponseHandlers.entrySet()
                .iterator(); it.hasNext();) {
            Map.Entry<Byte, RequestResponseHandler> entry = it.next();
            if (entry.getValue().getRequestPacket().getMessageType() == type) {
                it.remove();
                entry.getValue().superseded();
            }
        }
    }

    public synchronized void setColor(LifxProtocolDevice device, int duration, LifxColor color) {
        supersedeCommand(device, TYPE_LIGHT_SET_COLOR);
        ByteBuffer payload = ByteBuffer.wrap(new byte[13]);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.put((byte) 0);
        color.encodeInto(payload);
        payload.putInt(duration);
        sendAndExpectReply(device, TYPE_LIGHT_SET_COLOR, true, payload.array());
    }

    public synchronized void setPower(LifxProtocolDevice device, int duration, boolean power) {
        supersedeCommand(device, TYPE_LIGHT_SET_POWER);
        ByteBuffer payload = ByteBuffer.wrap(new byte[6]);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putShort(power ? (short) 0xFFFF : 0);
        payload.putInt(duration);
        sendAndExpectReply(device, TYPE_LIGHT_SET_POWER, true, payload.array());
    }

    public synchronized void queryLightState(LifxProtocolDevice device) {
        sendAndExpectReply(device, TYPE_LIGHT_GET, false, new byte[0]);
    }

    public synchronized void queryLabel(LifxProtocolDevice device) {
        sendAndExpectReply(device, TYPE_GET_LABEL, false, new byte[0]);
    }

    public synchronized void queryVersion(LifxProtocolDevice device) {
        sendAndExpectReply(device, TYPE_GET_VERSION, false, new byte[0]);
    }

    private void sendAndExpectReply(LifxProtocolDevice device, short type, boolean responseTypeAck, byte[] payload) {
        byte seq = device.sequenceNumber++;
        LanProtocolPacket cmdPacket = new LanProtocolPacket(clientId, false, responseTypeAck, !responseTypeAck, seq,
                device.id, type, payload);
        RequestResponseHandler rrHandler = new RequestResponseHandler(device, cmdPacket, this);
        device.requestResponseHandlers.put(seq, rrHandler);
        rrHandler.start();
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
        datagramPacket.setPort(LIFX_PORT);
        if (destination != null) {
            datagramPacket.setAddress(destination);
        } else {
            datagramPacket.setAddress(broadcastAddress);
        }
        socket.send(datagramPacket);
    }

}

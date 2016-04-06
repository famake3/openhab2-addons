package org.openhab.binding.lifx.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LanProtocolPacket {

    /**
     * Protocol basic message.
     *
     */

    // Size in bytes: Frame=8 + Frame address=16 + Protocol header=12
    private static final int HEADER_LENGTH = 8 + 16 + 12;
    private static final short TAGGED = 0x2000; // Broadcast scan flag
    private static final short ADDRESSABLE = 0x1000; // Always set to one
    private static final short PROTOCOL_VERSION = 1024; // As stated in docs
    private static final byte ACK_REQUIRED = 2;
    private static final byte RES_REQUIRED = 1;

    private boolean tagged, ackRequired, resRequired;
    private byte sequence;
    private long target;
    private byte[] payload;
    private short messageType;

    private int source;

    private LanProtocolPacket() {
    }

    public LanProtocolPacket(int source, boolean tagged, boolean ackRequired, boolean resRequired, byte sequence,
            long target, short messageType, byte[] payload) {
        this.source = source;
        this.tagged = tagged;
        this.ackRequired = ackRequired;
        this.resRequired = resRequired;
        this.sequence = sequence;
        this.target = target;
        this.messageType = messageType;
        this.payload = payload;
    }

    public byte[] getData() {
        // Create new packet
        byte[] data = new byte[HEADER_LENGTH + payload.length];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // --- Frame ---
        bb.putShort((short) (HEADER_LENGTH + payload.length));
        // This field consists of flags in the top 4 bits, then the protocol version
        // All but the "tagged" flag are specified as constants in the current
        // protocol. Protocol spec is ambiguous on the exact placements of bits before/
        // after little endian conversion, the below reconstructed from a packet dump.
        short header = (short) (ADDRESSABLE + PROTOCOL_VERSION);
        if (tagged) {
            header |= TAGGED;
        }
        bb.putShort(header);
        bb.putInt(source);

        // --- Frame Address ---
        bb.putLong(target);
        for (int i = 0; i < 3; ++i) {
            bb.putShort((short) 0);
        }
        byte responseCodes = 0;
        if (ackRequired) {
            responseCodes |= ACK_REQUIRED;
        }
        if (resRequired) {
            responseCodes |= RES_REQUIRED;
        }
        bb.put(responseCodes);
        bb.put(sequence);

        // Protocol header
        bb.putLong(0); // Reserved
        bb.putShort(messageType);
        bb.putShort((short) 0);
        return bb.array();
    }

    public static LanProtocolPacket decode(byte[] data, int length) throws PacketFormatException {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int protocolLength = bb.getShort() & 0xFFFF;
        if (length != protocolLength) {
            throw new PacketFormatException("Length field does not match packet length");
        }
        short header = bb.getShort();
        LanProtocolPacket result = new LanProtocolPacket();
        result.tagged = (header & TAGGED) == TAGGED;
        int vers = header & 0x0FFF;
        if (vers != 1024) {
            throw new PacketFormatException("Protocol version " + vers + "not understood");
        }
        result.source = bb.getInt();
        result.target = bb.getLong();
        bb.getShort();
        bb.getShort();
        bb.getShort();
        byte responseCodes = bb.get();
        result.ackRequired = (responseCodes & ACK_REQUIRED) == ACK_REQUIRED;
        result.resRequired = (responseCodes & RES_REQUIRED) == RES_REQUIRED;
        result.sequence = bb.get();
        bb.getLong();
        result.messageType = bb.getShort();
        bb.getShort();
        result.payload = new byte[length - HEADER_LENGTH];
        System.arraycopy(data, HEADER_LENGTH, result.payload, 0, length - HEADER_LENGTH);
        return result;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    public boolean isAckRequired() {
        return ackRequired;
    }

    public void setAckRequired(boolean ackRequired) {
        this.ackRequired = ackRequired;
    }

    public boolean isResRequired() {
        return resRequired;
    }

    public void setResRequired(boolean resRequired) {
        this.resRequired = resRequired;
    }

    public byte getSequence() {
        return sequence;
    }

    public void setSequence(byte sequence) {
        this.sequence = sequence;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public short getMessageType() {
        return messageType;
    }

    public void setMessageType(short messageType) {
        this.messageType = messageType;
    }
}

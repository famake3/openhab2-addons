package org.openhab.binding.lifx.handler;

import java.nio.ByteBuffer;

public class LanProtocolFrame {

    /**
     * Protocol codec; implements basic protocol message.
     *
     * To be subclassed.
     */

    private final boolean tagged, ack_required, res_required;
    private final int source;
    private final byte sequence;
    private final byte[] payload;

    public LanProtocolFrame(boolean tagged, int source, boolean ack_required, boolean res_required, byte sequence,
            byte[] payload) {
        this.tagged = tagged;
        this.source = source;
        this.ack_required = ack_required;
        this.res_required = res_required;
        this.sequence = sequence;
        this.payload = payload;
    }

    public LanProtocolFrame(byte[] packetData) {
        tagged = false;
        ack_required = false;
        res_required = false;
        source = 0;
        sequence = 0;
        payload = null;
    }

    public byte[] getPacket() {
        // Create new packet
        byte[] data = new byte[1];
        ByteBuffer bb = ByteBuffer.wrap(data);
        return null;
    }

}

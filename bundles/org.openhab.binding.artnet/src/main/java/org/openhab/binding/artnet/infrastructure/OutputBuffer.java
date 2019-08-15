package org.openhab.binding.artnet.infrastructure;

public class OutputBuffer {

    public final byte[] data;

    public OutputBuffer(int size) {
        this.data = new byte[size];
    }

}

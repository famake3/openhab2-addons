package org.openhab.binding.fmklifx.protocol;

import java.io.IOException;

public class PacketFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    public PacketFormatException(String message) {
        super(message);
    }

    public PacketFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}

package org.openhab.binding.lifx.protocol;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class LifxColor {

    public double hue, saturation, brightness, colorTemperature;

    private LifxColor() {
    }

    public LifxColor(double hue, double saturation, double brightness, double colorTemperature) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.colorTemperature = colorTemperature;
    }

    public static LifxColor decodeFrom(ByteBuffer bb) throws PacketFormatException {
        try {
            LifxColor color = new LifxColor();
            color.hue = (bb.getShort() & 65535) * 1.0 / 65535;
            color.saturation = (bb.getShort() & 65535) * 1.0 / 65535;
            color.brightness = (bb.getShort() & 65535) * 1.0 / 65535;
            color.colorTemperature = (bb.getShort() & 65535) * 1.0;
            return color;
        } catch (BufferOverflowException e) {
            throw new PacketFormatException("Payload is too short", e);
        }
    }

    public void encodeInto(ByteBuffer bb) {
        bb.putShort((short) (hue * 65535));
        bb.putShort((short) (saturation * 65535));
        bb.putShort((short) (brightness * 65535));
        bb.putShort((short) (colorTemperature));
    }

}

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
            color.hue = (bb.getShort() & 0xFFFF) * 1.0 / 0xFFFF;
            color.saturation = (bb.getShort() & 0xFFFF) * 1.0 / 0xFFFF;
            color.brightness = (bb.getShort() & 0xFFFF) * 1.0 / 0xFFFF;
            color.colorTemperature = (bb.getShort() & 0xFFFF) * 1.0 / 0xFFFF;
            return color;
        } catch (BufferOverflowException e) {
            throw new PacketFormatException("Payload is too short", e);
        }
    }

    public void encodeInto(ByteBuffer bb) {
        bb.putShort((short) (hue * 0xFFFF));
        bb.putShort((short) (saturation * 0xFFFF));
        bb.putShort((short) (brightness * 0xFFFF));
        bb.putShort((short) (colorTemperature * 0xFFFF));
    }

}

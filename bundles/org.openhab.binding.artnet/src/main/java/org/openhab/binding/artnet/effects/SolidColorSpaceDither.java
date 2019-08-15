package org.openhab.binding.artnet.effects;

import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;
import org.openhab.binding.artnet.infrastructure.RgbColor;

public class SolidColorSpaceDither extends Layer {

    private boolean generated = false;
    private RgbColor color;

    public SolidColorSpaceDither(Model model, RgbColor color) {
        super(model);
        this.color = color;
    }

    @Override
    public Layer update(long timeCode) {
        if (!generated) {
            generateRandom(data, color.r, color.g, color.b);
        }
        return null;
    }

    private byte[] generateRandom(byte[] data, double r, double g, double b) {
        int ir = (int) r, ig = (int) g, ib = (int) b;
        double rx = r - ir, gx = g - ig, bx = b - ib;
        for (int i = 0; i < data.length; ++i) {
            data[i * 3] = (byte) (ir + (Math.random() < rx ? 1 : 0));
            data[i * 3 + 1] = (byte) (ig + (Math.random() < gx ? 1 : 0));
            data[i * 3 + 2] = (byte) (ib + (Math.random() < bx ? 1 : 0));
        }
        return data;
    }

}

package org.openhab.binding.artnet.effects;

import java.awt.Frame;

import org.openhab.binding.artnet.infrastructure.Color;
import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;

public class SolidColorSpaceDither extends Layer {

    private boolean generated = false;
    private Color color;

    public SolidColorSpaceDither(Model model, Color color) {
        super(model);
        generate(color);
    }

    @Override
    public Layer update(long timeCode, Frame frame) {
        if (!generated) {
        }
        return null;
    }

    private void generate(Color color) {

        int ir = (int) color.r, ig = (int) color.g, ib = (int) color.b;
        double rx = color.r - ir, gx = g - ig, bx = b - ib;

        for (byte[] data : model.buffers) {
            for (int i = 0; i < data.length; ++i) {
                data[i * 3] = (byte) (ir + (Math.random() < rx ? 1 : 0));
                data[i * 3 + 1] = (byte) (ig + (Math.random() < gx ? 1 : 0));
                data[i * 3 + 2] = (byte) (ib + (Math.random() < bx ? 1 : 0));
            }
        }
    }
}

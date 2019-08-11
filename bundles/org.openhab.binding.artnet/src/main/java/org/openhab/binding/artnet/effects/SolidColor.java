package org.openhab.binding.artnet.effects;

import org.openhab.binding.artnet.infrastructure.Color;
import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;

public class SolidColor extends Layer {

    private boolean generated = false;
    private Color color;

    public enum Mode {
        SPACE_DITHER
    }

    public SolidColor(Model model, Color color) {
        super(model);
        this.color = color;
    }

    @Override
    public Layer update(long timeCode) {
        if (!generated) {
            generate();
        }
        return this;
    }

    private void generate() {

        int ir = (int) color.r, ig = (int) color.g, ib = (int) color.b;
        double rx = color.r - ir, gx = color.g - ig, bx = color.b - ib;

        for (int i = 0; i < data.length; ++i) {
            data[i * 3] = (byte) (ir + (Math.random() < rx ? 1 : 0));
            data[i * 3 + 1] = (byte) (ig + (Math.random() < gx ? 1 : 0));
            data[i * 3 + 2] = (byte) (ib + (Math.random() < bx ? 1 : 0));
        }
    }
}

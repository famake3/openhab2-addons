package org.openhab.binding.artnet.effects;

import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;

/**
 * This class implements a fade effect which changes from one layer to another over a fixed time interval.
 *
 * The time interval starts when start() is called, or on the first call to update().
 *
 *
 * @author fa2k
 *
 */

public class Fader extends Layer {

    public enum Mode {
        LINEAR_STABLE_SPACE_DITHER
    }

    float[] pixelFadeVal;
    long startTime = 0;
    final Mode mode;

    public Fader(Model model, Layer from, Layer to, long timeMs, Mode mode) {
        super(model);
        this.mode = mode;
    }

    public Fader(Model mode, Layer from, Layer to, long timeMs) {
        this(mode, from, to, timeMs, Mode.LINEAR_STABLE_SPACE_DITHER);
    }

    public void setStart(long timeCode) {
        this.startTime = timeCode;
    }

    @Override
    public Layer update(long timeCode) {
        // TODO Auto-generated method stub
        return null;
    }

}

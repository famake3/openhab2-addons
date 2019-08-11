package org.openhab.binding.artnet.effects;

import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;

public class StableFader extends Layer {

    private long startTime, duration;
    private double[] roundAffinity;
    private Layer from, to;

    public StableFader(Model model, Layer from, Layer to, long startTime, long duration) {
        super(model);
        this.startTime = startTime;
        this.duration = duration;
        this.from = from;
        this.to = to;
        if (from.data.length != model.getSize() || to.data.length != model.getSize()) {
            throw new IllegalArgumentException(
                    "From and To layers have different sizes, " + from.data.length + " and " + to.data.length + ".");
        }
        for (int i = 0; i < model.getSize(); ++i) {
            roundAffinity[i] = Math.random();
        }
    }

    @Override
    public Layer update(long timeCode) {
        double fraction = (timeCode - startTime) / (double) duration;
        if (fraction >= 1.0) {
            return to.update(timeCode);
        }
        from = from.update(timeCode);
        to = to.update(timeCode);
        for (int i = 0; i < model.getSize(); ++i) {
            data[i] = (byte) (from.data[i] * (1 - fraction) + to.data[i] * fraction + roundAffinity[i]);
        }
        return this;
    }

}

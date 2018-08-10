package org.openhab.binding.artnet.infrastructure;

public abstract class Layer {

    public final Model model;
    public final byte[] data;

    public Layer() {
        model = null;
        data = null;
    }

    public Layer(Model model) {
        this.model = model;
        this.data = new byte[model.getSize()];
    }

    /**
     * Layer update function. Returns a layer to use in the current
     * refresh cycle. Normally returns self.
     *
     * @param timeCode Monotonically increasing number representing milliseconds
     * @return Layer to use for ext frame, or null if the animation is finished.
     */
    public abstract Layer update(long timeCode);
}

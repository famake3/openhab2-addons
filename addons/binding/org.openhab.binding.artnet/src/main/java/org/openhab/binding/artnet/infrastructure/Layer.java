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

    public abstract boolean update(long timeCode);
}

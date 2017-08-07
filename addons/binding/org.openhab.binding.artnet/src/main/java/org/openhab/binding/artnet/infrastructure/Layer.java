package org.openhab.binding.artnet.infrastructure;

public class Layer {

    public final Model model;
    public byte[] data;

    public Layer(Model model) {
        this.model = model;
        this.data = new byte[model.getSize()];
    }

}

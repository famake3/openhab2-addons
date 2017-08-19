package org.openhab.binding.artnet.effects;

import org.openhab.binding.artnet.infrastructure.Layer;
import org.openhab.binding.artnet.infrastructure.Model;

public class StableFader extends Layer {

    public StableFader(Model model, Layer from, Layer to) {
        super(model);
    }

    @Override
    public Layer update(long timeCode) {
        // TODO Auto-generated method stub
        return null;
    }

}

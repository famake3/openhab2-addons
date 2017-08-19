package org.openhab.binding.artnet.infrastructure;

import java.util.ArrayList;

public abstract class Layer {

    public final Model model;
    public final ArrayList<ArrayList<byte[]>> buffers;
    public ArrayList<ArrayList<byte[]>> alpha;

    public Layer(Model model) {
        this.model = model;
        ArrayList<ArrayList<byte[]>> bufferListList = new ArrayList<>();
        for (Device dev : model.devices) {
            ArrayList<byte[]> bufferList = new ArrayList<byte[]>(dev.buffers.size());
            for (OutputBuffer buf : dev.buffers) {
                bufferList.add(new byte[buf.data.length]);
            }
        }
        buffers = bufferListList;
    }

    /**
     * Layer update function. Returns a layer to use in the next frame. Normally returns self.
     * It can also return null. This indicates that no further update is needed.
     *
     * @param timeCode Monotonically increasing number representing milliseconds
     * @return Layer to use for ext frame, or null if the animation is finished.
     */
    public abstract Layer update(long timeCode);

    /**
     * In case the layer is fully transparent, it is considered "Null" and
     * can be discarded in certain cases.
     *
     * @return Whether the layer is fully transparent and no longer will be
     *         updated.
     */
    public abstract boolean isNull();
}

package org.openhab.binding.artnet.infrastructure;

public class OutputLoop extends Thread {

    private final int frameTime;
    private final ArtNetSender sender;
    private long lastSendTime;
    private volatile boolean exit = false;

    public Layer topLayer;

    public OutputLoop(int frameTime, ArtNetSender sender) {
        this.frameTime = frameTime;
        this.sender = sender;
        this.lastSendTime = 0;
    }

    @Override
    public void run() {
        while (!exit) {
            long now = System.currentTimeMillis();
            lastSendTime = now;
            // topLayer = topLayer.update(now);

            try {
                Thread.sleep(frameTime + lastSendTime - now);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}

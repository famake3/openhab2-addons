package org.openhab.binding.artnet.infrastructure;

public class OutputLoop extends Thread {

    private final int frameTime;
    private final ArtNetSender sender;

    public OutputLoop(int frameTime, ArtNetSender sender) {
        this.frameTime = frameTime;
        this.sender = sender;
    }

    @Override
    public void run() {

    }

}

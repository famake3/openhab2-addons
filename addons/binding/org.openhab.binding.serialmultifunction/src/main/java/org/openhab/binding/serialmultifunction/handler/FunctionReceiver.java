package org.openhab.binding.serialmultifunction.handler;

public interface FunctionReceiver {

    void receivedUpdate(byte[] data);

    int getMaxMessageSize();

}

package org.openhab.binding.benqprojector.handler;

public class CommunicationException extends Exception {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Exception cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 1L;

}

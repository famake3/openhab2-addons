package org.openhab.binding.receivernadt748.handler;

public class CommunicationException extends Exception {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Exception e) {
        super(message, e);
    }

    private static final long serialVersionUID = 1L;

}

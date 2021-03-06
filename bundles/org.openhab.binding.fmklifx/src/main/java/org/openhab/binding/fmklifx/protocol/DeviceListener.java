package org.openhab.binding.fmklifx.protocol;

public interface DeviceListener {

    /**
     * Interface for receiving messages from LIFX light bulbs.
     */

    void ping(); // ACK / ping reply / state service / etc -- indication of ONLINE

    void color(LifxColor color);

    void power(boolean on);

    void label(String label);

    void timeout();

    void version(int vendor, int product, int version);

}

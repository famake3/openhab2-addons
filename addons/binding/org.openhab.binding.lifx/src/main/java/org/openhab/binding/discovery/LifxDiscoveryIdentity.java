package org.openhab.binding.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.lifx.protocol.DeviceListener;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxColor;

public class LifxDiscoveryIdentity implements DeviceListener {

    private final LifxLightIdentificationListener listener;
    private final LanProtocolService protocol;
    private final long id;
    private String label;
    private ThingTypeUID type;

    public LifxDiscoveryIdentity(LanProtocolService protocol, LifxLightIdentificationListener listener, long id) {
        this.listener = listener;
        this.protocol = protocol;
        this.id = id;
        // protocol.queryLabel(lifxDeviceStatus);
    }

    @Override
    public void ping() {
    }

    @Override
    public void color(LifxColor color) {
    }

    @Override
    public void power(boolean on) {
    }

    @Override
    public void label(String label) {
        this.label = label;
        if (this.type != null) {
            listener.lightIdentified(this);
        }
    }

    @Override
    public void timeout() {
    }

}

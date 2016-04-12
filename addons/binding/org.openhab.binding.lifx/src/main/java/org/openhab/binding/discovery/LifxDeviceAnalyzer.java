package org.openhab.binding.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.lifx.LifxBindingConstants;
import org.openhab.binding.lifx.protocol.DeviceListener;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxColor;
import org.openhab.binding.lifx.protocol.LifxProtocolDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDeviceAnalyzer implements DeviceListener {

    private final LifxLightIdentificationListener listener;
    private final LanProtocolService protocol;
    private final long id;
    private String label;
    private ThingTypeUID type;
    private String typeDescription;
    private boolean cleanupTag;

    private Logger logger = LoggerFactory.getLogger(LifxDeviceAnalyzer.class);

    public LifxDeviceAnalyzer(LanProtocolService protocol, LifxLightIdentificationListener listener, long id) {
        this.listener = listener;
        this.protocol = protocol;
        this.id = id;
        LifxProtocolDevice lifxDevice = protocol.registerDeviceListener(id, this);
        protocol.queryLabel(lifxDevice);
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
    public void version(int vendor, int product, int version) {
        boolean knownId = vendor == 1;
        if (knownId) {
            typeDescription = LanProtocolService.LIFX_TYPE_DESCRIPTIONS.get(product);
            if (typeDescription == null) {
                typeDescription = "Unknown";
            }
            boolean color = LanProtocolService.LIFX_COLOR_TYPE_IDS.contains(product);
            boolean white = LanProtocolService.LIFX_WHITE_TYPE_IDS.contains(product);
            if (color && !white) {
                type = LifxBindingConstants.THING_TYPE_LIGHT_COLOR;
            } else if (!color && white) {
                type = LifxBindingConstants.THING_TYPE_LIGHT_WHITE;
            } else {
                logger.warn("Don't know if device is white or color light or something else, assuming color");
                type = LifxBindingConstants.THING_TYPE_LIGHT_COLOR;
            }
        } else {
            protocol.unregisterDeviceListener(id);
            listener.lightIdFailed(this);
        }
        if (label != null) {
            listener.lightIdentified(this);
        }
    }

    @Override
    public void timeout() {
        protocol.unregisterDeviceListener(id);
        listener.lightIdFailed(this);
    }

    public String getLabel() {
        return label;
    }

    public ThingTypeUID getType() {
        return type;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public long getId() {
        return id;
    }

    public boolean cleanupTag() {
        boolean prev = cleanupTag;
        cleanupTag = true;
        return prev;
    }

    public void clearCleanupTag() {
        cleanupTag = false;
    }

}

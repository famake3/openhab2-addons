package org.openhab.binding.fmklifx.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.fmklifx.LifxBindingConstants;
import org.openhab.binding.fmklifx.protocol.DeviceListener;
import org.openhab.binding.fmklifx.protocol.LanProtocolService;
import org.openhab.binding.fmklifx.protocol.LifxColor;
import org.openhab.binding.fmklifx.protocol.LifxProtocolDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDeviceAnalyzer implements DeviceListener {

    private final LifxLightIdentificationListener listener;
    private final LanProtocolService protocol;
    private final long id;
    private String label;
    private ThingTypeUID type;
    private String typeDescription;

    private Logger logger = LoggerFactory.getLogger(LifxDeviceAnalyzer.class);

    public LifxDeviceAnalyzer(LanProtocolService protocol, LifxLightIdentificationListener listener, long id) {
        this.listener = listener;
        this.protocol = protocol;
        this.id = id;
        LifxProtocolDevice lifxDevice = protocol.registerDeviceListener(id, this);
        logger.debug("Querying new device for label and version...");
        protocol.queryLabel(lifxDevice);
        protocol.queryVersion(lifxDevice);
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
        logger.debug("Received a label '" + label + "' for light ID " + LifxDiscoveryService.getIdString(id));
        if (this.type != null) {
            protocol.unregisterDeviceListener(id, this);
            listener.lightIdentified(this);
        }
    }

    @Override
    public void version(int vendor, int product, int version) {
        boolean knownId = vendor == 1;
        logger.debug("Received a version reply (" + vendor + ", " + product + ", " + version + ") for light ID "
                + LifxDiscoveryService.getIdString(id));
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
            protocol.unregisterDeviceListener(id, this);
            listener.lightIdFailed(this);
        }
        if (label != null) {
            protocol.unregisterDeviceListener(id, this);
            listener.lightIdentified(this);
        }
    }

    @Override
    public void timeout() {
        protocol.unregisterDeviceListener(id, this);
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

}

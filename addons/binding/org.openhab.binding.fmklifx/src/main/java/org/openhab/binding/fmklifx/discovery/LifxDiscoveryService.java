package org.openhab.binding.fmklifx.discovery;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.fmklifx.LifxBindingConstants;
import org.openhab.binding.fmklifx.protocol.LanProtocolService;
import org.openhab.binding.fmklifx.protocol.LifxDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService
        implements LifxDiscoveryListener, LifxLightIdentificationListener {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);

    private LanProtocolService protocol;
    private Map<Long, LifxDeviceAnalyzer> discoveredDevices;

    private boolean scanMode;

    public LifxDiscoveryService() {
        super(10);
        discoveredDevices = new HashMap<>();
        try {
            protocol = LanProtocolService.getInstance();
            protocol.registerDiscoveryListener(this);
        } catch (SocketException e) {
            logger.error("Failed to get / create an instance of the protocol service", e);
            throw new RuntimeException("Discovery service can't get protocol service instance", e);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        try {
            protocol.sendDiscoveryPacket();
        } catch (IOException e) {
            logger.warn("Failed to start LIFX discovery", e);
        }
    }

    @Override
    protected void deactivate() {
        protocol.removeDiscoveryListener(this);
    }

    @Override
    protected void startScan() {
        scanMode = true;
        try {
            protocol.sendDiscoveryPacket();
        } catch (IOException e) {
            logger.error("Failed to send probe for new devices", e);
        }
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        scanMode = false;
    }

    @Override
    public void deviceDiscovered(long id) {
        // Called by protocol service
        if (scanMode || isBackgroundDiscoveryEnabled()) {
            LifxDeviceAnalyzer existingDevice = discoveredDevices.get(id);
            if (existingDevice == null) {
                logger.debug("Received a response from a new device: " + getIdString(id));
                discoveredDevices.put(id, new LifxDeviceAnalyzer(protocol, this, id));
            } else {
                logger.debug("Received a response from a device which is already discovered: " + getIdString(id));
            }
        } else {
            logger.debug("Got discovery result, but discovery not enabled");
        }
    }

    public static String getIdString(long id) {
        return String.format("%06x", id);
    }

    @Override
    public void lightIdentified(LifxDeviceAnalyzer ident) {
        if (scanMode || isBackgroundDiscoveryEnabled()) {
            logger.debug("A device with ID " + getIdString(ident.getId()) + " was completely analyzed");
            ThingUID thingUid = new ThingUID(ident.getType(), getIdString(ident.getId()));
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LifxBindingConstants.PARAM_DEVICE_ID, getIdString(ident.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withProperties(properties)
                    .withLabel(ident.getLabel()).build();
            thingDiscovered(discoveryResult);

        } else {
            logger.debug("A device with ID " + getIdString(ident.getId())
                    + " analyzed, but ignored since we are no longer in discovery mode");
        }
    }

    @Override
    public void lightIdFailed(LifxDeviceAnalyzer ident) {
        logger.debug("Identification of light " + getIdString(ident.getId()) + " timed out.");
        discoveredDevices.remove(ident);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return LifxBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

}

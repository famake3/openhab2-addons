package org.openhab.binding.discovery;

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
import org.openhab.binding.lifx.LifxBindingConstants;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService
        implements LifxDiscoveryListener, LifxLightIdentificationListener {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);

    private LanProtocolService protocol;
    private Map<Long, LifxDeviceAnalyzer> discoveredDevices;

    private boolean scanMode;

    public LifxDiscoveryService(int timeout) throws IllegalArgumentException {
        super(timeout);
        try {
            protocol = LanProtocolService.getInstance();
            protocol.registerDiscoveryListener(this);
        } catch (SocketException e) {
            logger.error("Failed to get / create an instance of the protocol service", e);
            throw new RuntimeException("Discovery service can't get protocol service instance", e);
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
            protocol.startDiscovery();
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
                discoveredDevices.put(id, new LifxDeviceAnalyzer(protocol, this, id));
            } else {
                existingDevice.clearCleanupTag();
            }
        }
    }

    private static String getIdString(long id) {
        return String.format("%06x", id);
    }

    @Override
    public void lightIdentified(LifxDeviceAnalyzer ident) {
        if (scanMode || isBackgroundDiscoveryEnabled()) {
            ThingUID thingUid = new ThingUID(ident.getType(), getIdString(ident.getId()));
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LifxBindingConstants.PARAM_DEVICE_ID, getIdString(ident.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withProperties(properties)
                    .withLabel(ident.getLabel()).build();
            thingDiscovered(discoveryResult);

        }
    }

    @Override
    public void lightIdFailed(LifxDeviceAnalyzer ident) {
        discoveredDevices.remove(ident);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return LifxBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

}

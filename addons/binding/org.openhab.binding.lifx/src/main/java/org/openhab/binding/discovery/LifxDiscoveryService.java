package org.openhab.binding.discovery;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.lifx.LifxBindingConstants;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService
        implements LifxDiscoveryListener, LifxLightIdentificationListener {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);

    private LanProtocolService protocol;
    private Map<Long, LifxDeviceIdentifier> discoveredDevices;

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
            if (discoveredDevices.get(id) == null) {
                discoveredDevices.put(id, new LifxDeviceIdentifier(protocol, this, id));
            }
        }
    }

    @Override
    public void lightIdentified(LifxDeviceIdentifier ident) {

    }

    @Override
    public void lightIdFailed(LifxDeviceIdentifier ident) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return LifxBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

}

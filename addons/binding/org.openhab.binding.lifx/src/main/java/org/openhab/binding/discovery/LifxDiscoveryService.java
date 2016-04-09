package org.openhab.binding.discovery;

import java.net.SocketException;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService implements LifxDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);
    private static final int PROBE_INTERVAL = 60;

    private volatile boolean scanActive = false, backgroundDiscoveryActive = false;
    private LanProtocolService protocol;

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
        scanActive = true;
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        scanActive = false;
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        backgroundDiscoveryActive = true;
    }

    @Override
    protected void stopBackgroundDiscovery() {
        backgroundDiscoveryActive = false;
    }

    @Override
    public void deviceDiscovered(long id) {
    }

}

package org.openhab.binding.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);
    private static final int PROBE_INTERVAL = 60;

    private boolean scanActive = false, backgroundDiscoveryActive = false;

    public LifxDiscoveryService(int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    @Override
    protected void startScan() {

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

}

package org.openhab.binding.discovery;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxDiscoveryService extends AbstractDiscoveryService implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(LifxDiscoveryService.class);
    private final int REFRESH_INTERVAL = 60;

    ScheduledFuture<?> discoveryJob;

    public LifxDiscoveryService(int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    @Override
    protected void startScan() {
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start LIFX device background discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleAtFixedRate(this, 0, REFRESH_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop LIFX device background discovery");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void run() {
        // Discovery task: send a broadcast

    }

    public void sendBroadcast() {

    }

}

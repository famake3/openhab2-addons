package org.openhab.binding.discovery;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class LifxDiscoveryService extends AbstractDiscoveryService {

    public LifxDiscoveryService(int timeout) throws IllegalArgumentException {
        super(timeout);
        // TODO Auto-generated constructor stub
    }

    public LifxDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout) throws IllegalArgumentException {
        super(supportedThingTypes, timeout);
        // TODO Auto-generated constructor stub
    }

    public LifxDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout,
            boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub

    }

}

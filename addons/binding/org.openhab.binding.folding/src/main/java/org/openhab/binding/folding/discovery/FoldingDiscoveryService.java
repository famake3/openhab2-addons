package org.openhab.binding.folding.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;

public class FoldingDiscoveryService extends AbstractDiscoveryService {

    public FoldingDiscoveryService() {
        super(10);
    }

    @Override
    protected void startScan() {
    }

    public void addDevices() {
        /*
         * ThingUID thingUid = new ThingUID(ident.getType(), getIdString(ident.getId()));
         * 
         * Map<String, Object> properties = new HashMap<>(1);
         * properties.put(LifxBindingConstants.PARAM_DEVICE_ID, getIdString(ident.getId()));
         * DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid).withProperties(properties)
         * .withLabel(ident.getLabel()).build();
         * thingDiscovered(discoveryResult);
         */

    }

}

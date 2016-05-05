package org.openhab.binding.lgtv.discovery;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;

public class LgTvDiscoveryParticipant implements UpnpDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        // TODO Auto-generated method stub
        return null;
    }

}

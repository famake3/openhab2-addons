/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.folding.FoldingBindingConstants;

public class FoldingSlotDiscoveryService extends AbstractDiscoveryService {

    public FoldingSlotDiscoveryService() {
        super(Collections.singleton(FoldingBindingConstants.THING_TYPE_SLOT), 10, true);
        FoldingDiscoveryProxy.getInstance().setService(this);
    }

    @Override
    protected void startScan() {
    }

    protected String getLabel(String host, String description) {
        if (description == null) {
            description = "slot";
        }
        int endOfLabel = description.indexOf(' ');
        if (endOfLabel > 0) {
            description = description.substring(0, endOfLabel);
        }
        endOfLabel = description.indexOf(':');
        if (endOfLabel > 0) {
            description = description.substring(0, endOfLabel);
        }
        return description + " @ " + host;
    }

    public void newSlot(ThingUID bridgeUID, String host, String id, String description) {
        if (isBackgroundDiscoveryEnabled() && id != null) {
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(FoldingBindingConstants.PARAM_SLOT_ID, id);
            ThingUID thingUID = new ThingUID(FoldingBindingConstants.THING_TYPE_SLOT, bridgeUID, id);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(getLabel(host, description)).build();
            thingDiscovered(discoveryResult);
        }
    }
}

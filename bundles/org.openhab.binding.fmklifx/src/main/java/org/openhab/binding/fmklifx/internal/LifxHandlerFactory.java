/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fmklifx.internal;

import static org.openhab.binding.fmklifx.LifxBindingConstants.THING_TYPE_LIGHT_COLOR;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.fmklifx.LifxBindingConstants;
import org.openhab.binding.fmklifx.handler.LifxColorLightHandler;

/**
 * The {@link LifxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Famake - Initial contribution
 */
public class LifxHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return LifxBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_LIGHT_COLOR)) {
            // String deviceId = thing.getProperties().get(LifxBindingConstants.PARAM_DEVICE_ID);
            return new LifxColorLightHandler(thing);
        }

        return null;
    }
}

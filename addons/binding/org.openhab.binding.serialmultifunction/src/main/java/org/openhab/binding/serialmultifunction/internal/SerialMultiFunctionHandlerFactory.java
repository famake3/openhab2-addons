/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.serialmultifunction.internal;

import static org.openhab.binding.serialmultifunction.SerialMultiFunctionBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.serialmultifunction.handler.OnOffCodeHandler;
import org.openhab.binding.serialmultifunction.handler.SerialMultiFunctionHandler;
import org.openhab.binding.serialmultifunction.handler.SwitchHandler;
import org.openhab.binding.serialmultifunction.handler.TemperatureHandler;

/**
 * The {@link SerialMultiFunctionHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author FaMaKe - Initial contribution
 */
public class SerialMultiFunctionHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SWITCH, THING_TYPE_TEMPERATURE, THING_TYPE_ON_OFF_CODE));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new SerialMultiFunctionHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE)) {
            return new TemperatureHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new SwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ON_OFF_CODE)) {
            return new OnOffCodeHandler(thing);
        }

        return null;
    }
}

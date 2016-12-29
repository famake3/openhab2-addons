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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.serialmultifunction.handler.CodeReceiver;
import org.openhab.binding.serialmultifunction.handler.DecimalTemperatureHandler;
import org.openhab.binding.serialmultifunction.handler.InputSwitchHandler;
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
            Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SWITCH, THING_TYPE_TEMPERATURE, THING_TYPE_TEMPERATURE_DECIMAL,
                    THING_TYPE_ON_OFF_CODE, THING_TYPE_CODE_RECEIVER, THING_TYPE_INPUT_SWITCH));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new SerialMultiFunctionHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE)) {
            return new TemperatureHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE_DECIMAL)) {
            return new DecimalTemperatureHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new SwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_INPUT_SWITCH)) {
            return new InputSwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ON_OFF_CODE)) {
            return new OnOffCodeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CODE_RECEIVER)) {
            return new CodeReceiver(thing);
        }

        return null;
    }
}

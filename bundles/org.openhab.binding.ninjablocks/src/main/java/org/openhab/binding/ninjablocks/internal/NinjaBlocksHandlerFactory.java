/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ninjablocks.internal;

import static org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ninjablocks.handler.NinjaBlockHandler;
import org.openhab.binding.ninjablocks.handler.NinjaDoubleContactHandler;
import org.openhab.binding.ninjablocks.handler.NinjaSocketHandler;
import org.openhab.binding.ninjablocks.handler.NinjaTempHumidHandler;
import org.openhab.binding.ninjablocks.handler.NinjaTriggerHandler;
import org.osgi.service.http.HttpService;

/**
 * The {@link NinjaBlocksHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Famake - Initial contribution
 */
public class NinjaBlocksHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.addAll(NinjaTriggerHandler.SUPPORTED_THING_TYPES_UIDS);
        SUPPORTED_THING_TYPES_UIDS.addAll(NinjaDoubleContactHandler.SUPPORTED_THING_TYPES_UIDS);
        SUPPORTED_THING_TYPES_UIDS.addAll(NinjaTempHumidHandler.SUPPORTED_THING_TYPES_UIDS);
        SUPPORTED_THING_TYPES_UIDS.addAll(NinjaSocketHandler.SUPPORTED_THING_TYPES_UIDS);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_NINJA_RF);
    }
    private HttpService httpService;
    private GlobalBridge globalBridge = new GlobalBridge();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_NINJA_RF)) {
            globalBridge.bridge = new NinjaBlockHandler((Bridge) thing, httpService);
            return globalBridge.bridge;
        } else if (thingTypeUID.equals(THING_TYPE_MOTION_SENSOR) || thingTypeUID.equals(THING_TYPE_BUTTON)) {
            return new NinjaTriggerHandler(thing, globalBridge);
        } else if (thingTypeUID.equals(THING_TYPE_SOCKET)) {
            return new NinjaSocketHandler(thing, globalBridge);
        } else if (thingTypeUID.equals(THING_TYPE_TEMPERATURE_HUMIDITY)) {
            return new NinjaTempHumidHandler(thing, globalBridge);
        } else if (thingTypeUID.equals(THING_TYPE_DOUBLE_CONTACT)) {
            return new NinjaDoubleContactHandler(thing, globalBridge);
        }
        return null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}

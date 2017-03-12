/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lgtvserial.LgTvSerialBindingConstants;
import org.openhab.binding.lgtvserial.handler.LgTvSerialHandler;
import org.openhab.binding.lgtvserial.internal.protocol.serial.SerialCommunicatorFactory;

/**
 * The {@link LgTvSerialHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Added communicator to support daisy chained TV
 */
public class LgTvSerialHandlerFactory extends BaseThingHandlerFactory {

    private final static SerialCommunicatorFactory FACTORY = new SerialCommunicatorFactory();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.getBindingId().equals(LgTvSerialBindingConstants.BINDING_ID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.getBindingId().equals(LgTvSerialBindingConstants.BINDING_ID)) {
            return new LgTvSerialHandler(thing, FACTORY);
        }

        return null;
    }
}

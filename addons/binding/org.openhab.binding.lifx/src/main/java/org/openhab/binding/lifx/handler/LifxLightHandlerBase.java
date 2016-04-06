/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lifx.handler;

import static org.openhab.binding.lifx.LifxBindingConstants.CHANNEL_COLOR;

import java.net.SocketException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lifx.protocol.DeviceListener;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxColor;
import org.openhab.binding.lifx.protocol.LifxDeviceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LIFX light bulb.
 *
 * @author Famake - Initial contribution
 */
public abstract class LifxLightHandlerBase extends BaseThingHandler implements DeviceListener {

    private Logger logger = LoggerFactory.getLogger(LifxLightHandlerBase.class);

    private LanProtocolService lanProtocolService;
    private LifxDeviceStatus deviceStatus;

    public LifxLightHandlerBase(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COLOR)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {

        try {
            lanProtocolService = LanProtocolService.getInstance();
            updateStatus(ThingStatus.INITIALIZING);
            deviceStatus = lanProtocolService.registerDeviceListener(getDeviceId(), this);
            lanProtocolService.queryLightState(deviceStatus);
        } catch (SocketException e) {
            throw new RuntimeException("Unable to open a socket, there's nothing to do but give up", e);
        }
    }

    private long getDeviceId() {
        getThing().getConfiguration().get("device-id");
        return 0;
    }

    @Override
    public void ping() {
        // TODO Auto-generated method stub

    }

    @Override
    public abstract void color(LifxColor color);

    @Override
    public abstract void power(boolean on);

    @Override
    public void label(String label) {
    }

    @Override
    public void timeout() {
    }
}

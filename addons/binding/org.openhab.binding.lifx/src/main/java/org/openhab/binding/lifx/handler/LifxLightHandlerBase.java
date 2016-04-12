/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lifx.handler;

import java.math.BigDecimal;
import java.net.SocketException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lifx.LifxBindingConstants;
import org.openhab.binding.lifx.protocol.DeviceListener;
import org.openhab.binding.lifx.protocol.LanProtocolService;
import org.openhab.binding.lifx.protocol.LifxColor;
import org.openhab.binding.lifx.protocol.LifxProtocolDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a LIFX light bulb.
 *
 * @author Famake - Initial contribution
 */
public abstract class LifxLightHandlerBase extends BaseThingHandler implements DeviceListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(LifxLightHandlerBase.class);

    protected LanProtocolService protocol;
    protected LifxProtocolDevice device;
    protected ScheduledFuture<?> pollingTask;

    public LifxLightHandlerBase(Thing thing) {
        super(thing);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        try {
            protocol = LanProtocolService.getInstance();
            updateStatus(ThingStatus.INITIALIZING);
            device = protocol.registerDeviceListener(deviceId(), this);
            protocol.queryLightState(device);
            startStatePolling();
        } catch (SocketException e) {
            throw new RuntimeException("Unable to open a socket, there's nothing to do but give up", e);
        }
    }

    @Override
    public void dispose() {
        if (pollingTask != null) {
            pollingTask.cancel(false);
        }
    }

    private void startStatePolling() {
        int pollingInterval = ((BigDecimal) getConfig().get(LifxBindingConstants.PARAM_POLLING_INTERVAL)).intValue();
        pollingTask = scheduler.scheduleAtFixedRate(this, 10, pollingInterval, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        logger.debug("Calling polling function");
        poll();
    }

    public abstract void poll();

    protected String deviceIdString() {
        return (String) getThing().getConfiguration().get(LifxBindingConstants.PARAM_DEVICE_ID);
    }

    protected long deviceId() {
        return Long.parseLong(deviceIdString(), 16);
    }

    protected void online() {
        logger.debug("Received a valid packet from device " + deviceId());
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void ping() {
        online();
    }

    @Override
    public abstract void color(LifxColor color);

    @Override
    public abstract void power(boolean on);

    @Override
    public void label(String label) {
        // Label is not used
    }

    @Override
    public void version(int vendor, int product, int version) {
        // Not used
    }

    @Override
    public void timeout() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Response timeout");
    }
}

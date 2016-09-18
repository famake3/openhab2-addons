/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lgtvserial.LgTvSerialBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

/**
 * The {@link LgTvSerialHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Famake - Initial contribution
 */
public class LgTvSerialHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(LgTvSerialHandler.class);
    private final static int BAUD = 9600;
    private NRSerialPort serialPort2;
    private OutputStreamWriter output;

    public LgTvSerialHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        String portName = (String) getThing().getConfiguration().get("port");
        if (portName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            serialPort2 = new NRSerialPort(portName, BAUD);
            serialPort2.connect();
            output = new OutputStreamWriter(serialPort2.getOutputStream());
        }
    }

    @Override
    public void dispose() {
        if (serialPort2 != null) {
            serialPort2.disconnect();
            serialPort2 = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return; // Don't support refreshing
        }
        try {
            if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_POWER)) {
                if (command == OnOffType.ON) {
                    output.write("ka 0 1\r");
                    updateState(channelUID, OnOffType.ON);
                } else if (command == OnOffType.OFF) {
                    output.write("ka 0 0\r");
                }
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_INPUT)) {
                output.write(String.format("xb 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, OnOffType.ON);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_VOLUME)) {
                // TODO: Implement increase/decrease
                PercentType vol = (PercentType) command;
                output.write(String.format("kf 0 %x\r", vol.intValue()));
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_MUTE)) {
                if (command == OnOffType.ON) {
                    output.write("ke 0 0\r");
                    updateState(channelUID, OnOffType.ON);
                } else if (command == OnOffType.OFF) {
                    output.write("ke 0 1\r");
                }
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_BACKLIGHT)) {
                // TODO: Implement increase/decrease
                PercentType vol = (PercentType) command;
                output.write(String.format("mg 0 %x\r", vol.intValue()));
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_COLOR_TEMPERATURE)) {
                output.write(String.format("ku 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, OnOffType.ON);
            }
        } catch (IOException e) {
            logger.error("Serial port write error", e);
        }
    }
}

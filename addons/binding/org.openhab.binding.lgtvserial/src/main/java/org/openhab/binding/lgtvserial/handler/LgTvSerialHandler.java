/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgtvserial.LgTvSerialBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * The {@link LgTvSerialHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Famake - Initial contribution
 */
public class LgTvSerialHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(LgTvSerialHandler.class);
    private final static int BAUD = 9600;
    private SerialPort serialPort;

    public LgTvSerialHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        String portName = (String) getThing().getConfiguration().get("port");
        if (portName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            serialPort = new SerialPort(portName);
            try {
                serialPort.openPort();
                serialPort.setParams(BAUD, 8, 1, 0);
                updateStatus(ThingStatus.ONLINE);
            } catch (SerialPortException e) {
                logger.error("Serial port setup error!", e);
            }
        }
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_POWER)) {
                if (command == OnOffType.ON) {
                    serialPort.writeString("ka 0 1\r");
                    updateState(channelUID, OnOffType.ON);
                } else if (command == OnOffType.OFF) {
                    serialPort.writeString("ka 0 0\r");
                }
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_INPUT)) {
                serialPort.writeString(String.format("xb 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, OnOffType.ON);
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_VOLUME)) {
                // TODO: Implement increase/decrease
                PercentType vol = (PercentType) command;
                serialPort.writeString(String.format("kf 0 %x\r", vol.intValue()));
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_MUTE)) {
                if (command == OnOffType.ON) {
                    serialPort.writeString("ke 0 0\r");
                    updateState(channelUID, OnOffType.ON);
                } else if (command == OnOffType.OFF) {
                    serialPort.writeString("ke 0 1\r");
                }
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_BACKLIGHT)) {
                // TODO: Implement increase/decrease
                PercentType vol = (PercentType) command;
                serialPort.writeString(String.format("mg 0 %x\r", vol.intValue()));
            } else if (channelUID.getId().equals(LgTvSerialBindingConstants.CHANNEL_COLOR_TEMPERATURE)) {
                serialPort.writeString(String.format("ku 0 %x\r", Integer.parseInt(command.toString())));
                updateState(channelUID, OnOffType.ON);
            }
            // Prevent filling up input buffer: Will get data on power on.
            // Not foolproof if also using other means of powering on / off.
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
        } catch (SerialPortException e) {
            logger.error("Serial port write error: ", e);
        }
    }
}

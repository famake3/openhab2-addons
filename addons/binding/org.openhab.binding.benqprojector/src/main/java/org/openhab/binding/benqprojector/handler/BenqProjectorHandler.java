/**
 * Copyright (c) 2014 openHAB UG (haftungsabescthrhaenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.benqprojector.handler;

import static org.openhab.binding.benqprojector.BenqProjectorBindingConstants.POWER;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * The {@link BenqProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author famake - Initial contribution
 */
public class BenqProjectorHandler extends BaseThingHandler implements Runnable {

    private static final int TIMEOUT_MS = 50;
    private static final int N_TRIES = 100, N_TRIES_FAST = 2;
    private Logger logger = LoggerFactory.getLogger(BenqProjectorHandler.class);
    private SerialPort port;
    private boolean on = false;

    public BenqProjectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ChannelUID channelUID = getThing().getChannel(POWER).getUID();
        updateStatus(ThingStatus.ONLINE);
        try {
            updateState(channelUID, isOn() ? OnOffType.ON : OnOffType.OFF);
        } catch (SerialPortException | CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
        BigDecimal pollingInterval = (BigDecimal) getThing().getConfiguration().get("polling-interval");
        if (pollingInterval != null && pollingInterval.longValue() != 0) {
            scheduler.scheduleAtFixedRate(this, pollingInterval.longValue(), pollingInterval.longValue(),
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        try {
            port.closePort();
        } catch (SerialPortException e) {
        }
    }

    private boolean isOn() throws SerialPortException, CommunicationException {
        SerialPort port = getSerialPort();
        port.readBytes();
        port.writeBytes(("X000X").getBytes());
        String expected = "X000XRS232_OKX0_0X";
        int waiting = 0;
        while (port.getInputBufferBytesCount() < expected.length() && waiting < N_TRIES_FAST) {
            try {
                Thread.sleep(TIMEOUT_MS);
            } catch (InterruptedException e) {
            }
            ++waiting;
        }
        if (waiting == N_TRIES_FAST) {
            on = false;
        } else {
            readReply(expected, port);
            on = true;
        }
        return on;
    }

    public void on() throws SerialPortException, CommunicationException {
        command("X001X", "X0_1X");
    }

    public void off() throws SerialPortException, CommunicationException {
        command("X002X", "X002XX0_2X");
    }

    private void command(String cmd, String expectedReply) throws SerialPortException, CommunicationException {
        SerialPort port = getSerialPort();
        port.readBytes();

        // System.out.println("Sending command");
        if (port.writeBytes(cmd.getBytes())) {
            int waiting = 0;
            while (port.getInputBufferBytesCount() < expectedReply.length() && waiting < N_TRIES) {
                try {
                    Thread.sleep(TIMEOUT_MS);
                } catch (InterruptedException e) {
                }
                ++waiting;
            }
            if (waiting == N_TRIES) {
                throw new CommunicationException("Serial communication failed");
            }
            // System.out.println("Sent bytes to projector");
            readReply(expectedReply, port);
            // System.out.println("Read reply");
        } else {
            logger.error("Could not send to projector");
            throw new CommunicationException("Could not send to projector");
        }
    }

    private void readReply(String expectedReply, SerialPort port) throws SerialPortException, CommunicationException {
        for (char c : expectedReply.toCharArray()) {
            byte[] in = port.readBytes(1);
            if (in == null || in.length == 0) {
                throw new CommunicationException("Invalid response from projector");
            } else if (in[0] != c) {
                throw new CommunicationException("Invalid response from projector");
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private SerialPort getSerialPort() throws SerialPortException {
        if (port == null || !port.isOpened()) {
            String comPort = (String) getThing().getConfiguration().get("port");
            port = new SerialPort(comPort);
            port.openPort();
            port.setParams(112500, 8, 1, 0);
            updateStatus(ThingStatus.ONLINE);
        }
        return port;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            if (channelUID.getId().equals(POWER)) {
                handlePower(channelUID, command);
            }
        } catch (SerialPortException | CommunicationException e) {
            try {
                port.closePort();
            } catch (SerialPortException e1) {
            }
            port = null;
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Serial port error: " + e.getMessage());
            on = false;
        }
    }

    private void handlePower(ChannelUID channelUID, Command command)
            throws SerialPortException, CommunicationException {
        if (command != null) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    on();
                } else if (command.equals(OnOffType.OFF)) {
                    off();
                }
            } else if (command instanceof RefreshType) {
                updateState(channelUID, isOn() ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    @Override
    public void run() {
        // Polling
        ChannelUID channelUID = getThing().getChannel(POWER).getUID();
        try {
            boolean powerOn = isOn();
            updateStatus(ThingStatus.ONLINE);
            updateState(channelUID, powerOn ? OnOffType.ON : OnOffType.OFF);
        } catch (SerialPortException | CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}

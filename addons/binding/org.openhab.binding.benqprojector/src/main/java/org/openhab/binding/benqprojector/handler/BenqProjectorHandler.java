/**
 * Copyright (c) 2014 openHAB UG (haftungsabescthrhaenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.benqprojector.handler;

import static org.openhab.binding.benqprojector.BenqProjectorBindingConstants.POWER;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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

import gnu.io.NRSerialPort;

/**
 * The {@link BenqProjectorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author famake - Initial contribution
 */
public class BenqProjectorHandler extends BaseThingHandler implements Runnable {

    private Logger logger = LoggerFactory.getLogger(BenqProjectorHandler.class);
    private NRSerialPort serialPort;
    private boolean on = false;
    private OutputStreamWriter output;
    private InputStream input;

    public BenqProjectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ChannelUID channelUID = getThing().getChannel(POWER).getUID();
        updateStatus(ThingStatus.ONLINE);
        try {
            updateState(channelUID, isOn() ? OnOffType.ON : OnOffType.OFF);
        } catch (CommunicationException | IOException e) {
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
        serialPort.disconnect();
    }

    private boolean isOn() throws CommunicationException, IOException {
        connect();
        output.write("X000X");
        output.flush();
        String expected = "X000XRS232_OKX0_0X";
        int numRead = 0;
        int length = expected.length();
        char[] reply = new char[length];
        while (numRead < expected.length()) {
            int b = input.read();
            if (b == -1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
            } else {
                reply[numRead++] = (char) b;
            }
        }
        if (new String(reply).equals(expected)) {
            on = true;
            updateStatus(ThingStatus.ONLINE);
        } else {
            on = false;
            throw new CommunicationException("Unexpected reply from projector");
        }
        return on;
    }

    public void on() throws CommunicationException, IOException {
        command("X001X", "X0_1X");
    }

    public void off() throws CommunicationException, IOException {
        command("X002X", "X002XX0_2X");
    }

    private void command(String cmd, String expectedReply) throws CommunicationException, IOException {
        connect();

        // System.out.println("Sending command");
        output.write(cmd);
        output.flush();
        int numRead = 0;
        int length = expectedReply.length();
        char[] reply = new char[length];
        int numTries = 5;
        while (numRead < expectedReply.length()) {
            int b = 0;
            try {
                b = input.read();
            } catch (IOException e) {
                e.printStackTrace();
                if (numTries-- == 0) {
                    throw new CommunicationException("Input error", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    return;
                }
                continue;
            }
            if (b == -1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            } else {
                reply[numRead++] = (char) b;
            }
        }
        if (new String(reply).equals(expectedReply)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            throw new CommunicationException("Unexpected reply from projector");
        }
    }

    private synchronized void connect() {
        if (serialPort == null || !serialPort.isConnected()) {
            String comPort = (String) getThing().getConfiguration().get("port");
            serialPort = new NRSerialPort(comPort, 112500);
            serialPort.connect();
            output = new OutputStreamWriter(serialPort.getOutputStream());
            input = serialPort.getInputStream();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(POWER)) {
                handlePower(channelUID, command);
            }
        } catch (CommunicationException | IOException e) {
            serialPort.disconnect();
            serialPort = null;
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Serial port error: " + e.getMessage());
            on = false;
        }
    }

    private void handlePower(ChannelUID channelUID, Command command) throws CommunicationException, IOException {
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
        } catch (CommunicationException | IOException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}

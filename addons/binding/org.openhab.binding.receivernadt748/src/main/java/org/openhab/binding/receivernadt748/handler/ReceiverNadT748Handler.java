/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.receivernadt748.handler;

import static org.openhab.binding.receivernadt748.ReceiverNadT748BindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

/**
 * The {@link ReceiverNadT748Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mikael Aasen - Initial contribution
 */
public class ReceiverNadT748Handler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ReceiverNadT748Handler.class);

    private NRSerialPort serialPort;
    private boolean on = false;
    private OutputStreamWriter output;
    private InputStream input;

    public ReceiverNadT748Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            ChannelUID channelUID = getThing().getChannel(CHANNEL_POWER).getUID();
            boolean powerOn = isOn();
            updateState(channelUID, powerOn ? OnOffType.ON : OnOffType.OFF);
            if (powerOn) {
                refreshAll();
            } else {
                channelUID = getThing().getChannel(CHANNEL_VOLUME).getUID();
                updateState(channelUID, UnDefType.UNDEF);
                channelUID = getThing().getChannel(CHANNEL_MUTE).getUID();
                updateState(channelUID, UnDefType.UNDEF);
                channelUID = getThing().getChannel(CHANNEL_SOURCE).getUID();
                updateState(channelUID, UnDefType.UNDEF);
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

    }

    private void refreshAll() throws IOException {
        ChannelUID channelUID;
        channelUID = getThing().getChannel(CHANNEL_VOLUME).getUID();
        try {
            updateState(channelUID, PercentType.valueOf(Double.toString(toPercent(askInt("Main.Volume")))));
            boolean isMute = "On".equals(askString("Main.Mute", false));
            channelUID = getThing().getChannel(CHANNEL_MUTE).getUID();
            updateState(channelUID, isMute ? OnOffType.ON : OnOffType.OFF);
            channelUID = getThing().getChannel(CHANNEL_SOURCE).getUID();
            updateState(channelUID, new StringType(Integer.toString(askInt("Main.Source"))));
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        try {
            serialPort.disconnect();
        } catch (NullPointerException e) {
        }
    }

    private synchronized void connect() {
        if (serialPort == null || !serialPort.isConnected()) {
            String comPort = (String) getThing().getConfiguration().get("port");
            serialPort = new NRSerialPort(comPort, 112500);
            serialPort.connect();
            OutputStream outStream = serialPort.getOutputStream();
            output = new OutputStreamWriter(outStream);
            input = serialPort.getInputStream();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private double toPercent(double decibel) {
        return (decibel + 40) * 2.0;
    }

    private double toDecibel(double percent) {
        return (percent / 2.0) - 40;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(CHANNEL_POWER)) {
                handlePower(channelUID, command);
            } else if (channelUID.getId().equals(CHANNEL_VOLUME)) {
                handleVolume(channelUID, command);
            } else if (channelUID.getId().equals(CHANNEL_MUTE)) {
                handleMute(channelUID, command);
            } else if (channelUID.getId().equals(CHANNEL_SOURCE)) {
                handleSource(channelUID, command);
            }
        } catch (IOException | CommunicationException e) {
            serialPort.disconnect();
            serialPort = null;
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Serial port error: " + e.getMessage());
            on = false;
        }
    }

    private void handleMute(ChannelUID channelUID, Command command) throws CommunicationException, IOException {
        if (command != null) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    setValue("Main.Mute", "On");
                } else if (command.equals(OnOffType.OFF)) {
                    setValue("Main.Mute", "Off");
                }
            } else if (command instanceof RefreshType) {
                boolean isMute = "On".equals(askString("Main.Mute", false));
                updateState(channelUID, isMute ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    private void handleSource(ChannelUID channelUID, Command command) throws CommunicationException, IOException {
        if (command != null) {
            if (command instanceof RefreshType) {
                updateState(channelUID, new StringType(Integer.toString(askInt("Main.Source"))));
            } else if (command instanceof StringType) {
                setValue("Main.Source", command.toString());
            }
        }
    }

    private void handlePower(ChannelUID channelUID, Command command) throws IOException, CommunicationException {
        System.out.println("Setting power to: " + command.toString());
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

    private void handleVolume(ChannelUID channelUID, Command command) throws CommunicationException, IOException {
        if (command != null) {
            if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
                adjustValue("Main.Volume", "+");
            } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
                adjustValue("Main.Volume", "-");
            } else if (command instanceof DecimalType) {
                setValue("Main.Volume", Integer.toString((int) toDecibel(Double.parseDouble(command.toString()))));
            } else if (command instanceof RefreshType) {
                updateState(channelUID, PercentType.valueOf(Double.toString(toPercent(askInt("Main.Volume")))));
            }
        }
    }

    public void off() throws IOException, CommunicationException {
        connect();
        setValue("Main.Power", "Off");
    }

    public void on() throws IOException {
        connect();
        int attempts = 0;
        do {
            output.write("\rMain.Power=On\r");
            output.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        } while (!isOn() && attempts++ < 3);
        if (isOn()) {
            refreshAll();
        }
    }

    public boolean isOn() throws IOException {
        String power;
        try {
            power = askString("Main.Power", true);
        } catch (CommunicationException e) {
            power = "";
        }
        on = "On".equals(power);
        return on;
    }

    private String askString(String key, boolean ignorePowerState) throws CommunicationException, IOException {
        String answer = requestResponse(key + "?", ignorePowerState);
        String[] parts = answer.split("=");
        if (parts.length == 2) {
            updateStatus(ThingStatus.ONLINE);
            return parts[1];
        }
        throw new CommunicationException("Invalid response from receiver");
    }

    private int askInt(String key) throws CommunicationException, IOException {
        try {
            return Integer.parseInt(askString(key, false));
        } catch (NumberFormatException e) {
            throw new CommunicationException("Not a number received", e);
        }
    }

    private void setValue(String key, String value) throws CommunicationException, IOException {
        String answer = requestResponse(key + "=" + value, false);
        String[] parts = answer.split("=");
        if (parts.length != 2) {
            throw new CommunicationException("Invalid response from receiver: (length=" + Integer.toString(parts.length)
                    + ") key \"" + parts[0] + "\" answer " + answer + " expected \"" + key + "\"");
        }
    }

    private String adjustValue(String key, String operator) throws CommunicationException, IOException {
        String answer = requestResponse(key + operator, false);
        String[] parts = answer.split("=");
        if (parts.length == 2 && key.equals(parts[0])) {
            return parts[1];
        } else {
            throw new CommunicationException("Got the wrong answer: " + answer);
        }
    }

    private String requestResponse(String request, boolean ignorePowerState)
            throws IOException, CommunicationException {
        if (!on && !ignorePowerState && !isOn()) {
            throw new CommunicationException("Receiver is off");
        }
        connect();
        while (input.available() > 0) {
            char c = (char) input.read();
            System.err.println("FLUSHING '" + c + "'");
        }
        output.write("\r" + request + "\r");
        output.flush();
        int data = input.read();
        if (data == '\r') {
            boolean end = false;
            String answer = "";
            while (!end) {
                data = input.read();
                if (data == '\r') {
                    end = true;
                } else if (data == -1) {
                    throw new CommunicationException("Unexpected end of input");
                } else {
                    answer += (char) data;
                }
            }
            return answer;
        }
        throw new CommunicationException("Invalid response from receiver");
    }
}

/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.receivernadt748.handler;

import static org.openhab.binding.receivernadt748.ReceiverNadT748BindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * The {@link ReceiverNadT748Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mikael Aasen - Initial contribution
 */
public class ReceiverNadT748Handler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ReceiverNadT748Handler.class);

    private SerialPort port;

    private boolean on = false;

    public ReceiverNadT748Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
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
        }
    }

    private void refreshAll() {
        ChannelUID channelUID;
        channelUID = getThing().getChannel(CHANNEL_VOLUME).getUID();
        try {
            updateState(channelUID, PercentType.valueOf(Double.toString(toPercent(askInt("Main.Volume")))));
            boolean isMute = "On".equals(askString("Main.Mute", false));
            channelUID = getThing().getChannel(CHANNEL_MUTE).getUID();
            updateState(channelUID, isMute ? OnOffType.ON : OnOffType.OFF);
            channelUID = getThing().getChannel(CHANNEL_SOURCE).getUID();
            updateState(channelUID, new StringType(Integer.toString(askInt("Main.Source"))));
        } catch (CommunicationException | SerialPortException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        try {
            port.closePort();
        } catch (SerialPortException | NullPointerException e) {
        }
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
        } catch (SerialPortException | CommunicationException e) {
            port = null;
            updateStatus(ThingStatus.OFFLINE);
            logger.error("Serial port error: " + e.getMessage());
            on = false;
        }
    }

    private void handleMute(ChannelUID channelUID, Command command) throws CommunicationException, SerialPortException {
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

    private void handleSource(ChannelUID channelUID, Command command)
            throws SerialPortException, CommunicationException {
        if (command != null) {
            if (command instanceof RefreshType) {
                updateState(channelUID, new StringType(Integer.toString(askInt("Main.Source"))));
            } else if (command instanceof StringType) {
                setValue("Main.Source", command.toString());
            }
        }
    }

    private void handlePower(ChannelUID channelUID, Command command) throws SerialPortException {
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

    private void handleVolume(ChannelUID channelUID, Command command)
            throws SerialPortException, CommunicationException {
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

    public void off() throws SerialPortException {
        getSerialPort().readBytes();
        getSerialPort().writeBytes("\rMain.Power=Off\r".getBytes());
        getSerialPort().readBytes();
    }

    public void on() throws SerialPortException {
        int attempts = 0;
        do {
            getSerialPort().readBytes();
            getSerialPort().writeBytes("\rMain.Power=On\r".getBytes());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            getSerialPort().readBytes();
        } while (!isOn() && attempts++ < 3);
        if (isOn()) {
            refreshAll();
        }
    }

    public boolean isOn() {
        String power;
        try {
            power = askString("Main.Power", true);
        } catch (SerialPortException | CommunicationException e) {
            power = "";
        }
        on = "On".equals(power);
        return on;
    }

    private String askString(String key, boolean ignorePowerState) throws SerialPortException, CommunicationException {
        if (!on && !ignorePowerState && !isOn()) {
            throw new CommunicationException("Receiver is off");
        }
        SerialPort port = getSerialPort();
        port.readBytes();
        port.writeBytes(("\r" + key + "?\r").getBytes());
        int waiting = 0;
        while (port.getInputBufferBytesCount() == 0 && waiting < 10) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
            ++waiting;
        }
        if (port.getInputBufferBytesCount() == 0) {
            throw new CommunicationException("Communication timeout");
        }
        byte[] data = port.readBytes(1);

        if (data[0] == '\r') {
            boolean end = false;
            String answer = "";
            while (!end) {
                data = port.readBytes(1);
                if (data == null || data.length == 0 || data[0] == '\r')
                    end = true;
                else
                    answer += new String(data);
            }
            port.readBytes();
            String[] parts = answer.split("=");
            if (parts.length == 2) {
                updateStatus(ThingStatus.ONLINE);
                return parts[1];
            }
        }
        throw new CommunicationException("Invalid response from receiver");
    }

    private int askInt(String key) throws CommunicationException, SerialPortException {
        if (!on && !isOn()) {
            throw new CommunicationException("Receiver is off");
        }
        SerialPort port = getSerialPort();
        port.readBytes();
        port.writeBytes(("\r" + key + "?\r").getBytes());
        byte[] data = port.readBytes(1);
        if (data[0] == '\r') {
            boolean end = false;
            String answer = "";
            while (!end) {
                data = port.readBytes(1);
                if (data == null || data.length == 0 || data[0] == '\r')
                    end = true;
                else
                    answer += new String(data);
            }
            port.readBytes();

            String[] parts = answer.split("=");
            if (parts.length == 2 && key.equals(parts[0])) {
                return Integer.parseInt(parts[1]);
            } else {
                System.err.println("Ugyldig svar fra forsterker: " + answer);
            }
        } else {
            System.err.println("Ugyldig svar fra forsterker: " + data[0]);
        }
        throw new CommunicationException("Invalid response from receiver");
    }

    private void setValue(String key, String value) throws CommunicationException, SerialPortException {
        if (!on && !isOn()) {
            throw new CommunicationException("Receiver is off");
        }
        SerialPort port = getSerialPort();
        port.readBytes();
        port.writeBytes(("\r" + key + "=" + value + "\r").getBytes());

        byte[] data = port.readBytes(1);

        if (data[0] == '\r') {
            boolean end = false;
            String answer = "";
            while (!end) {
                data = port.readBytes(1);
                if (data == null || data.length == 0 || data[0] == '\r')
                    end = true;
                else
                    answer += new String(data);
            }
            port.readBytes();

            String[] parts = answer.split("=");
            if (parts.length != 2) {
                throw new CommunicationException(
                        "Invalid response from receiver: (length=" + Integer.toString(parts.length) + ") key \""
                                + parts[0] + "\" answer " + answer + " expected \"" + key + "\"");
            }
        } else {
            throw new CommunicationException("Invalid response from receiver" + data[0]);
        }
    }

    private String adjustValue(String key, String operator) throws CommunicationException, SerialPortException {
        if (!on && !isOn()) {
            throw new CommunicationException("Receiver is off");
        }
        SerialPort port = getSerialPort();
        port.readBytes();
        port.writeBytes(("\r" + key + operator + "\r").getBytes());

        byte[] data = port.readBytes(1);
        // System.out.println();
        if (data[0] == '\r') {
            boolean end = false;
            String answer = "";
            while (!end) {
                data = port.readBytes(1);
                if (data == null || data.length == 0 || data[0] == '\r')
                    end = true;
                else
                    answer += new String(data);
            }
            port.readBytes();

            String[] parts = answer.split("=");
            if (parts.length == 2 && key.equals(parts[0])) {
                return parts[1];
            } else {

                System.out.println("Got the wrong answer: " + answer);
            }
        } else {
            System.out.println("Got the wrong byte: " + Integer.toString(data[0]));
        }
        throw new CommunicationException("Invalid response from receiver");
    }
}

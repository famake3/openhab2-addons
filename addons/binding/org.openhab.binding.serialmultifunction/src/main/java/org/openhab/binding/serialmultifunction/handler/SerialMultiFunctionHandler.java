/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.serialmultifunction.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * The {@link SerialMultiFunctionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author FaMaKe - Initial contribution
 */
public class SerialMultiFunctionHandler extends BaseBridgeHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SerialMultiFunctionHandler.class);

    private SerialPort serialPort;
    private final static int BAUD = 9600;
    private boolean connected = false;
    private Map<Integer, FunctionReceiver> receivers;

    public SerialMultiFunctionHandler(Bridge thing) {
        super(thing);
        receivers = new HashMap<Integer, FunctionReceiver>();
    }

    @Override
    public void initialize() {
        String portName = (String) getThing().getConfiguration().get("port");
        if (portName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            serialPort = new SerialPort(portName);
            try {
                connect();
            } catch (SerialPortException e) {
                logger.error("Serial port setup error!", e);
            }
        }
    }

    @Override
    public void dispose() {
        if (connected && serialPort != null) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                logger.error("Failed to close the serial port", e);
            }
        }
        connected = false;
    }

    private void connect() throws SerialPortException {
        if (!connected) {
            connected = serialPort.openPort();
            serialPort.setParams(BAUD, 8, 1, 0);
            Thread receiver = new Thread(this);
            receiver.start();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void addFunctionReceiver(int function, FunctionReceiver fr) {
        receivers.put(function, fr);
    }

    public void send(int functionId, byte[] data) {
        try {
            connect();
            serialPort.writeBytes(new byte[] { (byte) '!', (byte) functionId, (byte) data.length });
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            connected = false;
            logger.error("Error while writing to serial port", e);
            try {
                serialPort.closePort();
            } catch (SerialPortException e2) {
                logger.error("Additionally, failed to close the serial port", e2);
            }
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000); // Seems we need to wait a little to be able to update state
            while (connected) {
                while (serialPort.readBytes(1)[0] != '!') {
                    ;
                }
                byte[] header = serialPort.readBytes(2);
                int function = header[0] & 0xFF;
                FunctionReceiver receiver = receivers.get(function);
                int length = header[1] & 0xFF;
                if (receiver != null) {
                    if (length > receiver.getMaxMessageSize()) {
                        continue; // Protect against corrupted data
                    }
                    byte[] data = serialPort.readBytes(length);
                    receiver.receivedUpdate(data);
                } else {
                    // For unknown codes, we'll read up to 8 data bytes (works for most cases, will re-sync)
                    byte[] data = serialPort.readBytes(Math.min(length, 8));
                    System.out.println("Unknown function " + function + " with data: " + bytesToHex(data));
                }
            }
        } catch (SerialPortException | InterruptedException e) {
            connected = false;
            logger.error("Error while reading from serial port (or waiting)", e);
            try {
                serialPort.closePort();
            } catch (SerialPortException e2) {
                logger.error("Additionally, there was an error while closing the serial port", e2);
            }
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

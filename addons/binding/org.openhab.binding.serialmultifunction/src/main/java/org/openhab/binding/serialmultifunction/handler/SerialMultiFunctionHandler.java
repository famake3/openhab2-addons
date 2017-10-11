/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.serialmultifunction.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import gnu.io.NRSerialPort;

/**
 * The {@link SerialMultiFunctionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author FaMaKe - Initial contribution
 */
public class SerialMultiFunctionHandler extends BaseBridgeHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SerialMultiFunctionHandler.class);

    private NRSerialPort serialPort;
    private final static int BAUD = 9600;
    private boolean connected = false;
    private Map<Integer, FunctionReceiver> receivers;
    private OutputStream output;
    private InputStream input;

    public SerialMultiFunctionHandler(Bridge thing) {
        super(thing);
        receivers = new HashMap<Integer, FunctionReceiver>();
    }

    @Override
    public void initialize() {
        connect();
    }

    private void connect() {
        if (!connected) {
            String portName = (String) getThing().getConfiguration().get("port");
            if (portName == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                serialPort = new NRSerialPort(portName, BAUD);
                connected = serialPort.connect();
                output = serialPort.getOutputStream();
                input = serialPort.getInputStream();
                Thread receiver = new Thread(this);
                receiver.start();
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void dispose() {
        if (connected && serialPort != null) {
            serialPort.disconnect();
        }
        connected = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void addFunctionReceiver(int function, FunctionReceiver fr) {
        receivers.put(function, fr);
    }

    public void send(int functionId, byte[] data) {
        connect();
        try {
            output.write(new byte[] { (byte) '!', (byte) functionId, (byte) data.length });
            output.write(data);
            output.flush();
        } catch (IOException e) {
            connected = false;
            logger.error("Error while writing to serial port", e);
            serialPort.disconnect();
            serialPort = null;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000); // Seems we need to wait a little to be able to update state
            while (connected) {
                while (input.read() != '!') {
                    ;
                }
                byte[] header = new byte[2];
                if (input.read(header) != 2) {
                    continue;
                }
                int function = header[0] & 0xFF;

                FunctionReceiver receiver = receivers.get(function);
                int length = header[1] & 0xFF;
                if (receiver != null) {
                    if (length > receiver.getMaxMessageSize()) {
                        continue; // Protect against corrupted data
                    }
                    byte[] data = new byte[length];
                    int n_read = 0;
                    while (n_read < length) {
                        int code = input.read(data, n_read, length - n_read);
                        if (code == -1) {
                            break;
                        } else {
                            n_read += code;
                        }
                    }
                    if (n_read == length) {
                        receiver.receivedUpdate(data);
                    } else {

                        logger.warn("While reading from serial port, expected " + length + " bytes but only read "
                                + n_read);
                    }
                } else {
                    // For unknown codes, we'll read up to 8 data bytes (works for most cases, will re-sync)
                    byte[] data = new byte[Math.min(length, 8)];
                    input.read(data);
                    System.out.println("Unknown function " + function + " with data: " + bytesToHex(data));
                }
            }
        } catch (InterruptedException | IOException e) {
            connected = false;
            logger.error("Error while reading from serial port (or waiting)", e);
            serialPort.disconnect();
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

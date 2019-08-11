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

    public void inputWrapper(byte[] buffer, int n_requested) throws IOException {
        int n_read = 0;
        while (connected && n_read < n_requested) {
            int n_ew = input.read(buffer, n_read, n_requested - n_read);
            if (n_ew > 0) {
                n_read += n_ew;
            }
        }
        if (!connected) {
            throw new IOException();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000); // Seems we need to wait a little to be able to update state
            while (connected) {
                StringBuilder junc = new StringBuilder("Junk data discarded: ");
                int n = 0, c;
                while ((c = input.read()) != '!') {
                    if (c == -1) {
                        continue;
                    } else {
                        junc.append(c);
                        junc.append(" ");
                        n++;
                    }
                }
                if (n > 0) {
                    logger.info(junc.toString());
                }
                byte[] header = new byte[2];
                inputWrapper(header, 2);
                int function = header[0] & 0xFF;

                FunctionReceiver receiver = receivers.get(function);
                int length = header[1] & 0xFF;
                if (receiver != null) {
                    if (length > receiver.getMaxMessageSize()) {
                        continue; // Protect against corrupted data
                    }
                    byte[] data = new byte[length];
                    inputWrapper(data, length);
                    receiver.receivedUpdate(data);
                } else {
                    // For unknown codes, we'll read up to 8 data bytes (works for most cases, will re-sync)
                    byte[] data = new byte[Math.min(length, 8)];
                    inputWrapper(data, data.length);
                    logger.debug("Unknown function " + function + " with data: " + bytesToHex(data));
                }
            }
        } catch (InterruptedException | IOException e) {
            if (connected) {
                connected = false;
                logger.error("Error while reading from serial port (or waiting)", e);
                serialPort.disconnect();
                updateStatus(ThingStatus.OFFLINE);
            }
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

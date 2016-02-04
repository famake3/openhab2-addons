/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.serialmultifunction.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
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
public class SerialMultiFunctionHandler extends BaseThingHandler implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SerialMultiFunctionHandler.class);
    private NRSerialPort serialPort;
    private final static int BAUD = 115200;
    private boolean connected = false;
    private Map<Integer, FunctionReceiver> receivers;

    public SerialMultiFunctionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        String portName = (String) getThing().getConfiguration().get("port");
        if (portName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            receivers = new HashMap<Integer, FunctionReceiver>();
            serialPort = new NRSerialPort(portName, BAUD);
            connect();
        }
    }

    @Override
    public void dispose() {
        if (connected && serialPort != null) {
            serialPort.disconnect();
        }
        connected = false;
    }

    private void connect() {
        if (!connected) {
            connected = serialPort.connect();
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
        connect();
        try {
            serialPort.getOutputStream().write('!');
            serialPort.getOutputStream().write(functionId);
            serialPort.getOutputStream().write(data.length);
            serialPort.getOutputStream().write(data);
        } catch (IOException e) {
            connected = false;
            serialPort.disconnect();
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                while (serialPort.getInputStream().read() != '!') {
                    ;
                }
                byte[] header = new byte[2];
                int off = 0;
                while (off < 2) {
                    off += serialPort.getInputStream().read(header, off, 2 - off);
                }
                int function = header[0] & 0xFF;
                int length = header[1] & 0xFF;
                byte[] data = new byte[length];
                off = 0;
                while (off < length) {
                    off += serialPort.getInputStream().read(data, off, length - off);
                }
                System.out.println("Received " + function);
                FunctionReceiver receiver = receivers.get(function);
                if (receiver != null) {
                    receiver.receivedUpdate(data);
                }
            }
        } catch (IOException e) {
            connected = false;
            serialPort.disconnect();
            updateStatus(ThingStatus.OFFLINE);
        }
    }
}

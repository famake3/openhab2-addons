/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.folding.discovery.FoldingSlotDiscoveryService;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * The {@link FoldingClientHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author FaMaKe - Initial contribution
 */
public class FoldingClientHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(FoldingClientHandler.class);

    private ScheduledFuture<?> refreshJob;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private boolean initializing = true;

    private Socket activeSocket;
    private BufferedReader socketReader;
    private Gson gson;

    private volatile int idRefresh = 0;

    private Map<String, SlotUpdateListener> slotUpdateListeners = new HashMap<String, SlotUpdateListener>();

    public FoldingClientHandler(Bridge thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                refresh();
            } else if (channelUID.getId().equals("run")) {
                if (command == OnOffType.ON) {
                    sendCommand("unpause");
                } else if (command == OnOffType.OFF) {
                    sendCommand("pause");
                }
                refresh();
                delayedRefresh();
            } else if (channelUID.getId().equals("finish")) {
                if (command == OnOffType.ON) {
                    sendCommand("finish");
                } else if (command == OnOffType.OFF) {
                    sendCommand("unpause");
                }
                refresh();
                delayedRefresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnected();
        }
    }

    @Override
    public void initialize() {
        BigDecimal period = (BigDecimal) getThing().getConfiguration().get("polling");
        if (period != null && period.longValue() != 0) {
            refreshJob = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, 5, period.longValue(), TimeUnit.SECONDS);
        } else {
            refresh();
        }
    }

    @Override
    public synchronized void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        closeSocket();
    }

    public synchronized void refresh() {
        initializing = false;
        List<SlotInfo> slotList = null;
        try {
            Socket s = getSocket();
            s.getOutputStream().write(("slot-info\r\n").getBytes());
            socketReader.readLine(); // Discard PyON header
            JsonReader jr = new JsonReader(socketReader);
            jr.setLenient(true);
            Type slotListType = new TypeToken<List<SlotInfo>>() {
            }.getType();

            slotList = gson.fromJson(jr, slotListType);
        } catch (IOException e) {
            e.printStackTrace();
            disconnected();
            return;
        }
        boolean running = false, finishing = true;
        for (SlotInfo si : slotList) {
            finishing &= "FINISHING".equals(si.status);
            running |= "FINISHING".equals(si.status) || "RUNNING".equals(si.status);
            SlotUpdateListener listener = slotUpdateListeners.get(si.id);
            if (listener != null) {
                listener.refreshed(si);
            } else {
                logger.debug("Providing a new discovery result for slot " + si.id);
                Collection<ServiceReference<DiscoveryService>> references;
                try {
                    references = bundleContext.getServiceReferences(DiscoveryService.class,
                            "(objectClass=" + FoldingSlotDiscoveryService.class.getName() + ")");
                } catch (InvalidSyntaxException e1) {
                    throw new RuntimeException("Search string became invalid");
                }
                if (references.isEmpty()) {
                    logger.warn("The Folding slot discovery service (" + FoldingSlotDiscoveryService.class.getName()
                            + ") is not available right now");
                }
                for (ServiceReference<DiscoveryService> ref : references) {
                    try {
                        FoldingSlotDiscoveryService service = (FoldingSlotDiscoveryService) bundleContext
                                .getService(ref);
                        if (service != null) {
                            logger.debug("Offering the new slot to discovery service");
                            String host = (String) getThing().getConfiguration().get("host");
                            service.newSlot(getThing().getUID(), host, si.id, si.description);
                        }
                    } catch (ClassCastException e) {
                    }
                }
            }
        }
        updateState(getThing().getChannel("run").getUID(), running ? OnOffType.ON : OnOffType.OFF);
        updateState(getThing().getChannel("finish").getUID(), finishing ? OnOffType.ON : OnOffType.OFF);
    }

    public void delayedRefresh() {
        final int i_refresh = ++idRefresh;
        refreshJob = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (i_refresh == idRefresh) { // Make a best effort to not run multiple deferred refresh
                    refresh();
                }
            }
        }, 5, TimeUnit.SECONDS);

    }

    void closeSocket() {
        if (activeSocket != null && activeSocket.isConnected()) {
            try {
                socketReader.close();
            } catch (IOException e) {
            }
        }
        socketReader = null;
        activeSocket = null;
    }

    private void disconnected() {
        closeSocket();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    private synchronized Socket getSocket() throws IOException {
        if (activeSocket == null) {
            String cfgHost = (String) getThing().getConfiguration().get("host");
            BigDecimal cfgPort = (BigDecimal) getThing().getConfiguration().get("port");
            String password = (String) getThing().getConfiguration().get("password");
            if (cfgHost == null || cfgHost.isEmpty()) {
                throw new IOException("Host was not configured");
            } else if (cfgPort == null || cfgPort.intValue() == 0) {
                throw new IOException("Port was not configured");
            }
            activeSocket = new Socket();
            activeSocket.connect(new InetSocketAddress(cfgHost, cfgPort.intValue()), 2000);
            socketReader = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
            readUntilPrompt(activeSocket); // Discard initial banner message
            if (password != null) {
                activeSocket.getOutputStream().write(("auth \"" + password + "\"\r\n").getBytes());
                if (readUntilPrompt(activeSocket).startsWith("OK")) { // Discard initial banner message
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect password");
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }
        return activeSocket;
    }

    private synchronized String readUntilPrompt(Socket s) throws IOException {
        boolean havePrompt1 = false;
        StringBuilder response = new StringBuilder();
        try {
            while (true) {
                int c = socketReader.read();
                if (havePrompt1) {
                    if (c == ' ') {
                        return response.toString();
                    } else {
                        response.append((char) c);
                    }
                }
                response.append((char) c);
                havePrompt1 = (c == '>');
            }
        } catch (IOException e) {
            disconnected();
            throw e;
        }
    }

    public synchronized void sendCommand(String command) throws IOException {
        try {
            Socket s = getSocket();
            s.getOutputStream().write((command + "\r\n").getBytes());
            readUntilPrompt(s);
        } catch (IOException e) {
            disconnected();
            throw e;
        }
    }

    public void registerSlot(String id, SlotUpdateListener slotListener) {
        slotUpdateListeners.put(id, slotListener);
        if (!initializing) {
            delayedRefresh();
        }
    }
}

package org.openhab.binding.folding.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class SlotHandler extends BaseThingHandler implements SlotUpdateListener {

    public SlotHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        getBridgeHandler().registerSlot(myId(), this);
    }

    private FoldingClientHandler getBridgeHandler() {
        return (FoldingClientHandler) super.getBridge().getHandler();
    }

    private String myId() {
        return (String) getThing().getConfiguration().get("id");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals("pause")) {
                if (command == OnOffType.ON) {
                    getBridgeHandler().sendCommand("pause " + myId());
                } else if (command == OnOffType.OFF) {
                    getBridgeHandler().sendCommand("unpause " + myId());
                }
            } else if (channelUID.getId().equals("finish")) {
                if (command == OnOffType.ON) {
                    getBridgeHandler().sendCommand("finish " + myId());
                } else if (command == OnOffType.OFF) {
                    getBridgeHandler().sendCommand("unpause " + myId());
                }
            }
            getBridgeHandler().delayedRefresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshed(SlotInfo si) {
        updateStatus(ThingStatus.ONLINE);
        updateState(getThing().getChannel("status").getUID(), new StringType(si.status));
        boolean finishing = "FINISHING".equals(si.status);
        boolean paused = "true".equals(si.options.get("paused"));
        updateState(getThing().getChannel("finish").getUID(), finishing ? OnOffType.ON : OnOffType.OFF);
        updateState(getThing().getChannel("pause").getUID(), paused ? OnOffType.ON : OnOffType.OFF);
        updateState(getThing().getChannel("description").getUID(), new StringType(si.description));
    }

}

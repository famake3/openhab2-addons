package org.openhab.binding.ninjablocks.handler;

import static org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants;
import org.openhab.binding.ninjablocks.internal.GlobalBridge;

public class NinjaTriggerHandler extends BaseThingHandler implements NinjaThingEventListener {

    private GlobalBridge globalBridge;
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_MOTION_SENSOR, THING_TYPE_BUTTON));

    public NinjaTriggerHandler(Thing thing, GlobalBridge globalBridge) {
        super(thing);
        this.globalBridge = globalBridge;
    }

    @Override
    public void initialize() {
        int code = Integer.parseInt((String) getThing().getConfiguration().get("code"), 16);
        globalBridge.get().registerCallbackForThing(code, this);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void ninjaEvent(int code) {
        ChannelUID channelUID = getThing().getChannel(NinjaBlocksBindingConstants.TRIGGER).getUID();
        updateState(channelUID, OnOffType.ON);
    }

}

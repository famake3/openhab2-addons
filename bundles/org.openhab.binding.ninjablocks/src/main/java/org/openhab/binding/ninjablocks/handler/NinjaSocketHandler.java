package org.openhab.binding.ninjablocks.handler;


import static org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ninjablocks.internal.GlobalBridge;

public class NinjaSocketHandler extends BaseThingHandler {

	public final  static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = 
			Collections.singleton(THING_TYPE_SOCKET);
	private final GlobalBridge globalBridge;
	
	public NinjaSocketHandler(Thing thing, GlobalBridge globalBridge) {
		super(thing);
		this.globalBridge = globalBridge;
	}

	@Override
	public void initialize() {
		updateStatus(ThingStatus.ONLINE);
	}
	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if(channelUID.getId().equals(POWER)) {
        	setPower(channelUID, command.equals(OnOffType.ON));
        }
	}

	private void setPower(ChannelUID channelUID, boolean on) {
		int command = 0;
		if (on) {
			command = Integer.parseInt((String)getThing().getConfiguration().get("on_code"), 16);
		}
		else {
			command = Integer.parseInt((String)getThing().getConfiguration().get("off_code"), 16);
		}
		globalBridge.get().sendCommand(command);
	}

}

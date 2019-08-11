package org.openhab.binding.ninjablocks.handler;

import static org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants.THING_TYPE_DOUBLE_CONTACT;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants;
import org.openhab.binding.ninjablocks.internal.GlobalBridge;

import com.google.common.collect.Sets;

public class NinjaDoubleContactHandler extends BaseThingHandler
			implements NinjaThingEventListener {

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = 
			Sets.newHashSet(THING_TYPE_DOUBLE_CONTACT);
	private GlobalBridge globalBridge;
	private int open_code;
	private int closed_code;

	public NinjaDoubleContactHandler(Thing thing, GlobalBridge globalBridge) {
		super(thing);
		this.globalBridge = globalBridge;
	}

	@Override
	public void initialize() {
		open_code = Integer.parseInt((String)getThing().getConfiguration().get("open_code"), 16);
		closed_code = Integer.parseInt((String)getThing().getConfiguration().get("closed_code"), 16);
		globalBridge.get().registerCallbackForThing(open_code, this);
		globalBridge.get().registerCallbackForThing(closed_code, this);
		updateStatus(ThingStatus.ONLINE);
	}
	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

	@Override
	public void ninjaEvent(int code) {
		ChannelUID channelUID = getThing().
				getChannel(NinjaBlocksBindingConstants.CONTACT).getUID();
		if (code == open_code) {
			updateState(channelUID, OpenClosedType.OPEN);
		}
		else if (code == closed_code) {
			updateState(channelUID, OpenClosedType.CLOSED);
		}
	}
}

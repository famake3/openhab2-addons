package org.openhab.binding.ninjablocks.handler;

import static org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants.THING_TYPE_TEMPERATURE_HUMIDITY;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ninjablocks.NinjaBlocksBindingConstants;
import org.openhab.binding.ninjablocks.internal.GlobalBridge;

public class NinjaTempHumidHandler extends BaseThingHandler
		implements NinjaSensorEventListener {

	public final  static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = 
			Collections.singleton(THING_TYPE_TEMPERATURE_HUMIDITY);
	private final GlobalBridge globalBridge;
	private String tempGuid;
	private String humidGuid;
	
	public NinjaTempHumidHandler(Thing thing, GlobalBridge globalBridge) {
		super(thing);
		this.globalBridge = globalBridge;
	}

	@Override
	public void initialize() {
		int index = Integer.parseInt((getThing().getConfiguration().get("sensor_no")).toString());
		if (index >= 0 && index < 10) {
			String baseGuid = globalBridge.get().getBlockId() + "_010" + index + "_0_3";
			tempGuid = baseGuid + "1";
			humidGuid = baseGuid + "0";
			globalBridge.get().registerSensorCallback(tempGuid, this);
			globalBridge.get().registerSensorCallback(humidGuid, this);
			updateStatus(ThingStatus.ONLINE);
		}
		else {
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void dispose() {
		globalBridge.get().unregisterCallback(tempGuid);
		globalBridge.get().unregisterCallback(humidGuid);
	}
	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

	@Override
	public void sensorEvent(String guid, String data) {
		if (tempGuid.equals(guid)) {
			ChannelUID channelUID = getThing().
					getChannel(NinjaBlocksBindingConstants.TEMPERATURE).getUID();
			updateState(channelUID, new DecimalType(data));
		}
		else if (humidGuid.equals(guid)) {
			ChannelUID channelUID = getThing().
					getChannel(NinjaBlocksBindingConstants.HUMIDITY).getUID();
			updateState(channelUID, new DecimalType(data));
		}
	}

}

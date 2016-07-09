package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class SwitchHandler extends BaseThingHandler {

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int funcId = ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        if (channelUID.getId().equals("switch")) {
            ((SerialMultiFunctionHandler) getBridge().getHandler()).send(funcId,
                    new byte[] { (byte) ((command == OnOffType.ON) ? 1 : 0) });
        }
    }

}

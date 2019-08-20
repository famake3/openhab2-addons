package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class OnOffCodeHandler extends BaseThingHandler {

    public OnOffCodeHandler(Thing thing) {
        super(thing);
    }

    public byte[] getCode(String configParameter) throws NotConfiguredException {
        String codeHex = (String) getThing().getConfiguration().get(configParameter);
        if (codeHex != null) {
            if ((codeHex.length() & 1) == 1) {
                throw new NotConfiguredException("Code " + configParameter + " must be an even number of hex digits.");
            }
            int len = codeHex.length() / 2;
            byte[] data = new byte[len];
            for (int i = 0; i < len * 2; i += 2) {
                byte b = (byte) Short.parseShort(codeHex.substring(i, i + 2), 16);
                data[i / 2] = b;
            }
            return data;
        } else {
            throw new NotConfiguredException("Code " + configParameter + " not configured.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int funcId = ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        try {
            Bridge bridge = getBridge();
            if (bridge != null) {
                SerialMultiFunctionHandler handler = (SerialMultiFunctionHandler) bridge.getHandler();
                if (handler != null) {
                    if (command == OnOffType.ON) {
                        handler.send(funcId, getCode("on_code"));
                    }
                    if (command == OnOffType.OFF) {
                        handler.send(funcId, getCode("off_code"));
                    }
                }
            }
        } catch (NotConfiguredException e) {
            e.printStackTrace();
        }
    }

}

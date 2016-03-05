package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class CodeSenderHandler extends BaseThingHandler {

    public CodeSenderHandler(Thing thing) {
        super(thing);
    }

    public byte[] getCode(String codeHex) {
        int len = codeHex.length() / 2;
        byte[] data = new byte[len];
        for (int i = 0; i < len * 2; i += 2) {
            byte b = (byte) Short.parseShort(codeHex.substring(i, i + 2), 16);
            data[i / 2] = b;
        }
        return data;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int funcId = ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        if (command instanceof StringType) {
            ((SerialMultiFunctionHandler) getBridge().getHandler()).send(funcId, getCode(command.toString()));
        }
    }

}

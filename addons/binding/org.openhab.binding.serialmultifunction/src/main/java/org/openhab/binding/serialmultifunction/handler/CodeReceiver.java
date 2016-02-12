package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class CodeReceiver extends BaseThingHandler implements FunctionReceiver {

    public CodeReceiver(Thing thing) {
        super(thing);
        SerialMultiFunctionHandler bridge = (SerialMultiFunctionHandler) getBridge().getHandler();
        if (getFunctionId() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            bridge.addFunctionReceiver(getFunctionId(), this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void receivedUpdate(byte[] data) {

    }

    private int getFunctionId() {
        try {
            return ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        } catch (ClassCastException e) {
            return 0; // Unconfigured
        }
    }

}

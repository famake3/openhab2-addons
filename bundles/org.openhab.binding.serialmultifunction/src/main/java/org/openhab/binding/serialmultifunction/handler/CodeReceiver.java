package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class CodeReceiver extends BaseThingHandler implements FunctionReceiver {

    public CodeReceiver(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        SerialMultiFunctionHandler bridge = (SerialMultiFunctionHandler) getBridge().getHandler();
        if (getFunctionId() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            bridge.addFunctionReceiver(getFunctionId(), this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void receivedUpdate(byte[] data) {
        updateStatus(ThingStatus.ONLINE);
        ChannelUID channelUID = getThing().getChannel("code-input").getUID();
        postCommand(channelUID, new StringType(SerialMultiFunctionHandler.bytesToHex(data)));
    }

    private int getFunctionId() {
        try {
            return ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        } catch (ClassCastException e) {
            return 0; // Unconfigured
        }
    }

    @Override
    public int getMaxMessageSize() {
        // Setting a somewhat arbitrary limit here
        // 16 byte is the size of a GUID, unlikely that people want something
        // bigger
        return 17;
    }

}

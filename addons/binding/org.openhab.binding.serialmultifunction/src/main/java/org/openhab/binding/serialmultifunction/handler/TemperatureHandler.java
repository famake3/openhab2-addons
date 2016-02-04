package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

public class TemperatureHandler extends BaseThingHandler implements FunctionReceiver {

    public TemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        SerialMultiFunctionHandler bridge = (SerialMultiFunctionHandler) getBridge().getHandler();
        if (getFunctionId() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            bridge.addFunctionReceiver(getFunctionId(), this);
            refresh();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private int getFunctionId() {
        try {
            return ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
        } catch (ClassCastException e) {
            return 0; // Unconfigured
        }
    }

    private void refresh() {
        if (getFunctionId() != 0) {
            ((SerialMultiFunctionHandler) getBridge().getHandler()).send(getFunctionId(), new byte[] {});
        }
    }

    @Override
    public void receivedUpdate(byte[] data) {
        updateStatus(ThingStatus.ONLINE);
        /// -128 ... 128
        int val = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        double volt = (5.0 * val / 1024.0);
        double temp = (volt - 0.5) * 100.0;
        temp = Math.round(temp * 10.0) / 10.0;
        ChannelUID channelUID = getThing().getChannel("temperature").getUID();
        updateState(channelUID, new DecimalType(temp));
    }

}

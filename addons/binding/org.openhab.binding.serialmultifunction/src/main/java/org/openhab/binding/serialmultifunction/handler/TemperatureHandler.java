package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
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
        bridge.addFunctionReceiver(getFunctionId(), this);
        refresh();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private int getFunctionId() {
        return ((BigDecimal) getThing().getConfiguration().get("func_id")).intValue();
    }

    private void refresh() {
        ((SerialMultiFunctionHandler) getBridge().getHandler()).send(getFunctionId(), new byte[] {});
    }

    @Override
    public void receivedUpdate(byte[] data) { // -1 255 | -2 254 | -3 253 | -127
        /// -128 ... 128
        int val = ((data[0] << 8) & 0xFF) | (data[1] & 0xFF);
        double volt = (5.0 * val / 1024.0);
        double temp = (volt - 0.5) * 100.0;
        temp = Math.round(temp * 10.0) / 10.0;
        ChannelUID channelUID = getThing().getChannel("temperature").getUID();
        updateState(channelUID, new DecimalType(temp));
    }

}

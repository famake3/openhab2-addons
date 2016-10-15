package org.openhab.binding.serialmultifunction.handler;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

public class DecimalTemperatureHandler extends BaseThingHandler implements FunctionReceiver {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DecimalTemperatureHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        SerialMultiFunctionHandler bridge = (SerialMultiFunctionHandler) getBridge().getHandler();
        if (getFunctionId() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            bridge.addFunctionReceiver(getFunctionId(), this);
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, 10, TimeUnit.SECONDS);
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
        /// Using default shift behaviour of sign extension
        int val = (data[0] << 8) | (data[1] & 0xFF);
        double temp = val / 10.0;
        // Improvements welcome :)
        BigDecimal tempbig = BigDecimal.valueOf(temp);
        ChannelUID channelUID = getThing().getChannel("temperature").getUID();
        updateState(channelUID, new DecimalType(tempbig));
    }

}

package org.openhab.binding.fmklifx.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.fmklifx.LifxBindingConstants;
import org.openhab.binding.fmklifx.protocol.LifxColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifxColorLightHandler extends LifxLightHandlerBase {

    private Logger logger = LoggerFactory.getLogger(LifxLightHandlerBase.class);

    private ChannelUID colorUid, colorTemperatureUid, colorTemperatureLatchedUid, transitionTimeUid, powerUid;
    private HSBType currentColor;
    private DecimalType currentColorTemperature, currentTransitionTime;
    private boolean bufferedMode = false;

    public LifxColorLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        colorUid = getThing().getChannel(LifxBindingConstants.CHANNEL_COLOR).getUID();
        colorTemperatureUid = getThing().getChannel(LifxBindingConstants.CHANNEL_COLOR_TEMPERATURE).getUID();
        colorTemperatureLatchedUid = getThing().getChannel(LifxBindingConstants.CHANNEL_COLOR_TEMPERATURE_BUFFERED)
                .getUID();
        transitionTimeUid = getThing().getChannel(LifxBindingConstants.CHANNEL_TRANSITION_TIME).getUID();
        powerUid = getThing().getChannel(LifxBindingConstants.CHANNEL_POWER).getUID();
        currentTransitionTime = new DecimalType(200.0);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            poll();
        } else {
            if (channelUID.equals(colorUid)) {
                handleColorCommand(command);
            } else if (channelUID.equals(colorTemperatureUid)) {
                handleColorTemperatureCommand(command, true);
            } else if (channelUID.equals(colorTemperatureLatchedUid)) {
                handleColorTemperatureCommand(command, false);
            } else if (channelUID.equals(transitionTimeUid)) {
                handleTransitionTimeComand(command);
            } else if (channelUID.equals(powerUid)) {
                handlePowerCommand(command);
            }
        }
    }

    private void handleColorCommand(Command command) {
        if (command instanceof HSBType) {
            currentColor = (HSBType) command;
            sendColorCommand();
            updateState(colorUid, currentColor);
        } else if (command instanceof IncreaseDecreaseType) {
            int brightnessValue = currentColor.getBrightness().intValue();
            int nextBrightnessValue;
            if (command == IncreaseDecreaseType.DECREASE) {
                nextBrightnessValue = Math.max(0, brightnessValue - 5);
            } else {
                nextBrightnessValue = Math.min(100, brightnessValue + 5);
            }
            HSBType nextHsb = new HSBType(currentColor.getHue(), currentColor.getSaturation(),
                    new PercentType(nextBrightnessValue));
            currentColor = nextHsb;
            sendColorCommand();
            updateState(colorUid, currentColor);
        } else if (command instanceof PercentType) {
            currentColor = new HSBType(currentColor.getHue(), currentColor.getSaturation(), (PercentType) command);
            sendColorCommand();
            updateState(colorUid, currentColor);
        }
    }

    private void handlePowerCommand(Command command) {
        if (command instanceof OnOffType) {
            protocol.setPower(device, currentTransitionTime.intValue(), command == OnOffType.ON);
            updateState(powerUid, (OnOffType) command);
            resetTransitionTime();
        }
    }

    private DecimalType modifyDecimalType(DecimalType currentValue, double min, double max, double increment,
            Command command) {
        if (command instanceof DecimalType) {
            return (DecimalType) command;
        } else if (command instanceof IncreaseDecreaseType) {
            double oldValue = currentValue.doubleValue(), newValue;
            if (command == IncreaseDecreaseType.DECREASE) {
                newValue = Math.max(min, oldValue - increment);
            } else {
                newValue = Math.min(max, oldValue + increment);
            }
            return new DecimalType(newValue);
        } else {
            logger.info("Unexpected command " + command + " received for numeric type");
            return currentValue;
        }
    }

    private void handleColorTemperatureCommand(Command command, boolean commit) {
        currentColorTemperature = modifyDecimalType(currentColorTemperature, 2500, 9000, 500, command);
        updateState(colorTemperatureLatchedUid, currentColorTemperature);
        updateState(colorTemperatureUid, currentColorTemperature);
        if (commit) {
            sendColorCommand();
        } else {
            bufferedMode = true;
        }
    }

    private void handleTransitionTimeComand(Command command) {
        currentTransitionTime = modifyDecimalType(currentTransitionTime, 0.0, Integer.MAX_VALUE, 50, command);
        updateState(transitionTimeUid, currentTransitionTime);
    }

    private void sendColorCommand() {
        double hue = currentColor.getHue().doubleValue() / 360.0;
        double saturation = currentColor.getSaturation().doubleValue() / 100.0;
        double brightness = currentColor.getBrightness().doubleValue() / 100.0;
        double colorTemperature = currentColorTemperature.doubleValue();
        LifxColor lifxColor = new LifxColor(hue, saturation, brightness, colorTemperature);
        protocol.setColor(device, currentTransitionTime.intValue(), lifxColor);
        resetTransitionTime();
        bufferedMode = false;
    }

    private void resetTransitionTime() {
        DecimalType oldTransitionTime = currentTransitionTime;
        currentTransitionTime = new DecimalType(
                (BigDecimal) getThing().getConfiguration().get("default-transition-time"));
        if (!oldTransitionTime.equals(currentTransitionTime)) {
            updateState(transitionTimeUid, currentTransitionTime);
        }
    }

    @Override
    public void color(LifxColor color) {
        online();
        DecimalType hue = new DecimalType(color.hue * 360.0);
        PercentType saturation = new PercentType(((int) Math.round(color.saturation * 100.0)));
        PercentType brightness = new PercentType(((int) Math.round(color.brightness * 100.0)));
        currentColor = new HSBType(hue, saturation, brightness);
        updateState(colorUid, currentColor);
        currentColorTemperature = new DecimalType(color.colorTemperature);
        if (!bufferedMode) {
            updateState(colorTemperatureUid, currentColorTemperature);
        }
    }

    @Override
    public void power(boolean on) {
        online();
        updateState(powerUid, on ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void poll() {
        protocol.queryLightState(device);
    }

}

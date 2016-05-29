/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.artnet.handler;

import static org.openhab.binding.artnet.ArtNetBindingConstants.COLOR;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.artnet.internal.ArtNetSender;

/**
 * The {@link ArtNetHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Famake - Initial contribution
 */
public class ArtNetHandler extends BaseThingHandler {

    private static final int MAX_PIXELS_PER_PACKET = 170;

    // Logging could be implemented in the future:
    // private Logger logger = LoggerFactory.getLogger(ArtNetHandler.class);

    private final ArtNetSender artNetSender;
    boolean on = true;
    private double h = 58;
    private double s = 30, b = 100;
    private long lastSend = 0;

    public ArtNetHandler(Thing thing) throws SocketException {
        super(thing);
        artNetSender = new ArtNetSender();
    }

    @Override
    public void initialize() {
        BigDecimal r = (BigDecimal) getThing().getConfiguration().get("startup-color-r");
        BigDecimal g = (BigDecimal) getThing().getConfiguration().get("startup-color-g");
        BigDecimal blue = (BigDecimal) getThing().getConfiguration().get("startup-color-b");
        if (r != null && g != null && blue != null) {
            float[] rgb = new float[] { (float) (r.doubleValue() / 256.0), (float) (g.doubleValue() / 256.0),
                    (float) (blue.doubleValue() / 256.0) };
            Color colorSrgb = new Color(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), rgb, (float) 1.0);
            colorSrgb.getColorComponents(ColorSpace.getInstance(ColorSpace.CS_sRGB), rgb);
            // colorRgblin.RGB
            float[] hsb = new float[3];
            Color.RGBtoHSB((int) (rgb[0] * 256), (int) (rgb[1] * 256), (int) (rgb[2] * 256), hsb);
            h = 360.0 * hsb[0];
            s = (int) (100.0 * hsb[1]);
            b = (int) (100.0 * hsb[2]);
        }

        ChannelUID channelUID = getThing().getChannel(COLOR).getUID();
        updateStatus(ThingStatus.ONLINE);
        updateState(channelUID, new HSBType(new DecimalType(h), new PercentType(BigDecimal.valueOf(s)),
                new PercentType(BigDecimal.valueOf(b))));
        try {
            sendColor(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(COLOR)) {
            try {
                if (command instanceof HSBType) {
                    handleHSBCommand((HSBType) command);
                } else if (command instanceof PercentType) {
                    handlePercentCommand((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseCommand((IncreaseDecreaseType) command);
                }
                if (on) {
                    updateState(channelUID, new HSBType(new DecimalType(h), new PercentType(BigDecimal.valueOf(s)),
                            new PercentType(BigDecimal.valueOf(b))));
                } else {
                    updateState(channelUID, new HSBType(new DecimalType(h), new PercentType(BigDecimal.valueOf(s)),
                            new PercentType(0)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleHSBCommand(HSBType command) throws IOException {
        h = command.getHue().doubleValue();
        s = command.getSaturation().doubleValue();
        b = command.getBrightness().doubleValue();
        sendColor(false);
    }

    private void handleIncreaseDecreaseCommand(IncreaseDecreaseType command) throws IOException {
        if (command == IncreaseDecreaseType.DECREASE) {
            b = Math.max(0, b - 10);
        } else {
            b = Math.min(100, b + 10);
        }
        sendColor(false);
    }

    private void handleOnOffCommand(OnOffType command) throws IOException {
        if (command == OnOffType.ON) {
            sendColor(false);
        } else {
            sendColor(true);
        }
    }

    private void handlePercentCommand(PercentType command) throws IOException {
        b = command.intValue();
        sendColor(false);
    }

    private void sendColor(boolean setOff) throws IOException {
        int num = ((BigDecimal) (getThing().getConfiguration().get("num-pixels"))).intValue();
        BigDecimal universeBD = (BigDecimal) getThing().getConfiguration().get("start-universe");
        int universe = 0;
        if (universeBD != null) {
            universe = universeBD.intValue();
        }
        String ipAddress = (String) getThing().getConfiguration().get("ip-address");

        double red, green, blue;
        if (setOff) {
            red = green = blue = 0;
            on = false;
        } else {
            Color value = Color.getHSBColor((float) (h / 360.0), (float) (s / 100.0), (float) (b / 100.0));
            float[] moo = new float[3];
            value.getColorComponents(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), moo);
            red = moo[0] * 256;
            green = moo[1] * 256;
            blue = moo[2] * 256;
            on = (red + green + blue) > 0;
        }

        if (ipAddress == null) {
            System.out.println("No IP address configured!");
            return;
        }
        while (num > 0) {
            int n_packet = Math.min(MAX_PIXELS_PER_PACKET, num);
            byte[] data = getData(n_packet, red, green, blue);
            artNetSender.sendData(ipAddress, universe, data);
            universe++;
            num -= n_packet;
            if (num > 0) {
                if (System.currentTimeMillis() - 30000 > lastSend) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
            lastSend = System.currentTimeMillis();
        }
    }

    /*
     * private byte[] getData(int num_pixels, double r, double g, double b) {
     * byte[] data = new byte[3 * num_pixels];
     * int ir = (int) r, ig = (int) g, ib = (int) b;
     * double rx = r - ir, gx = g - ig, bx = b - ib;
     * double accR = 0.0, accG = 0.0, accB = 0.0;
     * for (int i = 0; i < num_pixels; ++i) {
     * int incR = (int) accR, incG = (int) accG, incB = (int) accB;
     * accR += (rx - incR);
     * accG += (gx - incG);
     * accB += (bx - incB);
     * data[i * 3] = (byte) (ir + incR);
     * data[i * 3 + 1] = (byte) (ig + incG);
     * data[i * 3 + 2] = (byte) (ib + incB);
     * }
     * return data;
     * }
     */
    private byte[] getData(int num_pixels, double r, double g, double b) {
        int ir = (int) r, ig = (int) g, ib = (int) b;
        byte[] data = new byte[3 * num_pixels];
        double rx = r - ir, gx = g - ig, bx = b - ib;
        for (int i = 0; i < num_pixels; ++i) {
            data[i * 3] = (byte) (ir + (Math.random() < rx ? 1 : 0));
            data[i * 3 + 1] = (byte) (ig + (Math.random() < gx ? 1 : 0));
            data[i * 3 + 2] = (byte) (ib + (Math.random() < bx ? 1 : 0));
        }
        return data;
    }

}

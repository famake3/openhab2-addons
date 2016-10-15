/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.serialmultifunction;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SerialMultiFunctionBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author FaMaKe - Initial contribution
 */
public class SerialMultiFunctionBindingConstants {

    public static final String BINDING_ID = "serialmultifunction";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE_DECIMAL = new ThingTypeUID(BINDING_ID,
            "temperature-decimal");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID THING_TYPE_ON_OFF_CODE = new ThingTypeUID(BINDING_ID, "on-off-code");
    public final static ThingTypeUID THING_TYPE_CODE_RECEIVER = new ThingTypeUID(BINDING_ID, "code-receiver");

}

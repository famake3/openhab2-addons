/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ninjablocks;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NinjaBlocksBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Famake - Initial contribution
 */
public class NinjaBlocksBindingConstants {

    public static final String BINDING_ID = "ninjablocks";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_NINJA_RF = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motion_sensor");
    public final static ThingTypeUID THING_TYPE_BUTTON = new ThingTypeUID(BINDING_ID, "button");
    public final static ThingTypeUID THING_TYPE_SOCKET = new ThingTypeUID(BINDING_ID, "socket");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE_HUMIDITY = new ThingTypeUID(BINDING_ID, "temperature_humidity");
	public static final ThingTypeUID THING_TYPE_DOUBLE_CONTACT = new ThingTypeUID(BINDING_ID, "contact_sensor");
	
    // List of all Channel ids
    public final static String TRIGGER = "trigger";
    public final static String POWER = "power";

	public static final String TEMPERATURE = "temperature";
	public static final String HUMIDITY = "humidity";
	public static final String CONTACT = "contact";

}

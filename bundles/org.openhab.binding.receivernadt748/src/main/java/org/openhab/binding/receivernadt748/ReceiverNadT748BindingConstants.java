/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.receivernadt748;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ReceiverNadT748Binding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Mikael Aasen - Initial contribution
 */
public class ReceiverNadT748BindingConstants {

    public static final String BINDING_ID = "receivernadt748";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_RECEIVER = new ThingTypeUID(BINDING_ID, "receiver");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_MUTE = "mute";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_SOURCE = "source";

}

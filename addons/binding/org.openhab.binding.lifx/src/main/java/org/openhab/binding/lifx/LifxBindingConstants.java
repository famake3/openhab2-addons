/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lifx;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LifxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Famake - Initial contribution
 */
public class LifxBindingConstants {

    public static final String BINDING_ID = "lifx";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_LIGHT_COLOR = new ThingTypeUID(BINDING_ID, "light-color");
    public final static ThingTypeUID THING_TYPE_LIGHT_wHITE = new ThingTypeUID(BINDING_ID, "light-color");

    // List of all Channel ids
    public final static String CHANNEL_COLOR = "color";

}

/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.benqprojector;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BenqProjectorBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author famake - Initial contribution
 */
public class BenqProjectorBindingConstants {

    public static final String BINDING_ID = "benqprojector";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_PROJECTOR = new ThingTypeUID(BINDING_ID, "projector");

    // List of all Channel ids
    public final static String POWER = "power";

}

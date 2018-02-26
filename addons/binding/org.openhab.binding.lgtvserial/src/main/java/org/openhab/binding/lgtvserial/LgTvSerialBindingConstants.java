/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LgTvSerialBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
public class LgTvSerialBindingConstants {

    public static final String BINDING_ID = "lgtvserial";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_LGTV = new ThingTypeUID(BINDING_ID, "lgtv");

}

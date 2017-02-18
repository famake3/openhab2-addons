/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.handler;

import java.util.Map;

public class SlotInfo {

    // Entity for Json de-serialization

    public String id, status, description, reason;
    public Map<String, String> options;
    boolean idle;

}

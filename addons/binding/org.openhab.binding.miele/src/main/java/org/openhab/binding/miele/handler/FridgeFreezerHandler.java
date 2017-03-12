/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import static org.openhab.binding.miele.MieleBindingConstants.APPLIANCE_ID;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link FridgeFreezerHandler} is responsible for handling commands,
 * which are sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - fixed handling of REFRESH commands
 */
public class FridgeFreezerHandler extends MieleApplianceHandler<FridgeFreezerChannelSelector> {

    protected Logger logger = LoggerFactory.getLogger(FridgeFreezerHandler.class);

    public FridgeFreezerHandler(Thing thing) {
        super(thing, FridgeFreezerChannelSelector.class, "FridgeFreezer");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        super.handleCommand(channelUID, command);

        String channelID = channelUID.getId();
        String uid = (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);

        FridgeFreezerChannelSelector selector = (FridgeFreezerChannelSelector) getValueSelectorFromChannelID(channelID);
        JsonElement result = null;

        try {
            if (selector != null) {
                switch (selector) {
                    case SUPERCOOL: {
                        if (command.equals(OnOffType.ON)) {
                            result = bridgeHandler.invokeOperation(uid, modelID, "startSuperCooling");
                        } else if (command.equals(OnOffType.OFF)) {
                            result = bridgeHandler.invokeOperation(uid, modelID, "stopSuperCooling");
                        }
                        break;
                    }
                    case SUPERFREEZE: {
                        if (command.equals(OnOffType.ON)) {
                            result = bridgeHandler.invokeOperation(uid, modelID, "startSuperFreezing");
                        } else if (command.equals(OnOffType.OFF)) {
                            result = bridgeHandler.invokeOperation(uid, modelID, "stopSuperFreezing");
                        }
                        break;
                    }
                    default: {
                        logger.debug("{} is a read-only channel that does not accept commands",
                                selector.getChannelID());
                    }
                }
            }
            // process result
            if (result != null) {
                logger.debug("Result of operation is {}", result.getAsString());
            }
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                    channelID, command.toString());
        }
    }

}

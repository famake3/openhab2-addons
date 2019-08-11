/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ninjablocks.handler;

import java.util.HashMap;

import javax.servlet.ServletException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ninjablocks.internal.NinjaCallbackServlet;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * The {@link NinjaBlockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Famake - Initial contribution
 */
public class NinjaBlockHandler extends BaseBridgeHandler implements NinjaEventListener {

    //private Logger logger = LoggerFactory.getLogger(NinjaBlockHandler.class);
	private String callbackPath;
	private HttpService httpService;

	private Client client;
	private WebTarget apiTarget;
	
	private HashMap<Integer,NinjaThingEventListener> rfCommands;
	private HashMap<String,NinjaSensorEventListener> rfSensors;
    
	public NinjaBlockHandler(Bridge bridge, HttpService httpService) {
		super(bridge);
		rfCommands = new HashMap<>();
		rfSensors = new HashMap<>();
		client = ClientBuilder.newClient();
		this.httpService = httpService;
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        
	}

	@Override
	public void initialize() {
		updateStatus(ThingStatus.OFFLINE);
		String block_id = (String)getThing().getConfiguration().get("block-id");
		callbackPath = "/ninjablocks/" + block_id.trim();
		try {
			this.httpService.registerServlet(callbackPath, 
					new NinjaCallbackServlet(this), null, null);
		
		} catch (ServletException | NamespaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String accessToken = (String)getThing().getConfiguration().get("access-token");
		apiTarget = client.target("http://api.ninja.is/rest/v0/device")
				.queryParam("user_access_token", accessToken);
		
		// Register callback for simple RF433 devices like buttons, switches, etc
		registerCallbackWithNinjaBlocks(getCallbackUrl(), getRfGuid());

		updateStatus(ThingStatus.ONLINE);
	}
	
	private void registerCallbackWithNinjaBlocks(String url, String guid) {
		Response response = apiTarget.path(guid).path("callback")
				.request(MediaType.APPLICATION_JSON)
				.delete();
		
		response = apiTarget.path(guid).path("callback")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity("{ \"url\" : \"" + url + "\" }",
						MediaType.APPLICATION_JSON));
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not register callback!");
		}
	}

	private String getCallbackUrl() {
		String base = "http://";
		base += (String)getThing().getConfiguration().get("callback-host");
		base += ":";
		base += (String)getThing().getConfiguration().get("callback-port");
		return base + callbackPath;
	}
	
	public String getBlockId() {
		return ((String)getThing().getConfiguration().get("block-id"));
	}
	
	public String getRfGuid() {
		return getBlockId() + "_0_0_11";
	}

	@Override
	public void dispose() {
		this.httpService.unregister(callbackPath);
		unregisterCallback(getRfGuid());
	}
	

	@Override
	public void onNinjaEvent(String data, String guid, long timestamp) {
		if (getRfGuid().equals(guid)) {
			int command = Integer.parseInt(data, 2);
			NinjaThingEventListener listener = rfCommands.get(command);
			if (listener != null) 
				listener.ninjaEvent(command);
		}
		else {
			NinjaSensorEventListener sensor = rfSensors.get(guid);
			if (sensor != null) {
				sensor.sensorEvent(guid, data);
			}
		}
	}
    
	public void registerCallbackForThing(Integer command, NinjaThingEventListener thing) {
		rfCommands.put(command, thing);
	}

	public void registerSensorCallback(String guid, NinjaSensorEventListener listener) {
		rfSensors.put(guid, listener);
		registerCallbackWithNinjaBlocks(getCallbackUrl(), guid);
	}
	
	public void unregisterCallback(String guid) {
		apiTarget.path("callback")
				.request(MediaType.APPLICATION_JSON)
				.delete();
	}
	
	public void sendCommand(int command) {
		String binary = Integer.toBinaryString(command);
		while (binary.length() < 24)
			binary = "0" + binary;
		apiTarget.path(getRfGuid()).request(MediaType.APPLICATION_JSON)
			.put(Entity.entity("{ \"DA\" : \"" + binary +
					"\" }", MediaType.APPLICATION_JSON));
	}



	
}

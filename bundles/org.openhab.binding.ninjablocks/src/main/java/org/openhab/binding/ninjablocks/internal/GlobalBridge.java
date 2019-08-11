package org.openhab.binding.ninjablocks.internal;

import org.openhab.binding.ninjablocks.handler.NinjaBlockHandler;

public class GlobalBridge {

	public NinjaBlockHandler bridge;
	
	public GlobalBridge() {
	}
	
	public NinjaBlockHandler get() {
		if (bridge != null)
			return bridge;
		else 
			throw new IllegalStateException("There must be a bridge");
	}
}

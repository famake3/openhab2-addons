package org.openhab.binding.ninjablocks.handler;

public interface NinjaEventListener {

	void onNinjaEvent(String data, String guid, long timestamp);
	
}

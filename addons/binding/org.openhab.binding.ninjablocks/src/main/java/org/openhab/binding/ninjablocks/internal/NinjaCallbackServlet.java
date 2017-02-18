package org.openhab.binding.ninjablocks.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.ninjablocks.handler.NinjaEventListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class NinjaCallbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	NinjaEventListener ninjaEventListener;
	
	
	public NinjaCallbackServlet(NinjaEventListener ninjaEventListener) {
		this.ninjaEventListener = ninjaEventListener;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Got callback (GET)");

	      // Set response content type
	      response.setContentType("text/html");
	 
	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      out.println("<h1>hello</h1>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream in = request.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        char[] buffer = new char[4096];
        int n = reader.read(buffer);
        //System.out.println(new String(buffer, 0 , n));
        JsonElement ninjaData = new JsonParser().parse(new String(buffer, 0, n));
        String data = ninjaData.getAsJsonObject().get("DA").getAsString();
        String guid = ninjaData.getAsJsonObject().get("GUID").getAsString();
        long timestamp = ninjaData.getAsJsonObject().get("timestamp").getAsLong();
        
        ninjaEventListener.onNinjaEvent(data, guid, timestamp);
	}

}

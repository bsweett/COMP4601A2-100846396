package edu.carleton.COMP4601.a2.utility;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import edu.carleton.COMP4601.a2.dao.DocumentCollection;
import edu.carleton.COMP4601.a2.utility.ServiceRegistrar;

// Assumption is that this works as a singleton.
// We register the service when we create it.
public class SearchServiceManager {

	private static SearchServiceManager instance;
	
	private Timer timer;
	private ConcurrentHashMap<String, JSONObject> dirServices;
	private Logger logger;
	private String uniqueName;

	public SearchServiceManager() {
		logger = Logger.getGlobal();
		uniqueName = SDAConstants.COMP4601SDA + hashCode();
		dirServices = new ConcurrentHashMap<String, JSONObject>();
		timer = new Timer();
		timer.scheduleAtFixedRate(new AskDirectoryServer(), SDAConstants.ONE_MINUTE,
				SDAConstants.ONE_MINUTE);
	}
	
	public void stop() {
		logger.log(Level.INFO, "Stopping distributed search services...");
		timer.cancel();
	}

	public void register() throws IOException {
		ServiceRegistrar.register(uniqueName, SDAConstants.ENGINE_URL, SDAConstants.SDA,
				SDAConstants.DESCRIPTION);
	}

	public static String list() {
		return ServiceRegistrar.findType(SDAConstants.SDA);
	}
	public void unregister() {
		ServiceRegistrar.unregister(uniqueName);
	}

	public static SearchServiceManager getInstance() {
		if (instance == null) {
			instance = new SearchServiceManager();
			try {
				instance.register();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	// This is the distributed interface
	// Send off the query to all
	public SearchResult search(String tags) {
		SearchResult sr = new SearchResult(dirServices.values().size());
		for (JSONObject jsonObj : dirServices.values()) {
			String url = null;
			try {
				url = jsonObj.getString(SDAConstants.URL);
			} catch (JSONException e) {
			}
			if (url != null)
				new AsyncSearch(sr, url, tags, true).start();
			else
				// Won't finish otherwise, just timeout
				sr.countDown();
		}
		return sr;
	}

	public SearchResult query(String tags) {
		SearchResult sr = new SearchResult(dirServices.values().size());
		for (Entry<String, JSONObject> e: dirServices.entrySet()) {
			String url = null;
			try {
				url = e.getValue().getString(SDAConstants.URL);
			} catch (JSONException ex) {
			}
			if (url != null)
				new AsyncSearch(sr, url, tags, false).start();
			else
				// Won't finish otherwise, just timeout
				sr.countDown();
		}
		return sr;
	}

	// This distributes the search.
	// We don't wait for each task to finish
	private class AsyncSearch extends Thread {
		SearchResult sr;
		String tags;
		String url;
		boolean isQuery;

		AsyncSearch(SearchResult sr, String url, String tags, boolean isQuery) {
			super();
			this.sr = sr;
			this.url = url;
			this.tags = tags;
			this.isQuery = isQuery;
		}

		public void run() {
			// We handle every exception here in order to
			// ensure that we decrement the latch whatever
			// happens to the distributed search.
			try {
				WebResource service;
				Client client = Client.create(new DefaultClientConfig());
				logger.log(Level.INFO, "Searching: " + url);
				if (url.endsWith("/"))
					service = client.resource(url);
				else
					service = client.resource(url + "/");

				String serviceType;
				if (isQuery)
					serviceType = SDAConstants.QUERY;
				else
					serviceType = SDAConstants.SEARCH;

				ClientResponse r = service.path(SDAConstants.REST)
						.path(SDAConstants.SDA).path(serviceType)
						.path(tags).accept(MediaType.APPLICATION_XML)
						.get(ClientResponse.class);
				// Check to make sure that we got a reasonable response
				if (r.getStatus() < 204) {
					sr.addAll(r.getEntity(DocumentCollection.class)
							.getDocuments());
					logger.log(Level.INFO, "Count: " + sr.getDocs().size());
				}
			} catch (Exception e) {
				logger.log(Level.INFO, "Error in search: " + e);
			} finally {
				sr.countDown();
			}
		}
	}

	/*
	 * Ask the Directory server to give us entries
	 */
	private class AskDirectoryServer extends TimerTask {

		public void run() {
			logger.log(Level.INFO, "Listing Directory services...");
			dirServices.clear();
			String los = ServiceRegistrar.findType(SDAConstants.SDA);
			logger.log(Level.INFO, "LIST: "+los);
			try {
				JSONArray losArray = new JSONArray(los);
				for (int i = 0; i < losArray.length(); i++) {
					JSONObject jsonObj = losArray.getJSONObject(i);
					String name = jsonObj.getString(SDAConstants.NAME);
					if (name != null
							&& !name.equals(uniqueName))
						dirServices.put(name, jsonObj);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			/* Update my entry in the directory service */
			ServiceRegistrar.register(uniqueName, SDAConstants.ENGINE_URL, 
					SDAConstants.SDA, SDAConstants.DESCRIPTION);
		}
	}
}

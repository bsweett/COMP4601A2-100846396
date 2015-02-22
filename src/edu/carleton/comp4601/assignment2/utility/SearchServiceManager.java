package edu.carleton.comp4601.assignment2.utility;

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

import edu.carleton.comp4601.assignment2.dao.DocumentCollection;
import edu.carleton.comp4601.assignment2.utility.ServiceRegistrar;

public class SearchServiceManager {

	private static SearchServiceManager instance;

	private Timer timer;
	private ConcurrentHashMap<String, JSONObject> dirServices;
	private Logger logger;
	private String uniqueName;

	private SearchServiceManager() {
		logger = Logger.getGlobal();
		uniqueName = SDAConstants.COMP4601SDA + hashCode();
		dirServices = new ConcurrentHashMap<String, JSONObject>();
		start();
	}
	
	public void setUniqueName(String name) {
		uniqueName = name;
	}

	public void start() {
		logger.log(Level.INFO, "Starting distributed search services...");
		timer = new Timer();
		timer.scheduleAtFixedRate(new AskDirectoryServer(),
				SDAConstants.ONE_MINUTE, SDAConstants.ONE_MINUTE);
	}

	public void stop() {
		logger.log(Level.INFO, "Stopping distributed search services...");
		timer.cancel();
	}

	public void register() throws IOException {
		ServiceRegistrar.register(uniqueName, SDAConstants.ENGINE_URL,
				SDAConstants.SDA, SDAConstants.DESCRIPTION);
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
		return doTheWork(tags, SDAConstants.SEARCH);
	}

	// Local only
	public SearchResult query(String tags) {
		return doTheWork(tags, SDAConstants.QUERY);
	}

	public SearchResult doTheWork(String tags, String serviceType) {
		SearchResult sr = new SearchResult(dirServices.values().size());
		for (Entry<String, JSONObject> e : dirServices.entrySet()) {
			String url = null;
			try {
				url = e.getValue().getString(SDAConstants.URL);
			} catch (JSONException ex) {
			}
			if (url != null)
				new AsyncSearch(sr, url, tags, serviceType).start();
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
		String serviceType;

		AsyncSearch(SearchResult sr, String url, String tags, String serviceType) {
			super();
			this.sr = sr;
			this.url = url;
			this.tags = tags;
			this.serviceType = serviceType;
		}

		public void run() {
			// We handle every exception here in order to
			// ensure that we decrement the latch whatever
			// happens to the distributed search.
			WebResource service = null;
			try {
				Client client = Client.create(new DefaultClientConfig());
				logger.log(Level.INFO, "Searching: " + url);
				if (url.endsWith("/"))
					service = client.resource(url);
				else
					service = client.resource(url + "/");

				ClientResponse r = service.path(serviceType).path(tags)
						.accept(MediaType.APPLICATION_XML)
						.get(ClientResponse.class);
				// Check to make sure that we got a reasonable response
				if (r.getStatus() < 204) {
					sr.addAll(r.getEntity(DocumentCollection.class)
							.getDocuments());
					logger.log(Level.INFO, "Count: " + sr.getDocs().size());
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error in search: " + service.toString());
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
			logger.log(Level.INFO, "LIST: " + los);
			try {
				JSONArray losArray = new JSONArray(los);
				for (int i = 0; i < losArray.length(); i++) {
					JSONObject jsonObj = losArray.getJSONObject(i);
					String name = jsonObj.getString(SDAConstants.NAME);
					if (name != null && !name.equals(uniqueName))
						dirServices.put(name, jsonObj);
				}
			} catch (JSONException e) {
				logger.log(Level.SEVERE, "Directory error: " + e);
			}
			// Update my entry in the directory service
			// This just ensures that things won't expire
			ServiceRegistrar.register(uniqueName, SDAConstants.ENGINE_URL,
					SDAConstants.SDA, SDAConstants.DESCRIPTION);
		}
	}
}

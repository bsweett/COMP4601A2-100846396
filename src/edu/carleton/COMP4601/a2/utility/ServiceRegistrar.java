package edu.carleton.COMP4601.a2.utility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;

/*
 * The ServiceRegister class acts as an endpoint 
 * which hides the specific implementation of the Directory service.
 * 
 * NOTE: We do not allow names with zero length or null objects to be
 * used as keys in the directory service.
 */
public class ServiceRegistrar {
	
	/*
	 * The SERVICE_HOST should be initialized to your Directory service
	 * You could always use Bonjour to find it :-).
	 */
	private static String SERVICE_HOST = "sikaman.dyndns.org";
	/*
	 * We limit the maximum length of keys and URLs in the Directory
	 */
	private static int MAX_LEN = 1024;
	
	/*
	 * This method will register the service defined by the 4 arguments 
	 * and return a JSON representation of the entry created.
	 */
	public static String register(String name, String url, String type, String description) {
		if (name == null || name.length() == 0 || name.length() > MAX_LEN)
			return null;
		if (url == null || url.length() == 0 || url.length() > MAX_LEN)
			return null;
		if (description == null || description.length() > MAX_LEN)
			return null;
		if (type == null || type.length() > MAX_LEN)
			return null;
					
		Logger logger = Logger.getGlobal();
		logger.log(Level.INFO, "Registering with Directory service for "+name);
		Client client = Client.create(new DefaultClientConfig());
		Form form = new Form();
		form.add("name", name);
		form.add("url", url);
		form.add("type", type);
		form.add("description", description);
		URI sURL = UriBuilder.fromUri("http://"+SERVICE_HOST+":8080/Directory/rest/directory/register").build();
		WebResource service = client.resource(sURL);
		service.accept(MediaType.APPLICATION_JSON);
		ClientResponse r = service.type(MediaType.APPLICATION_FORM_URLENCODED)
		.post(ClientResponse.class, form);
		String rtn = r.getEntity(String.class);
		logger.log(Level.INFO, rtn);
		return rtn;		
	}
	
	/* 
	 * This removes an entry form the Directory service
	 */
	public static String unregister(String name) {
		if (name == null || name.length() == 0 || name.length() > MAX_LEN)
			return null;
		Logger logger = Logger.getGlobal();
		logger.log(Level.INFO, "Unregistering from Directory service for "+name);
		Client client = Client.create(new DefaultClientConfig());
		Form form = new Form();
		form.add("name", name);
		URI sURL = UriBuilder.fromUri("http://"+SERVICE_HOST+":8080/Directory/rest/directory/unregister").build();
		WebResource service = client.resource(sURL);
		service.accept(MediaType.APPLICATION_JSON);
		ClientResponse r = service.type(MediaType.APPLICATION_FORM_URLENCODED)
		.post(ClientResponse.class, form);
		String rtn = r.getEntity(String.class);
		logger.log(Level.INFO, rtn);
		return rtn;		
	}
		
	/*
	 * This retrieves a JSON-formatted version of the services
	 * known to the Directory service.
	 */
	public static String list() {
		Logger logger = Logger.getGlobal();
		logger.log(Level.INFO, "Listing Directory service ...");
		Client client = Client.create(new DefaultClientConfig());
		URI sURL = UriBuilder.fromUri("http://"+SERVICE_HOST+":8080/Directory/rest/directory/list").build();
		WebResource service = client.resource(sURL);
		service.accept(MediaType.APPLICATION_JSON);
		String rtn = service.get(String.class);
		logger.log(Level.INFO, rtn);
		return rtn;		
	}
	
	/* 
	 * This method allows a specific, named service to be retrieved
	 * from the Directory service. A JSON-formatted representation of
	 * the service is returned. The JSONObject should contain the keys:
	 * name (the service name), type (currently "GENERIC"), 
	 * url (the url of the service) and description (a friendly description 
	 * of the service). 
	 */
	public static String find(String name) {
		if (name == null || name.length() == 0 || name.length() > MAX_LEN)
			return null;
		Logger logger = Logger.getGlobal();
		logger.log(Level.INFO, "Searching Directory service ...");
		Client client = Client.create(new DefaultClientConfig());
		Form form = new Form();
		form.add("name", name);
		URI sURL = UriBuilder.fromUri("http://"+SERVICE_HOST+":8080/Directory/rest/directory/find").build();
		WebResource service = client.resource(sURL);
		service.accept(MediaType.APPLICATION_JSON);
		ClientResponse r = service.type(MediaType.APPLICATION_FORM_URLENCODED)
		.post(ClientResponse.class, form);
		String rtn = r.getEntity(String.class);
		logger.log(Level.INFO, rtn);
		return rtn;		
	}	
	
	/* 
	 * This method allows a specific, service type to be retrieved
	 * from the Directory service. A JSON-formatted representation of
	 * the service is returned. The JSONArray should contain the a JSON
	 * representation of each of the services. See find() for a description
	 * of the directory entry contents.
	 */
	public static String findType(String type) {
		if (type == null || type.length() == 0 || type.length() > MAX_LEN)
			return null;
		Logger logger = Logger.getGlobal();
		logger.log(Level.INFO, "Searching Directory service for "+type);
		Client client = Client.create(new DefaultClientConfig());
		URI sURL = UriBuilder.fromUri("http://"+SERVICE_HOST+":8080/Directory/rest/directory/"+type).build();
		WebResource service = client.resource(sURL);
		service.accept(MediaType.TEXT_PLAIN);
		ClientResponse r = service.get(ClientResponse.class);
		String rtn = r.getEntity(String.class);
		logger.log(Level.INFO, rtn);
		return rtn;		
	}
	
	/*
	 * Allows us to point to the location of the Directory.
	 * We copy the host in order to avoid anyone changing the 
	 * string after the call has been made (unless they call this
	 * method again, of course). The host string must be either
	 * a machine name, such as "sikaman.dyndns.org" or a textual
	 * representation of its IP address such as "192.168.100.1"
	 * We also check whether the host is reachable (in 2s) in order to 
	 * prevent errors when accessing the directory.
	 */
	public static String setServiceHost(String host) throws UnknownHostException, IOException {
		if (host == null || host.length() == 0 || host.length() > MAX_LEN)
			return null;
		if (!InetAddress.getByName(host).isReachable(2000))
			return null;
		return SERVICE_HOST = new String(host);
	}
}

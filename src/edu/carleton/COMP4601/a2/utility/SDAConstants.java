package edu.carleton.COMP4601.a2.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SDAConstants {
	public static final int PORT = 8080;
	public static final int TIMEOUT = 10; // Measured in seconds
	public static final String REST = "rest";
	public static final String SDA = "sda";
	public static final String URL = "url";
	public static final String NAME = "name";
	public static final String COMP4601SDA = "COMP4601SDA";
	public static final String DEFAULT = "COMP4601 Searchable Document Archive: Prof. White";
	public static final String LOCALHOST = "localhost";
	public static final String SEARCH = "search";
	public static final String QUERY = "query";
	public static final long ONE_MINUTE = 1000 * 60;
	public static final String HTTP = "http://";
	public static final String DESCRIPTION = "COMP 4601 Search Engine";
	public static String ENGINE_URL = null;

	static {
		if (ENGINE_URL == null)
			try {
				ENGINE_URL = HTTP 
						+ InetAddress.getLocalHost().getHostAddress() + ":"
						+ SDAConstants.PORT + "/" + SDAConstants.COMP4601SDA
						+ "/" + SDAConstants.REST + "/" + SDAConstants.SDA
						+ "/";
			} catch (UnknownHostException e) {
				ENGINE_URL = HTTP + SDAConstants.LOCALHOST + ":"
						+ SDAConstants.PORT + "/" + SDAConstants.COMP4601SDA
						+ "/" + SDAConstants.REST + "/" + SDAConstants.SDA
						+ "/";
			}
	}
}

package edu.carleton.COMP4601.a2.utility;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.carleton.COMP4601.a2.Main.DatabaseManager;

public class SDAContextClass implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			SearchServiceManager.getInstance().stop();
			DatabaseManager.getInstance().stopMongoClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void contextInitialized(ServletContextEvent arg0) {
		SearchServiceManager.getInstance();
		DatabaseManager.getInstance();
	}

}

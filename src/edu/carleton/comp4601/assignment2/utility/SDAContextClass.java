package edu.carleton.comp4601.assignment2.utility;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.carleton.comp4601.assignment2.database.DatabaseManager;

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
		System.out.println("CONTEXT STARTED");
		SearchServiceManager.getInstance();
		DatabaseManager.getInstance();
		PageRankManager.getInstance();
			
		Runnable myRunnable = new Runnable(){

			public void run(){
				PageRankManager.getInstance().computePageRank();
		     }
			};

		   Thread thread = new Thread(myRunnable);
		   thread.start();
		   
	}

}

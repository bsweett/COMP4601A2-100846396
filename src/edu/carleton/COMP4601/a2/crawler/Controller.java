package edu.carleton.COMP4601.a2.crawler;

import org.apache.log4j.PropertyConfigurator;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	public static CrawlController initController(CrawlConfig config, String[] domains) throws Exception {
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		  for (String domain : domains) {
		      controller.addSeed(domain);
		}

		return controller;
	}

	// TODO: Move Crawler logic into server call rather than local Java Main
	public static void main(String[] args) throws Exception {
		
		String homePath = System.getProperty("user.home");
		String crawlStorageFolder = "/data/crawl/root";
		int numberOfCrawlers = 7;
		String[] crawlDomains = new String[] { "http://sikaman.dyndns.org:8888/courses/4601/resources/", "http://www.carleton.ca", "http://props.social/" };

		PropertyConfigurator.configure("log4j.properties");
		
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(homePath + crawlStorageFolder);
		config.setMaxDepthOfCrawling(1);
		config.setPolitenessDelay(700);
		
		/*
	     * Since images are binary content, we need to set this parameter to
	     * true to make sure they are included in the crawl.
	     */
		config.setIncludeBinaryContentInCrawling(true);
		
		MyCrawler.configure(crawlDomains);
		
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		initController(config, crawlDomains).start(MyCrawler.class, numberOfCrawlers);
	}

}

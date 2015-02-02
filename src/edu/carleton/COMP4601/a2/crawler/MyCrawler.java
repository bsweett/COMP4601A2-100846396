package edu.carleton.COMP4601.a2.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.carleton.COMP4601.a2.graphing.*;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.carleton.COMP4601.a2.Main.DatabaseManager;
import edu.carleton.COMP4601.a2.tika.TikaParsingManager;

public class MyCrawler extends WebCrawler {

	private Grapher crawlGraph;
	private PageVertex root;
	private static String[] domains;

	private static final Pattern filters = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v"
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz|bmp|gif|jpe?g))$");

	//TODO: Add allow patterns (jpeg, tiff, gif, png), pdf, doc, docx, xls, xlsx, ppt and pptx
	private static final Pattern allowedPatterns = Pattern.compile(".*(\\.(pdf|png|doc|docx?))$");

	public static void configure(String[] domain) {
		domains = domain;
	}

	@Override
	public void onStart() {
		String graphName = "Crawler Graph: " + Integer.toString(this.myId);
		this.crawlGraph = new Grapher(graphName);
		
		this.root = new PageVertex(0, "Root", 0);
		this.crawlGraph.addVertex(this.root);
	}

	@Override
	public void onBeforeExit() {
		try {
			String name = this.crawlGraph.getName();
			byte[] bytes = Marshaller.serializeObject(crawlGraph);
			DatabaseManager.getInstance().addNewGraph(name, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * You should implement this function to specify whether
	 * the given url should be crawled or not (based on your
	 * crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		if (filters.matcher(href).matches()) {
			return false;
		}

		if (allowedPatterns.matcher(href).matches()) {
			return true;
		}

		for (String domain : domains) {
			if (href.startsWith(domain)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This function is called when a page is fetched and ready 
	 * to be processed by your program.
	 */
	@Override
	public void visit(Page page) {    
		Date date = new Date();
		long currentTime = date.getTime();

		String url = page.getWebURL().getURL();
		int docId = page.getWebURL().getDocid();
		
		System.out.println("URL: " + url);

		Tika tika = new Tika();
		MediaType mediaType = null;
		
		try {
			mediaType = MediaType.parse(tika.detect(new URL(url)));
		} catch (IOException e) {
			System.err.println("Exception while getting mime type: " + e.getLocalizedMessage());
		}

		if (page.getParseData() instanceof HtmlParseData) {
			parseHTMLToDocument(page, url, currentTime);

		} else if(page.getParseData() instanceof BinaryParseData && mediaType != null) {
			parseBinaryToDocument(page, mediaType, url, currentTime);
			
		}
		
		// Graphing with root node (NOTE: Untested)
		String parentUrl = page.getWebURL().getParentUrl();
		int parentId = page.getWebURL().getParentDocid();
		
		PageVertex newPage = new PageVertex(docId, url, currentTime);
		this.crawlGraph.addVertex(newPage);
		
		if(parentUrl.isEmpty()) {
			this.crawlGraph.addEdge(this.root, newPage);
		} else {
			PageVertex parentPage = new PageVertex(parentId, parentUrl, currentTime);
			this.crawlGraph.addVertex(parentPage);
			this.crawlGraph.addEdge(parentPage, newPage);
		}
	}


	// TODO: OtherDocument -> Document (what do i store where)?
	private boolean parseBinaryToDocument(Page page, MediaType mediaType, String url, long currentTime) {
		
		try {
			String type = mediaType.getSubtype();
			InputStream inputStream = new ByteArrayInputStream(page.getContentData());
			Metadata metadata = null;
			//OtherDocument doc = null;
			
			metadata = TikaParsingManager.getInstance().parseUsingAutoDetect(inputStream);
			// Build doc here
			edu.carleton.COMP4601.a2.dao.Document myDoc = new edu.carleton.COMP4601.a2.dao.Document(page.getWebURL().getDocid());
			//myDoc.set
			
			/*
			if(type.equals("png")) {
				
				metadata = TikaParsingManager.getInstance().parseMetadataForImageWithType(inputStream, "");
				//doc = buildMimeDocFromMetadata(metadata, page.getWebURL().getDocid(), url, currentTime);
				System.out.println("IMAGE: " + metadata.toString());

			} else if (type.equals("pdf")) {
				metadata = TikaParsingManager.getInstance().parseMetadataForPDF(inputStream);
				//doc = buildMimeDocFromMetadata(metadata, page.getWebURL().getDocid(), url, currentTime);
				System.out.println("PDF: " + metadata.toString());
				
			} else if (type.equals("msword") || type.equals("vnd.openxmlformats-officedocument.wordprocessingml.document")) {
				metadata = TikaParsingManager.getInstance().parseMetadataForDOC(inputStream);
				//doc = buildMimeDocFromMetadata(metadata, page.getWebURL().getDocid(), url, currentTime);
				System.out.println("WORD: " + metadata.toString());
				
			}*/
			
			// If doc was built properly add it to the DB
			/*
			if(doc != null) {
				DatabaseManager.getInstance().addNewOtherDocument(doc);
				return true;
			}*/
			
		} catch (Exception e) {
			System.err.println("Exception while parsing nonHTML: " + e.getLocalizedMessage());
		}
		
		return false;
	}
	
	/*
	private OtherDocument buildMimeDocFromMetadata(Metadata metadata, int id, String url, long time) {
		
		String type = metadata.get(Metadata.CONTENT_TYPE);
		String name = metadata.get(Metadata.TITLE);
		String author = metadata.get(Metadata.AUTHOR);
		String creator = metadata.get(Metadata.CREATOR);
		String producer = metadata.get(Metadata.PUBLISHER);
		String date = metadata.get(Metadata.CREATION_DATE);
		
		try {
			OtherDocument otherDoc = new OtherDocument();
			otherDoc.setId(id);
			otherDoc.setUrl(url);
			otherDoc.setTime(time);
			otherDoc.setType(type != null ? type : "");
			otherDoc.setName(name != null ? name : "");
			otherDoc.setAuthor(author != null ? author : "");
			otherDoc.setCreator(creator != null ? creator : "");
			otherDoc.setProducer(producer != null ? producer : "");
			
			if(date != null) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				sdf.setCalendar(cal);
				cal.setTime(sdf.parse(date));
				otherDoc.setDate(cal.getTime().getTime());
			} else {
				otherDoc.setDate(0);
			}
			
			return otherDoc;

		} catch (Exception e) {
			System.err.println("Exception while parsing nonHTML to Doc: " + e.getLocalizedMessage());
			return null;
		}
	}*/

	// TODO: Cleanup.. Soup stuff is probably needs by image's as well
	private boolean parseHTMLToDocument(Page page, String url, long currentTime) {

		try {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			//String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			int docId = page.getWebURL().getDocid();
			//List<WebURL> links = htmlParseData.getOutgoingUrls();

			//System.out.println("--FOUND HTML PAGE--");
			//System.out.println("Text length: " + text.length());
			//System.out.println("Html length: " + html.length());
			//System.out.println("Number of outgoing links: " + links.size());

			Document doc = Jsoup.parse(html);
			Elements jsoupLinks = doc.select("a[href]");
			//Elements allImages = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
			Elements allText = doc.select("p,h1,h2,h3,h4,h5");

			// Store document basic values
			//Document myDoc = new Document(docId);
			edu.carleton.COMP4601.a2.dao.Document myDoc = new edu.carleton.COMP4601.a2.dao.Document(docId);
			myDoc.setName(doc.title());
			//myDoc.setUrl(url);
			myDoc.setScore(0);
			//myDoc.setTime(currentTime);

			// Store all tag names
			for(Element elem : doc.getAllElements()) {
				String tag = elem.tagName();

				if(!tag.isEmpty())
					myDoc.addTag(tag);
			}

			// Store all links
			for(Element elem : jsoupLinks) {
				String linkHref = elem.attr("href");

				if(!linkHref.isEmpty())
					myDoc.addLink(linkHref);
			}

			/*
			// Store all image src and alt
			for(Element elem : allImages) {
				String imageSrc = elem.attr("src");
				String imageAlt = elem.attr("alt");

				
				if(!imageSrc.isEmpty())
					myDoc.addImage(imageSrc);

				if(!imageAlt.isEmpty())
					myDoc.addImage(imageAlt);
			}*/

			// Store all document text
			String rawText = "";
			for(Element elem : allText) {
				rawText += (" " + elem.text());
			}
			myDoc.setText(rawText);

			DatabaseManager.getInstance().addNewDocument(myDoc);

			return true;
		} catch (Exception e) {
			System.err.println("Exception while parsing HTML: " + e.getLocalizedMessage());
			return false;
		}
	}


}

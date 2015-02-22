package edu.carleton.comp4601.assignment2.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import edu.carleton.comp4601.assignment2.dao.Document;
import edu.carleton.comp4601.assignment2.database.DatabaseManager;
import edu.carleton.comp4601.assignment2.index.CrawlIndexer;
import edu.carleton.comp4601.assignment2.index.SearchEngine;
import edu.carleton.comp4601.assignment2.utility.PageRankManager;
import edu.carleton.comp4601.assignment2.utility.SDAConstants;
import edu.carleton.comp4601.assignment2.utility.SearchResult;
import edu.carleton.comp4601.assignment2.utility.SearchServiceManager;
import edu.carleton.comp4601.assignment2.utility.Tuple;

@Path("/sda")
public class SDA {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	final private String PATH = "http://localhost:8080/COMP4601SDA/rest/sda";
	final String homePath = System.getProperty("user.home");
	final String luceneIndexFolder = "/data/lucene/";
	
	private String name;

	public SDA() {
		name = "COMP4601 Searchable Document Archive V2: Benjamin Sweett and Brayden Girard";
	}

	// Gets the SDA name as a String
	@GET
	public String printName() {
		return name;
	}

	// Gets the SDA name as XML
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String sayXML() {
		return "<?xml version=\"1.0\"?>" + "<sda> " + name + " </sda>";
	}

	// Gets the SDA name as HTML
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtml() {
		return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
				+ "</body></h1>" + "</html> ";
	}

	// Gets all documents as XML
	@GET
	@Path("documents")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public ArrayList<Document> getDocumentsXML() {
		ArrayList<Document> documents = DatabaseManager.getInstance().getDocuments();

		if(documents == null) {
			throw new RuntimeException("No documents exist");
		}

		return documents;
	}

	// Gets all the documents as HTML
	@GET
	@Path("documents")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_XML)
	public String getDocumentsHTML() {
		ArrayList<Document> documents = DatabaseManager.getInstance().getDocuments();

		if(documents == null) {
			return get404();
		}

		return documentsToHTML(documents);
	}
/*
	// Gets all documents with the given tag string as XML
	@GET
	@Path("search/{tags}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public ArrayList<Document> getDocumentsByTagXML(@PathParam("tags") String tags) {
		ArrayList<Document> documents = DatabaseManager.getInstance().findDocumentsByTag(splitTags(tags));

		if(documents == null) {
			throw new RuntimeException("No documents exist");
		}

		return documents;
	}

	// Gets all documents with the given tag string as HTML
	@GET
	@Path("search/{tags}")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_XML)
	public String getDocumentsByTagHTML(@PathParam("tags") String tags) {
		ArrayList<Document> documents = DatabaseManager.getInstance().findDocumentsByTag(splitTags(tags));

		if(documents == null) {
			return get204();
		}

		return documentsToHTML(documents);
	}
*/
	// Deletes a document with a given tag string and returns a HTTP code
	@GET
	@Path("delete/{tags}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response deleteDocumentsByTagXML(@PathParam("tags") String tags) {
		Response res;
		ArrayList<Document> documents = DatabaseManager.getInstance().findDocumentsByTag(splitTags(tags));

		if(documents == null) {
			return Response.noContent().build();
		}

		res = Response.noContent().build();

		for(Document d : documents) {
			if (DatabaseManager.getInstance().removeDocument(d.getId()) != null) {
				res = Response.ok().build();
			}
		}

		return res;
	}

	// Gets a document by ID as XML
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Document getDocumentXML(@PathParam("id") String id) {
		try{
			Document d = DatabaseManager.getInstance().findDocument(Integer.parseInt(id));
			if (d == null) {
				throw new RuntimeException("No such Document: " + id);
			}
			return d;
		} catch (Exception e) {
			throw new RuntimeException("Server error: " + id);
		}

	}

	// Gets a document by ID as HTML
	@GET
	@Path("{id}")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_XML)
	public String getDocumentHTML(@PathParam("id") String id, @Context HttpServletResponse servletResponse) throws IOException {

		try{
			Document d = DatabaseManager.getInstance().findDocument(Integer.parseInt(id));
			if (d == null) {
				return get204();
			}

			return documentToHTML(d);

		} catch (Exception e) {
			return get406();
		}
	}

	// Deletes a document by ID and returns an HTTP code
	@DELETE
	@Path("{id}")
	public Response deleteAccount(@PathParam("id") String id) {
		Response res;

		if (DatabaseManager.getInstance().removeDocument(Integer.parseInt(id)) == null) {
			res = Response.noContent().build();
		}
		else {
			res = Response.ok().build();
		}

		return res;
	}

	// Creates a new document from the given XML and returns an HTTP code
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response createDocument(JAXBElement<Document> doc) {
		Response res;
		Document document = doc.getValue();
		document.setId(DatabaseManager.getInstance().getNextIndex());

		if(DatabaseManager.getInstance().addNewDocument(document)) {
			//INDEX DOCUMENT WITH LUCENE AND ADD BOOST VALUE OF 2 18.6
			CrawlIndexer indexer = new CrawlIndexer(homePath + luceneIndexFolder, document);
			try {
				indexer.indexHTMLDocument();
				res = Response.ok().build();
			} catch (IOException e) {
				e.printStackTrace();
				res = Response.serverError().build();
			}	
		}
		else {
			res = Response.noContent().build();
		}

		return res;
	}

	// Updates a document by ID with the given document from XML and returns an HTTP Code
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateDocument(@PathParam("id") String id, JAXBElement<Document> doc) {
		Response res;
		Document updatedDocument = doc.getValue();

		try{
			Document existingDocument = DatabaseManager.getInstance().findDocument(Integer.parseInt(id));
			if(existingDocument == null) {
				res = Response.noContent().build();
			}
			else {
				/*  These values are not send from the client 
				 *  (assignment only states updating tags and links) 
				 */
				updatedDocument.setId(existingDocument.getId());
				updatedDocument.setName(existingDocument.getName());
				updatedDocument.setText(existingDocument.getText());
				updatedDocument.setScore(existingDocument.getScore());

				if(DatabaseManager.getInstance().updateDocument(updatedDocument, existingDocument)) {
					//DELETE OLD LUCENE DOCUMENT AND INSERT NEW DOCUMENT 18.7
					CrawlIndexer indexer = new CrawlIndexer(homePath + luceneIndexFolder, updatedDocument);
					try {
						indexer.indexHTMLDocument();
						res = Response.ok().build();
					} catch (IOException e) {
						e.printStackTrace();
						res = Response.serverError().build();
					}	
				}
				else {
					res = Response.noContent().build();
				}
			}
		} catch (Exception e) {
			res = Response.notAcceptable(null).build();
		}

		return res;
	}

	// Takes a document object and returns an HTML Mark of its contents
	private String documentToHTML(Document d) {
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html>");
		htmlBuilder.append("<head><title>" + d.getName() + "</title></head>");
		htmlBuilder.append("<body><h1>" + d.getName() + "</h1>");
		htmlBuilder.append("<p>" + d.getText() + "</p>");
		htmlBuilder.append("<h1> Links </h1>");
		htmlBuilder.append("<ul>");
		for (String s : d.getLinks())
		{
			htmlBuilder.append("<li>");
			htmlBuilder.append("<a href=\"" + PATH);
			htmlBuilder.append(s);
			htmlBuilder.append("\">");
			htmlBuilder.append(PATH + s);
			htmlBuilder.append("</a>");
			htmlBuilder.append("</li>");
		}
		htmlBuilder.append("</ul>");
		htmlBuilder.append("<h1> Tags </h1>");
		htmlBuilder.append("<ul>");
		for (String s : d.getTags())
		{
			htmlBuilder.append("<li>");
			htmlBuilder.append(s);
			htmlBuilder.append("</li>");
		}
		htmlBuilder.append("<h1>" + d.getScore() + "</h1>");
		htmlBuilder.append("</ul></body>");
		htmlBuilder.append("</html>");

		return htmlBuilder.toString();
	}

	// Takes a document list and returns HTML markup for all its contents
	private String documentsToHTML(ArrayList<Document> documents) {
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html>");
		htmlBuilder.append("<head><title> All Documents </title></head>");
		htmlBuilder.append("<body>");
		for(Document d : documents) {
			htmlBuilder.append("<h1>" + d.getName() + "</h1>");
			htmlBuilder.append("<p>" + d.getText() + "</p>");
			htmlBuilder.append("<h1> Links </h1>");
			htmlBuilder.append("<ul>");
			for (String s : d.getLinks())
			{
				htmlBuilder.append("<li>");
				htmlBuilder.append("<a href=\"" + PATH);
				htmlBuilder.append(s);
				htmlBuilder.append("\">");
				htmlBuilder.append(PATH + s);
				htmlBuilder.append("</a>");
				htmlBuilder.append("</li>");
			}
			htmlBuilder.append("</ul>");
			htmlBuilder.append("<h1> Tags </h1>");
			htmlBuilder.append("<ul>");
			for (String s : d.getTags())
			{
				htmlBuilder.append("<li>");
				htmlBuilder.append(s);
				htmlBuilder.append("</li>");
			}
			htmlBuilder.append("</ul>");
			htmlBuilder.append("<h1>" + d.getScore() + "</h1>");
		}
		htmlBuilder.append("</body>");
		htmlBuilder.append("</html>");

		return htmlBuilder.toString();
	}

	// Splits a tag string by ':' and puts results into an array of strings
	private ArrayList<String> splitTags(String tags) {
		String[] tagArray = tags.split(":");
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(tagArray));
		return list;
	}

	//Link not found HTML
	private String get404() {
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<head><title>404</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">"
				+ "<script type=\"application/x-javascript\"> addEventListener(\"load\", function() { setTimeout(hideURLbar, 0); }, false); function "
				+ "hideURLbar(){ window.scrollTo(0,1); } </script><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
				+ "<link href='http://fonts.googleapis.com/css?family=Metal+Mania' rel='stylesheet' type='text/css'><style type=\"text/css\">body{font-family: "
				+ "'Metal Mania', cursive;}body{background:skyblue;}.wrap{width:100%;margin-top:60px;}.logo h1{font-size:140px;color:yellow;text-align:center;"
				+ "margin:40px 0 0 0;text-shadow:1px 1px 6px #555;}.logo p{color:white;font-size:15px;margin-top:1px;text-align:center;}.logo p span{color:lightgreen;}"
				+ ".sub a{color:yellow;background:#06afd8;text-decoration:none;padding:5px;font-size:12px;font-family: arial, serif;font-weight:bold;}.footer{color:white;"
				+ "position:absolute;right:10px;bottom:1px;}.footer a{color:yellow;}</style></head><body><div class=\"wrap\"><div class=\"logo\"><h1>404</h1>"
				+ "<p>Sorry document is dead - Document not found</p></div></div><div class=\"footer\">"
				+ "Design by-<a href=\"http://w3layouts.com\">W3Layouts</a></div></body>");
		return htmlBuilder.toString();
	}
	
	private String htmlResponse(String title, String text) {
		return "<html> " + "<title>" + title + "</title>" + "<body><p>" + text + "</p></body>" + "</html> "; 
	}

	//Server error HTML
	@SuppressWarnings("unused")
	private String get500() {
		return "<html> " + "<title>" + "500" + "</title>" + "<body><h1>" + "Server Error - 500" + "</h1></body>" + "</html> ";
	}

	//Document not found HTML
	private String get204() {
		return "<html> " + "<title>" + "204" + "</title>" + "<body><h1>" + "Document not found - 204" + "</h1></body>" + "</html> ";
	}

	//Invalid Arguments HTML
	private String get406() {
		return "<html> " + "<title>" + "406" + "</title>" + "<body><h1>" + "Bad Request - 406" + "</h1></body>" + "</html> ";
	}

	//Link not found XML
	@SuppressWarnings("unused")
	private String linkNotFound() {
		return "<?xml version=\"1.0\"?>" + "<code> " + "404" + " </code>" + "<status> " + "Link not found" + " </status>";
	}

	//Server error XML
	@SuppressWarnings("unused")
	private String serverError() {
		return "<?xml version=\"1.0\"?>" + "<code> " + "500" + " </code>" + "<status> " + "Server Error" + " </status>";
	}

	//Document not found XML
	@SuppressWarnings("unused")
	private String documentNotFound() {
		return "<?xml version=\"1.0\"?>" + "<code> " + "204" + " </code>" + "<status> " + "Document not found" + " </status>";
	}

	//Invalid Arguments XML
	@SuppressWarnings("unused")
	private String badRequest() {
		return "<?xml version=\"1.0\"?>" + "<code> " + "406" + " </code>" + "<status> " + "Bad Request" + " </status>";
	}
	
	//18.1 Reset document archive
	@GET
	@Path("reset")
	@Consumes(MediaType.APPLICATION_XML)
	public Response resetDocuments(){
		Response res;
		return Response.ok().build();
	}
	
	//18.2 List the discovered search services
	@GET
	@Path("list")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_HTML)
	public String listServices(){
		String list = SearchServiceManager.list();
		return htmlResponse("Discovered Search Services", list); 
	}
	
	//18.3 Get page rank score for all documents
	@GET
	@Path("pagerank")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_HTML)
	public String getPageRankHTML() {
		Tuple<ArrayList<String>, ArrayList<Float>> pageRanks = PageRankManager.getInstance().computePageRank();

		if(pageRanks == null) {
			return get204();
		}

		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html>");
		htmlBuilder.append("<head><title> Document Page Ranks </title></head>");
		htmlBuilder.append("<body>");
		htmlBuilder.append("<table style=\"width:100%\">");	
		for(int i=0; i<pageRanks.x.size(); i++) {
			htmlBuilder.append("<tr>");
			htmlBuilder.append("<td>" + pageRanks.x.get(i) + "</td>");
			htmlBuilder.append("<td>" + pageRanks.y.get(i) + "</td>");
			htmlBuilder.append("</tr>");
		}
		htmlBuilder.append("</table>");
		htmlBuilder.append("</body>");
		htmlBuilder.append("</html>");

		return htmlBuilder.toString();
	}
	
	//18.4 Boost document relevance
	@GET
	@Path("boost")
	@Consumes(MediaType.APPLICATION_XML)
	public Response boostDocuments(){
		Response res;
		
		CrawlIndexer indexer = new CrawlIndexer(homePath + luceneIndexFolder);
		try {
			indexer.applyBoost();
			res = Response.ok().build();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = Response.serverError().build();
		}
		return res;
	}
	
	//18.5 Boost document relevance
	@GET
	@Path("noboost")
	@Consumes(MediaType.APPLICATION_XML)
	public Response noBoostDocuments(){
		Response res;

		CrawlIndexer indexer = new CrawlIndexer(homePath + luceneIndexFolder);
		try {
			indexer.removeBoost();
			res = Response.ok().build();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = Response.serverError().build();
			System.out.println("In catch of no boost");
		}
		System.out.println("Success no boost complete with response of: " + res.getStatus());
		return res;

	}
	
	//18.8 Query documents with specific terms
	//DISTIBUTED SEARCH
	@GET
	@Path("search/{terms}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_HTML)
	public String searchDistributedHTML(@PathParam("terms") String terms) {
		
		if(terms == null || terms.isEmpty()) {
			return htmlResponse("Error", "No search term provided by client");
		}
		
		//SearchResult result = SearchServiceManager.getInstance().search(terms);

		SearchResult result = SearchServiceManager.getInstance().search(terms);
		
		try {
			result.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//ArrayList<Document> docs = result.getDocs();
		//System.out.println(docs.size());
		//return documentsToHTML(result.getDocs());
		
		return "nothing";
	}
	
	@GET
	@Path("search/{terms}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public ArrayList<Document> searchDistributedXML(@PathParam("terms") String terms){
		
		if(terms == null || terms.isEmpty()) {
			throw new RuntimeException("No search term provided by client");
		}
		
		SearchResult result = SearchServiceManager.getInstance().search(terms);
		
		/*
		try {
			result.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		ArrayList<Document> documents = result.getDocs();
		if(documents == null || documents.isEmpty()) {
			return new ArrayList<Document>();
		}
		
		//return new ArrayList<Document>();
		return result.getDocs();
	}
	
	//LOCAL DOCUMENT SEARCH
	@GET
	@Path("query/{terms}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_HTML)
	public String searchLocalHTML(@PathParam("terms") String terms) {
		try {
			SearchEngine searchEngine = new SearchEngine(homePath + luceneIndexFolder);
			TopDocs topDocs = searchEngine.performSearch(terms, 100000);
			System.out.println("Hello");
			if(topDocs.totalHits == 0) {
				return htmlResponse("Error", "No documents found for terms provided");
			}

	        ArrayList<Document> documents = getDocumentsFromHits(topDocs.scoreDocs, searchEngine);
	        
	        return documentsToHTML(documents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return htmlResponse("Error", "General exception when trying to search");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return htmlResponse("Error", "Could not parse terms");
		}
	}
	
	@GET
	@Path("query/{terms}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public ArrayList<Document> searchLocalXML(@PathParam("terms") String terms){
		try {
			SearchEngine searchEngine = new SearchEngine(homePath + luceneIndexFolder);
			TopDocs topDocs = searchEngine.performSearch(terms, 100000);
			
			if(topDocs.totalHits == 0) {
				throw new RuntimeException("No such Document for terms provided");
			}

	        ArrayList<Document> documents = getDocumentsFromHits(topDocs.scoreDocs, searchEngine);
	        
	        return documents;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Server Error: Error with search engine");
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("Problem parsing terms in search engine");	
		}
	}
	
	public ArrayList<Document> getDocumentsFromHits(ScoreDoc[] hits, SearchEngine searchEngine) throws IOException {
		ArrayList<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < hits.length; i++) {
            org.apache.lucene.document.Document doc = searchEngine.getDocument(hits[i].doc);
            Document document = new Document(Integer.parseInt(doc.get("docId")));
      
            if(doc.get("docName") != null) {
            	document.setName(doc.get("docName"));
            } else {
            	document.setName("");
            }
            
            if(doc.getValues("docLink") != null) {
            	List<String> links = Arrays.asList(doc.getValues("docLink"));
            	for (String l : links) {
            		document.addLink(l);
            	}
            } else {
            	document.setLinks(new ArrayList<String>());
            }
            
            if(doc.getValues("docTag") != null) {
            	List<String> tags = Arrays.asList(doc.getValues("docTag"));
            	for (String t : tags) {
            		document.addTag(t);
            	} 
            } else {
            	document.setTags(new ArrayList<String>());
            }
            
            if(doc.get("docText") != null) {
            	document.setText(doc.get("docText"));
            } else{
            	document.setText("");
            }
            
            document.setScore(hits[i].score);
            documents.add(document);
        }
		return documents;
	}
}

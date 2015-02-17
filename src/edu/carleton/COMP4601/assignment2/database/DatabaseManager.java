package edu.carleton.comp4601.assignment2.database;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

import edu.carleton.comp4601.assignment2.dao.Document;

public class DatabaseManager {

	// DB getter
	public DB getDatabase() {
		return db;
	}

	// DB setter
	public void setDatabase(DB db) {
		this.db = db;
	}

	// Singleton setter
	public static void setInstance(DatabaseManager instance) {
		DatabaseManager.instance = instance;
	}

	private DB db;
	private MongoClient mongoClient;
	private static DatabaseManager instance;
	private final String DOCUMENTS = "documents";
	private final String GRAPH = "graph";

	// Constructor (only called once)
	public DatabaseManager() {

		try {
			this.mongoClient = new MongoClient( "localhost" );
			setDatabase(this.mongoClient.getDB( "comp4601A2" ));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}
	
	public boolean addNewGraph(String name, byte[] graph) {
		try {
			DBCollection col = db.getCollection(GRAPH);
			BasicDBObject obj = new BasicDBObject();
			obj.put("name", name);
			obj.put("bytes", graph);
			col.insert(obj);
		} catch (MongoException e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
	
	public byte[] getGraphData(String name) {

		try {
			BasicDBObject query = new BasicDBObject("name", name);
			DBCollection col = db.getCollection(GRAPH);
			DBObject result = col.findOne(query);

			if(result != null) {
				Map<?, ?> graphMap = result.toMap();
				byte[] bytes = (byte[]) graphMap.get("bytes");				
				return bytes;
			}

			return null;
		} catch (MongoException e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return null;
		}

	}

	// Adds a new document to the database
	public boolean addNewDocument(Document document) {

		try {
			DBCollection col = db.getCollection(DOCUMENTS);
			col.insert(buildDBObject(document));

		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return false;
		}

		return true; 	
	}

	// Updates an existing document with a new document in the database
	public boolean updateDocument(Document newDocument, Document oldDocument) {

		try {
			DBCollection col = db.getCollection(DOCUMENTS);
			col.update(buildDBObject(oldDocument), buildDBObject(newDocument));

		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return false;
		}

		return true; 
	}

	// Removes an existing document by ID from the database
	public Document removeDocument(Integer id) {	

		try {
			BasicDBObject query = new BasicDBObject("id", id);
			DBCollection col = db.getCollection(DOCUMENTS);
			DBObject result = col.findAndRemove(query);
			Document doc = new Document(result.toMap());
			return doc;

		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return null;
		}
	}

	// Finds a document by ID in from the database
	public Document findDocument(Integer id) {

		try {

			BasicDBObject query = new BasicDBObject("id", id);
			DBCollection col = db.getCollection(DOCUMENTS);
			DBObject result = col.findOne(query);

			if(result != null) {
				return new Document(result.toMap());
			}

			return null;
		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return null;
		}

	}

	// Finds a list of documents by a list of tags from the database
	public ArrayList<Document> findDocumentsByTag(ArrayList<String> tags) {

		try {
			ArrayList<Document> documents = new ArrayList<Document>();
			DBCollection col = db.getCollection(DOCUMENTS);
			BasicDBObject multipleTagsQuery = new BasicDBObject("tags", new BasicDBObject("$all", tags));

			DBCursor cursor = col.find(multipleTagsQuery);
			if(cursor.hasNext()) {
				while (cursor.hasNext()) {
					documents.add(new Document(cursor.next().toMap()));
				}
				return documents;
			}
			else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return null;
		}
	}

	// Gets a list of all the documents from the database
	public ArrayList<Document> getDocuments() {

		try {
			DBCollection col = db.getCollection(DOCUMENTS);
			DBCursor result = col.find();

			if(result != null) {
				ArrayList<Document> documents = new ArrayList<Document>();
				while(result.hasNext()) {
					documents.add(new Document(result.next().toMap()));
				}
				return documents;
			}

			return null;
		} catch (Exception e) {
			System.out.println("MongoException: " + e.getLocalizedMessage());
			return null;
		}

	}

	// Gets the document collection size
	public int getDocumentCollectionSize() {
		DBCollection col = db.getCollection(DOCUMENTS);
		return (int) col.getCount();
	}

	// Builds a DBObject from a give document
	public BasicDBObject buildDBObject(Document document) {

		BasicDBObject newObj = new BasicDBObject();
		newObj.put("id", document.getId());
		newObj.put("name", document.getName());
		newObj.put("score", document.getScore());
		newObj.put("text", document.getText());
		newObj.put("tags", document.getTags());
		newObj.put("links", document.getLinks());
		return newObj;

	}

	// Gets this singleton instance
	public static DatabaseManager getInstance() {

		if (instance == null)
			instance = new DatabaseManager();
		return instance;

	}

	// Stops the MongoDB Client
	public void stopMongoClient() {

		if(this.mongoClient != null) {
			this.mongoClient.close();
		}

	}

	// Gets the next index from the document collection
	public int getNextIndex() {
		int id = getDocumentCollectionSize() + 1;
		Document document = findDocument(id);
		while(document != null) {
			id++;
			document = findDocument(id);
		}
		return id;
	}
}

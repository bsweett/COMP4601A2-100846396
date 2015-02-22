package edu.carleton.comp4601.assignment2.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.carleton.comp4601.assignment2.utility.PageRankManager;

public class CrawlIndexer {

	final static Logger logger = LoggerFactory.getLogger(CrawlIndexer.class);
	
	private long count;
	private String dirPath;
	
	private edu.carleton.comp4601.assignment2.dao.Document document;
	
	/** Creates a new instance of Indexer */
	public CrawlIndexer(String dirPath, edu.carleton.comp4601.assignment2.dao.Document document) {
		this.count = 0;
		this.dirPath = dirPath;
		this.document = document;
	}
	
	/** Creates a new instance of Indexer */
	public CrawlIndexer(String dirPath) {
		this.count = 0;
		this.dirPath = dirPath;
		this.document = null;
	}

	private IndexWriter indexWriter = null;

	/**
	 * 
	 * @param create
	 * @return
	 * @throws IOException
	 */
	private IndexWriter getIndexWriter(boolean create) throws IOException {
		if (indexWriter == null) {
			Directory indexDir = FSDirectory.open(new File( this.dirPath + "index-directory" ));
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, new StandardAnalyzer());
			indexWriter = new IndexWriter(indexDir, config);
		}
		return indexWriter;
	}    

	/**
	 * 
	 * @throws IOException
	 */
	private void closeIndexWriter() throws IOException {
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	/**
	 * 
	 * @param document
	 * @param imageAlts
	 * @param data
	 * @throws IOException
	 */
	public void indexHTMLDocument() throws IOException {
		
		logger.info("Indexing Document: " + this.count);
		IndexWriter writer = getIndexWriter(false);
		Document doc = new Document();
		
		doc.add(new TextField("docId", document.getId().toString(), Field.Store.YES));
		
		String name = document.getName();
		String text = document.getText();
		
		if(name != null) {
			doc.add(new StringField("docName", document.getName(), Field.Store.YES));
		}
		
		if(text != null) {
			doc.add(new StringField("docText", document.getText(), Field.Store.YES));
		}
		
		for (String tag : document.getTags()) {
			Field field = new StringField("docTag", tag, Field.Store.YES);
			field.setBoost(2);
			doc.add(field);
		}
		
		for (String link : document.getLinks()) {
			doc.add(new StringField("docLink", link, Field.Store.YES));
		}
		
		Date date = new Date();
		doc.add(new LongField("date", date.getTime(), Field.Store.YES));
		
		
		doc.add(new StringField("mimeType", "text/html", Field.Store.YES));
			
	
		
		String contents = document.getName() + " " + document.getText();
		doc.add(new TextField("contents", contents, Field.Store.YES));	
		doc.add(new TextField("i", "ben", Field.Store.YES));	
		
		writer.addDocument(doc);
		this.count++;
		
		this.closeIndexWriter();
	}  
	
	/**
	 * 
	 * @param document
	 * @param imageAlts
	 * @param data
	 * @throws IOException
	 */
	public void indexHTMLDocumentWithBoost(float boost) throws IOException {
		
		logger.info("Indexing Document: " + this.count);
		IndexWriter writer = getIndexWriter(false);
		Document doc = new Document();
		
		doc.add(new TextField("docId", document.getId().toString(), Field.Store.YES));
		
		String name = document.getName();
		String text = document.getText();
		
		if(name != null) {
			Field field = new StringField("docName", document.getName(), Field.Store.YES);
			field.setBoost(boost);
			doc.add(field);
		}
		
		if(text != null) {
			Field field = new StringField("docText", document.getText(), Field.Store.YES);
			field.setBoost(boost);
			doc.add(field);
		}
		int firstTag = 0;
		for (String tag : document.getTags()) {
			if(firstTag==0) {
				Field field = new StringField("docTag", tag, Field.Store.YES);
				field.setBoost(boost);
				doc.add(field);
				firstTag++;
			}
			else {
				break;
			}
		}
		int firstLink = 0;
		for (String link : document.getLinks()) {
			if(firstLink==0) {
				Field field = new StringField("docLink", link, Field.Store.YES);
				field.setBoost(boost);
				doc.add(field);
				firstLink++;
			}
			else {
				break;
			}
		}
		
		Date date = new Date();
		Field field = new LongField("date", date.getTime(), Field.Store.YES);
		field.setBoost(boost);
		doc.add(field);
		
		field = new StringField("mimeType", "text/html", Field.Store.YES);
		field.setBoost(boost);
		doc.add(field);
			
		String contents = document.getName() + " " + document.getText();
		field = new TextField("contents", contents, Field.Store.YES);
		field.setBoost(boost);
		doc.add(field);
		
		field = new TextField("i", "ben", Field.Store.YES);
		field.setBoost(boost);
		doc.add(field);
		
		writer.addDocument(doc);
		this.count++;
		
		this.closeIndexWriter();
	}  
	
	public void updateHtmlDocument() throws IOException {
		logger.info("Updating Document: " + this.count);
		IndexWriter writer = getIndexWriter(false);
		
		Document doc = new Document();
		
		doc.add(new TextField("docId", document.getId().toString(), Field.Store.YES));
		
		String name = document.getName();
		String text = document.getText();
		
		if(name != null) {
			doc.add(new StringField("docName", document.getName(), Field.Store.YES));
		}
		
		if(text != null) {
			doc.add(new StringField("docText", document.getText(), Field.Store.YES));
		}
		
		for (String tag : document.getTags()) {
			Field field = new StringField("docTag", tag, Field.Store.YES);
			field.setBoost(2);
			doc.add(field);
		}
		
		for (String link : document.getLinks()) {
			doc.add(new StringField("docLink", link, Field.Store.YES));
		}
		
		Date date = new Date();
		doc.add(new LongField("date", date.getTime(), Field.Store.YES));
		
		
		doc.add(new StringField("mimeType", "text/html", Field.Store.YES));
			
	
		
		String contents = document.getName() + " " + document.getText();
		doc.add(new TextField("contents", contents, Field.Store.YES));	
		doc.add(new TextField("i", "ben", Field.Store.YES));	
		
		Term term = new Term("docId", Integer.toString(document.getId()));
		writer.updateDocument(term, doc);
		
		this.count++;
		
		this.closeIndexWriter();
	}
	
	public void applyBoost() throws IOException, ParseException {
		SearchEngine searchEngine = new SearchEngine(dirPath);
		TopDocs topDocs = searchEngine.performSearch("*", 1000000);
		getIndexWriter(true);
		ArrayList<edu.carleton.comp4601.assignment2.dao.Document> documents = getDocumentsFromHits(topDocs.scoreDocs, searchEngine);
		
		for(edu.carleton.comp4601.assignment2.dao.Document d: documents) {
			this.document = d;
			this.indexHTMLDocumentWithBoost(PageRankManager.getInstance().getDocumentPageRank(d.getId()));
		}
				
	}
	
	public void removeBoost() throws IOException, ParseException {
		SearchEngine searchEngine = new SearchEngine(dirPath);
		TopDocs topDocs = searchEngine.performSearch("*", 1000000);
		getIndexWriter(true);
		ArrayList<edu.carleton.comp4601.assignment2.dao.Document> documents = getDocumentsFromHits(topDocs.scoreDocs, searchEngine);
		
		for(edu.carleton.comp4601.assignment2.dao.Document d: documents) {
			this.document = d;
			this.indexHTMLDocumentWithBoost(1);
		}
				
	}
	
	public ArrayList<edu.carleton.comp4601.assignment2.dao.Document> getDocumentsFromHits(ScoreDoc[] hits, SearchEngine searchEngine) throws IOException {
		ArrayList<edu.carleton.comp4601.assignment2.dao.Document> documents = new ArrayList<edu.carleton.comp4601.assignment2.dao.Document>();
		for (int i = 0; i < hits.length; i++) {
            org.apache.lucene.document.Document doc = searchEngine.getDocument(hits[i].doc);
            edu.carleton.comp4601.assignment2.dao.Document document = new edu.carleton.comp4601.assignment2.dao.Document(Integer.parseInt(doc.get("docId")));
      
            if(doc.get("docName") != null) {
            	document.setName(doc.get("docName"));
            }
            else {
            	document.setName("");
            }
            if(doc.getValues("docLink") != null) {
            	document.setLinks((ArrayList<String>) Arrays.asList(doc.getValues("docLink")));
            }
            else {
            	document.setLinks(new ArrayList<String>());
            }
            if(doc.getValues("docTag") != null) {
            	document.setTags((ArrayList<String>) Arrays.asList(doc.getValues("docTag")));
            }
            else {
            	document.setTags(new ArrayList<String>());
            }
            if(doc.get("docText") != null) {
            	document.setText(doc.get("docText"));
            }
            else{
            	document.setText("");
            }
            document.setScore(hits[i].score);
            documents.add(document);
        }
		return documents;
	}
}

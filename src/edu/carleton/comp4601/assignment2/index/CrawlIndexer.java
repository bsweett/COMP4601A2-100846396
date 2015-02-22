package edu.carleton.comp4601.assignment2.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			doc.add(new StringField("docTag", tag, Field.Store.YES));
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
			doc.add(new StringField("docTag", tag, Field.Store.YES));
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
}

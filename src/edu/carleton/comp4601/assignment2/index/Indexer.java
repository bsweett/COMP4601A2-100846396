package edu.carleton.comp4601.assignment2.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.carleton.comp4601.assignment2.database.DatabaseManager;

public class Indexer {

	private int count;
	
	/** Creates a new instance of Indexer */
	public Indexer() {
		this.count = 0;
	}

	private IndexWriter indexWriter = null;

	public IndexWriter getIndexWriter(boolean create) throws IOException {
		if (indexWriter == null) {
			Directory indexDir = FSDirectory.open(new File("index-directory"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, new StandardAnalyzer());
			indexWriter = new IndexWriter(indexDir, config);
		}
		return indexWriter;
	}    

	public void closeIndexWriter() throws IOException {
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	public void indexHTMLDocument(edu.carleton.comp4601.assignment2.dao.Document myDocument) throws IOException {
		
		System.out.println("Indexing HTMLDocument: " + myDocument + " " + this.count);
		IndexWriter writer = getIndexWriter(false);
		Document doc = new Document();
		doc.add(new IntField("id", myDocument.getId(), Field.Store.YES));
		//doc.add(new LongField("time", myDocument.getTime(), Field.Store.YES));
		doc.add(new StringField("text", myDocument.getText(), Field.Store.YES));
		//doc.add(new StringField("url", myDocument.getUrl(), Field.Store.YES));
		doc.add(new StringField("name", myDocument.getName(), Field.Store.YES));
		
		String fullSearchableText = myDocument.getId() + " " + myDocument.getName() + " " + myDocument.getText();
		doc.add(new TextField("content", fullSearchableText, Field.Store.NO));
		writer.addDocument(doc);
		this.count++;
	}   

	public void rebuildIndexes() throws IOException {
		//
		// Erase existing index
		//
		getIndexWriter(true);
		//
		// Index all Accommodation entries
		//
		ArrayList<edu.carleton.comp4601.assignment2.dao.Document> docs = DatabaseManager.getInstance().getDocuments();
		for(edu.carleton.comp4601.assignment2.dao.Document doc : docs) {
			indexHTMLDocument(doc);              
		}
		//
		// Don't forget to close the index writer when done
		//
		closeIndexWriter();
	}    
}

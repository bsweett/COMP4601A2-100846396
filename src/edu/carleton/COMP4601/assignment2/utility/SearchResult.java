package edu.carleton.comp4601.assignment2.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.carleton.comp4601.assignment2.dao.Document;

public class SearchResult {
	CountDownLatch latch;
	ArrayList<Document> docs;
	
	SearchResult(int size) {
		latch = new CountDownLatch(size);
		docs = new ArrayList<Document>();
	}

	public void countDown() {
		latch.countDown();
	}
	
	public void await(long timeout, TimeUnit unit) throws InterruptedException {
		latch.await(timeout, unit);
	}
	
	public synchronized ArrayList<Document> getDocs() {
		return docs;
	}
	
	public synchronized void addAll(List<Document> documents) {
		docs.addAll(documents);
	}
}

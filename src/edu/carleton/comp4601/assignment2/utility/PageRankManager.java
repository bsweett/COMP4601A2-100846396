package edu.carleton.comp4601.assignment2.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import edu.carleton.comp4601.assignment2.dao.Document;
import edu.carleton.comp4601.assignment2.database.DatabaseManager;
import edu.carleton.comp4601.assignment2.graphing.Grapher;
import edu.carleton.comp4601.assignment2.graphing.PageVertex;
import Jama.Matrix;

public class PageRankManager {
	
	Grapher graph;
	Matrix matrix;
	double a = 0.1;
	HashMap<Integer, Float> pageRanks = new HashMap<Integer, Float>();
	private boolean rankComplete = false;
	private static PageRankManager instance;
	Set<PageVertex> currentVertices;
	
	// Singleton setter
	public static void setInstance(PageRankManager instance) {
		PageRankManager.instance = instance;
	}
	

	// Gets this singleton instance
	public static PageRankManager getInstance() {

		if (instance == null)
			instance = new PageRankManager();
		return instance;

	}

	// Constructor (only called once)
	public PageRankManager() {
		try {
			this.graph = (Grapher) Marshaller.deserializeObject(DatabaseManager.getInstance().getGraphData("graph"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Tuple<ArrayList<String>, ArrayList<Float>> computePageRank() {
		setupMatrix();
        remakeMatrix();
        cleanMatrix(); 
        
        ArrayList<Document> allDocuments = DatabaseManager.getInstance().getDocuments();
        ArrayList<String> documentTitle = new ArrayList<String>();
        ArrayList<Float> documentRank = new ArrayList<Float>();
        
        for(int i=0; i<allDocuments.size(); i++) {
        	for(PageVertex v: currentVertices) {
        		if(v.getId() == allDocuments.get(i).getId()) {
        			documentTitle.add(allDocuments.get(i).getName());
        			documentRank.add((float) pageRanks.get(v.getRow()));
        		}
        	}
        }
        
        rankComplete = true;
        
        return new Tuple<ArrayList<String>, ArrayList<Float>>(documentTitle, documentRank);
	}

	private void setupMatrix() {

		double[][] arrayDummy = new double[graph.getGraph().vertexSet().size()][graph.getGraph().vertexSet().size()];
		matrix = new Matrix(arrayDummy);
		int row = 0;
		int col = 0;

		currentVertices = this.graph.getGraph().vertexSet();
		
		Iterator<PageVertex> itr1 = currentVertices.iterator();
		Iterator<PageVertex> itr2 = currentVertices.iterator();
		
		while(itr1.hasNext()) {
			while(itr1.hasNext()) {
				if(this.graph.getGraph().getEdge(itr1.next(), itr2.next()) != null) {	
					matrix.set(row, col, 1);
				}
				else {
					matrix.set(row, col, 0);	
				}
				itr2.next().setCol(col);
				col++;
			}
			itr1.next().setRow(row);
			row++;
			col = 0;
		}
	}

	private void remakeMatrix() {
		for(int i=0; i<matrix.getRowDimension(); i++) {
			Matrix row = matrix.getMatrix(i, i, 0, matrix.getColumnDimension() - 1);

			if(row.norm2() < 1) {
				row.timesEquals(1/matrix.getRowDimension());
			}
			else {
				int count = 0;
				for(int k=0; k<matrix.getColumnDimension(); k++) {
					if(row.get(0, k) == 1) {
						count++;
					}
				}
				row.timesEquals(1/count);
			}

			matrix.setMatrix(i, i, 0, matrix.getColumnDimension() - 1, row);
		}

		matrix.timesEquals(1-a);
		matrix.plusEquals(createAdditionMatrix(matrix.getColumnDimension()));
	}

	private Matrix createAdditionMatrix(int dimension) {
		double[][] dummyArray = new double[dimension][dimension];
		Matrix addMatrix = new Matrix(dummyArray);
		for(int i=0; i<dimension; i++) {
			for(int k=0; k<dimension; k++) {
				addMatrix.set(i, k, a/dimension);
			}
		}
		return addMatrix;
	}

	private void cleanMatrix() {
		for(int i=0; i<matrix.getRowDimension(); i++) {
			Matrix row = matrix.getMatrix(i, i, 0, matrix.getColumnDimension() - 1);
			for(int k=0; k<10; k++) {
				if(k == 0) {
					row = matrix.times(row.transpose());
				}
				else {
					row = matrix.times(row);
				}
			}

			pageRanks.put(i, (float) row.normInf());
		}
	}

	private double calculateTotal() {
		double total = 0;
		for(int i=0; i<matrix.getRowDimension(); i++) {
			for(int k=0; k<matrix.getColumnDimension(); k++) {
				total += matrix.get(i, k);
			}
		}
		return total;
	}

	private void checkResults() {
		for(int i = 0; i < pageRanks.size(); i++) {		
			
			float score = pageRanks.get(i);
			print("Row: " + i + " Score: " + score);
		}

	}

	private void print(String value) {
		System.out.println(value);
	}

	public boolean isRankComplete() {
		return rankComplete;
	}
}

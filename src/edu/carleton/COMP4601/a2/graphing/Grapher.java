package edu.carleton.COMP4601.a2.graphing;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.*;

public class Grapher implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6017417770252581831L;
	private Multigraph<PageVertex, DefaultEdge> graph;
	private ConcurrentHashMap<Integer, PageVertex> vertices;
	private String name;
	
	public Grapher(String name) {
		this.name = name;
		this.graph = new Multigraph<PageVertex, DefaultEdge>(DefaultEdge.class);
		this.vertices = new ConcurrentHashMap<Integer, PageVertex>();
	}
	
	public synchronized boolean addVertex(PageVertex vertex) {
		this.vertices.put(vertex.getId(), vertex);
		return this.graph.addVertex(vertex);
	}

	public synchronized boolean removeVertex(PageVertex vertex) {
		this.vertices.remove(vertex.getId());
		return this.graph.removeVertex(vertex);
	}
	
	public synchronized void addEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.addEdge(vertex1, vertex2);
	}
	
	public synchronized void removeEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.removeEdge(vertex1, vertex2);
	}
	
	public synchronized String getName() {
		return this.name;
	}
}

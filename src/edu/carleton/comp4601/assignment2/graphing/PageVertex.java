package edu.carleton.comp4601.assignment2.graphing;

import java.io.Serializable;

public class PageVertex implements Serializable, Cloneable {

	private static final long serialVersionUID = -1214346455090744350L;
	private Integer id;
	private String url;
	private long duration;
	
	private int row;
	private int col;
	
	public PageVertex(Integer id, String url, long duration) {
		setId(id);
		setUrl(url);
		setDuration(duration);
	}

	public synchronized String getUrl() {
		return url;
	}

	public synchronized void setUrl(String url) {
		this.url = url;
	}

	public synchronized Integer getId() {
		return id;
	}

	public synchronized void setId(int id) {
		this.id = id;
	}

	public synchronized long getDuration() {
		return duration;
	}

	public synchronized void setDuration(long duration) {
		this.duration = duration;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
}

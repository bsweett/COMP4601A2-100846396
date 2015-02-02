package edu.carleton.COMP4601.a2.dao;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

public class Action {
	@Context
	protected UriInfo uriInfo;
	@Context
	protected Request request;

	protected String id;

	// Based action constructor
	// Unused
	public Action(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}

}

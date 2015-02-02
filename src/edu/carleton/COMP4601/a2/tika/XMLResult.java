package edu.carleton.COMP4601.a2.tika;

import org.apache.tika.metadata.Metadata;

public class XMLResult {
	public final String xml;
    public final Metadata metadata;

    public XMLResult(String xml, Metadata metadata) {
        this.xml = xml;
        this.metadata = metadata;
    }
}



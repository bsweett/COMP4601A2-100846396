package edu.carleton.COMP4601.a2.tika;

import org.apache.tika.metadata.Metadata;

public class TextResult {
	public final String text;
    public final Metadata metadata;

    public TextResult(String text, Metadata metadata) {
        this.text = text;
        this.metadata = metadata;
    }
}

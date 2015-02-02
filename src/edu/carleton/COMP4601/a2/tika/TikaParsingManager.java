package edu.carleton.COMP4601.a2.tika;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TikaParsingManager {

	private static TikaParsingManager instance;
	
	public static void setInstance(TikaParsingManager instance) {
		TikaParsingManager.instance = instance;
	}
	
	public static TikaParsingManager getInstance() {

		if (instance == null)
			instance = new TikaParsingManager();
		return instance;

	}
	
	public Metadata parseUsingAutoDetect(InputStream is) throws Exception {
		Parser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		ParseContext context = new ParseContext();
		
		try {
			parser.parse(is, handler, metadata, context);
		} finally {
			is.close();
		}
		
		return metadata;
	}
	


}

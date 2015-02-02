package edu.carleton.COMP4601.a2.tika;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
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
	
	/*
	public Metadata parseMetadataForImageWithType(InputStream is, String imageType) throws Exception {
		Metadata metadata = new Metadata();
		metadata.set(Metadata.CONTENT_TYPE, imageType);
		
		try {
			new ImageParser().parse(is, new DefaultHandler(), metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}
	
	public Metadata parseMetadataForPDF(InputStream is) throws Exception {
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		
		try {
			new PDFParser().parse(is, handler, metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}
	
	public Metadata parseMetadataForDOC(InputStream is) throws Exception {
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		
		try {
			new OfficeParser().parse(is, handler, metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}*/
	
	public TextResult getText(InputStream is, Parser parser, Metadata metadata, ParseContext context) throws Exception{
		ContentHandler handler = new BodyContentHandler(1000000);
		try {
			parser.parse(is, handler, metadata, context);
		} finally {
			is.close();
		}
		return new TextResult(handler.toString(), metadata);
	}
	
	public XMLResult getXML(InputStream input, Parser parser, Metadata metadata) throws Exception {
		ParseContext context = new ParseContext();
		context.set(Parser.class, parser);

		try {
			ContentHandler handler = new ToXMLContentHandler();
			parser.parse(input, handler, metadata, context);
			return new XMLResult(handler.toString(), metadata);
		} finally {
			input.close();
		}
	}

}

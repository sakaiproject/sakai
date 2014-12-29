package org.sakaiproject.citation.impl.openurl;

/**
 * Different formats for the data being transferred.
 * @author buckett
 *
 */
public interface Format {

	boolean canHandle(String format);
	ContextObject parse(String data);
	String encode(ContextObject contextObject);
	
}

package org.sakaiproject.citation.impl.openurl;


/**
 * A holder for a contextobject which has yet to be parsed into entities.
 * It might have been parsed into key/values.
 * @author buckett
 *
 */
public class RawContextObject {

	private String format;
	private String data;
	
	public RawContextObject(String format, String data) {
		this.format = format;
		this.data = data;
	}

	public String getFormat() {
		return format;
	}
	public String getData() {
		return data;
	}
}

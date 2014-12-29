package org.sakaiproject.citation.impl.openurl;

import org.sakaiproject.citation.api.Citation;

/**
 * A two way conversion between a ContextObject and a Citation.
 * @author buckett
 *
 */
public interface Converter {

	public boolean canConvertOpenUrl(String format);
	public boolean canConvertCitation(String format);
	public ContextObjectEntity convert(Citation citation);
	public Citation convert(ContextObjectEntity entity);

}

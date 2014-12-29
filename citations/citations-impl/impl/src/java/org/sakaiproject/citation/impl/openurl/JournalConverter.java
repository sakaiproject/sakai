package org.sakaiproject.citation.impl.openurl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationService;

/**
 * Converts from a ContextObjectEntity to a Citation and back.
 * @author buckett
 *
 */
public class JournalConverter extends AbstractConverter {

	private final static Log log = LogFactory.getLog(JournalConverter.class);
	
	public final static String ID = "info:ofi/fmt:kev:mtx:journal";
	
	private CitationService citationService;
	
	public void setCitationService(CitationService citationService) {
		this.citationService = citationService;
	}

	private static BidiMap conversion = new TreeBidiMap();
	static {
		// From Citation to OpenURL.
		conversion.put("creator", "au");
		conversion.put("title", "atitle");
		conversion.put("sourceTitle", "jtitle");
		conversion.put("date", "date");
		conversion.put("volume", "volume");
		conversion.put("issue", "issue");
		conversion.put("startPage", "spage");
		conversion.put("endPage", "epage");
		conversion.put("pages", "pages");;
		conversion.put("isnIdentifier", "issn");
		// DOI and ISBN should become IDs on the context object.
	}
	
	@Override
	protected String getCitationsKey(String openUrlKey) {
		return (String) conversion.getKey(openUrlKey);
	}
	
	@Override
	protected String getOpenUrlKey(String citationsKey) {
		return (String) conversion.get(citationsKey);
	}
	
	public boolean canConvertOpenUrl(String format) {
		return ID.equals(format);
	}
	
	public boolean canConvertCitation(String type) {
		return "journal".equals(type);
	}
	

	public ContextObjectEntity convert(Citation citation) {
		Map<String,Object> props = citation.getCitationProperties();
		ContextObjectEntity entity = new ContextObjectEntity();
		entity.addValue("genre", "journal");
		// Loop through the citation props
		convertSimple(props, entity);
		// Do other mapping.
		if (citation.hasCitationProperty("doi")) {
			Object value = citation.getCitationProperty("doi", false);
			addId(entity, value, DOI_PREFIX);
		}
		return entity;
	}
	
	public Citation convert(ContextObjectEntity entity) {
		Map<String, List<String>> values = entity.getValues();
		
		// Set the genre.
		String genre = null;
		List<String> genres = values.get("genre");
		if (genres != null) {
			genre = genres.get(0);
		}
		if (genre == null) {
			genre = "article";
		}
		
		Citation citation = citationService.addCitation(genre);

		// Map the IDs from CO the citation.
		for (String id: entity.getIds()) {
			if (id.startsWith(ISSN_PREFIX)) {
				String isbn = id.substring(ISSN_PREFIX.length());
				if (isbn.length() > 0 ) {
					citation.setCitationProperty("isnIdentifier", isbn);
				}
			} else if (id.startsWith(DOI_PREFIX)) {
				String doi = id.substring(DOI_PREFIX.length());
				if (doi.length() > 0) {
					citation.setCitationProperty(DOI_PREFIX, doi);
				}
			} else {
				citation.setCitationProperty("otherIds", id);
			}
		}
		
		// Map the rest of the values.
		convertSimple(values, citation);
		
		String author = Utils.lookForAuthor(values);
		if (author != null) {
			citation.setCitationProperty("creator", author);
		}
		return citation;
	}

}

/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.citation.impl.openurl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationService;

/**
 * Converts from a ContextObjectEntity to a Citation and back.
 * @author buckett
 *
 */
public class BookConverter extends AbstractConverter implements Converter {
	
	public final static String ID = "info:ofi/fmt:kev:mtx:book";
	
	private CitationService citationService;
	
	public void setCitationService(CitationService citationService) {
		this.citationService = citationService;
	}

	static BidiMap conversion = new TreeBidiMap();
	static {
		// From Citation to OpenURL.
		conversion.put("creator", "au");
		conversion.put("title", "btitle");
		conversion.put("year", "date"); // TODO Should validate date (also "date", date").
		conversion.put("publisher", "pub");
		conversion.put("publicationLocation", "place");
		conversion.put("edition", "edition");
		conversion.put("sourceTitle", "series"); // Only for books, sourceTitle is used for other things.
		conversion.put("isnIdentifier", "isbn");
		// DOI and ISBN should become IDs on the context object.
	}
	
	public String getOpenUrlKey(String citationsKey) {
		return (String) conversion.get(citationsKey);
	}
	
	public String getCitationsKey(String openUrlKey) {
		return (String) conversion.getKey(openUrlKey);
	}
	

	public boolean canConvertOpenUrl(String format) {
		return ID.equals(format);
	}
	
	public boolean canConvertCitation(String type) {
		return "book".equals(type);
	}
	

	public ContextObjectEntity convert(Citation citation) {
		Map<String,Object> props = citation.getCitationProperties();
		ContextObjectEntity entity = new ContextObjectEntity();
		entity.addValue("genre", "book");
		// Loop through the citation props
		convertSimple(props, entity);
		
		// Handle ISBN
		if(citation.hasCitationProperty("isbn")) {
			Object value = citation.getCitationProperty("isbn", false);
			addId(entity, value, ISBN_URN_PREFIX);
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
			genre = "book";
		}
		
		Citation citation = citationService.addCitation(genre);

		// Map the IDs from CO the citation.
		for (String id: entity.getIds()) {
			if (id.startsWith(ISBN_URN_PREFIX)) {
				String isbn = id.substring(ISBN_URN_PREFIX.length());
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
		
		convertSimple(values, citation);
		String author = Utils.lookForAuthor(values);
		if (author != null) {
			citation.setCitationProperty("creator", author);
		}
		
		
		return citation;
	}
	
}

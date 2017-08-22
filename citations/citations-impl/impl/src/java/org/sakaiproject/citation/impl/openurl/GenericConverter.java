/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationService;


/**
 * This just attempts to convert as much of the data as possible without worrying about 
 * what it should be attempting to convert.
 * @author buckett
 *
 */
public class GenericConverter extends AbstractConverter {

	private CitationService citationService;
	
	public void setCitationService(CitationService citationService) {
		this.citationService = citationService;
	}
	
	private static BidiMap conversion = new TreeBidiMap();
	static {
		// From Citation to OpenURL.
		// conversion.put(citationProp, openUrlProp)
		conversion.put("creator", "au");
		//conversion.put("year", "date"); // TODO Should validate date (also "date", date").
		conversion.put("publisher", "pub");
		conversion.put("publicationLocation", "place");
		conversion.put("edition", "edition");
		// conversion.put("sourceTitle", "series"); // Only for books, sourceTitle is used for other things.
		// From Journals
		conversion.put("sourceTitle", "jtitle");
		// conversion.put("date", "date");
		conversion.put("volume", "volume");
		conversion.put("issue", "issue");
		conversion.put("startPage", "spage");
		conversion.put("endPage", "epage");
		conversion.put("pages", "pages");
		conversion.put("date", "date");
		conversion.put("isnIdentifier", "issn");
		// DOI and ISBN should become IDs on the context object.
		
		// For books citation(date) -> openurl(date)
		// For journals citation(year) -> openurl(date)
	}
	
	private static class Genre {
		String openUrlId;
		String openUrlGenre;
		String citationScheme;
		private Genre(String openUrlId, String openUrlGenre, String citationScheme) {
			this.openUrlId = openUrlId;
			this.openUrlGenre = openUrlGenre;
			this.citationScheme = citationScheme;
		}
	}
	
	private static Collection<Genre> genreConversion = new ArrayList<Genre>();
	static {
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "journal", "article"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "issue", "article"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "article", "article"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "proceeding", "proceeding"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "conference", "proceeding"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "report", "report"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:journal", "document", "report"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "book", "book"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "bookitem", "chapter"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "book", "book"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "proceeding", "proceeding"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "conference", "proceeding"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "report", "report"));
		genreConversion.add(new Genre("info:ofi/fmt:kev:mtx:book", "document", "report"));
	}


	@Override
	protected String getOpenUrlKey(String citationKey) {
		return (String) conversion.get(citationKey);
	}

	@Override
	protected String getCitationsKey(String openUrlKey) {
		return (String) conversion.getKey(openUrlKey);
	}
	
	public boolean canConvertOpenUrl(String format) {
		return true;
	}

	public boolean canConvertCitation(String type) {
		return true;
	}

	public ContextObjectEntity convert(Citation citation) {
		Map<String, Object> props = citation.getCitationProperties();
		ContextObjectEntity entity = new ContextObjectEntity();
		convertSimple(props, entity);
		String schema = citation.getSchema().getIdentifier();
		for (Genre genre: genreConversion) {
			if (genre.citationScheme.equals(schema)) {
				entity.setFormat(genre.openUrlId);
				entity.addValue("genre", genre.openUrlGenre);
				break;
			}
		}
		if(citation.hasCitationProperty("isbn")) {
			Object value = citation.getCitationProperty("isbn", false);
			addId(entity, value, ISBN_URN_PREFIX);
		}
		// Do other mapping.
		if (citation.hasCitationProperty("doi")) {
			Object value = citation.getCitationProperty("doi", false);
			addId(entity, value, DOI_PREFIX);
		}
		
		// TODO Guess the genre
		
		return entity;
	}

	public Citation convert(ContextObjectEntity entity) {
		Map<String, List<String>> values = entity.getValues();
		// Deal with genre.
		Genre destGenre = null;
		if(values.containsKey("genre")) {
			List<String> genres = values.get("genre"); // Should only have 1
			if (!genres.isEmpty()) {
				String openUrlGenre = genres.get(0);
				for (Genre genre : genreConversion) {
					if (genre.openUrlGenre.equals(openUrlGenre)) {
						destGenre = genre;
						break;
					}
				}
			}
		}
		
		Citation citation = citationService.addCitation(destGenre != null? destGenre.citationScheme: "unknown");
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
					citation.setCitationProperty("doi", doi);
				}
			} else if (id.startsWith(ISSN_PREFIX)) {
				String issn = id.substring(ISSN_PREFIX.length());
				if (issn.length() > 0) {
					citation.setCitationProperty("isnIdentifier", issn);
				}
			} else {
				citation.setCitationProperty("otherIds", id);
			}
		}
		convertSimple(values, citation);
		
		convertSingle(values, citation, "atitle", "title");
		convertSingle(values, citation, "btitle", "title");
		convertSingle(values, citation, "issn", "isnIdentifier");
		convertSingle(values, citation, "isbn", "isnIdentifier");
		if (destGenre != null) {
			if ("info:ofi/fmt:kev:mtx:journal".equals(destGenre.openUrlId)) {
				convertSingle(values, citation, "title", "sourceTitle");
				if ("journal".equals(destGenre.openUrlGenre)) {
					// If it says it's a journal and doesn't have a title already use the jtitle
					convertSingle(values, citation, "jtitle", "title");
					// Clear out the sourceTitle.
					citation.setCitationProperty("sourceTitle", null);
				}
			}
			if ("info:ofi/fmt:kev:mtx:book".equals(destGenre.openUrlId)) {
				convertSingle(values, citation, "title", "title");
			}
		}
		
		
		String author = Utils.lookForAuthor(values);
		if (author != null) {
			citation.setCitationProperty("creator", author);
		}
		
		
		return citation;
	}

	
	/**
	 * Converts a single value only if a value doesn't exist for the field already.
	 * @param values
	 * @param citation
	 * @param srcProp
	 * @param destProp
	 * @return
	 */
	private boolean convertSingle(Map<String, List<String>> values,
			Citation citation, String srcProp, String destProp) {
		List<String> valueList = values.get(srcProp);
		if (valueList != null && !valueList.isEmpty() && !citation.hasCitationProperty(destProp)) {
			String value = valueList.get(0);
			if (value != null && value.length() > 0) {
				citation.setCitationProperty(destProp, value);
				return true;
			}
		}
		return false;
	}


}

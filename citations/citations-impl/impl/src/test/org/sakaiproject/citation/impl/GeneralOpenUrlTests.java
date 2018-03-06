/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.citation.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.impl.openurl.BookConverter;
import org.sakaiproject.citation.impl.openurl.ContextObject;
import org.sakaiproject.citation.impl.openurl.ContextObjectEntity;
import org.sakaiproject.citation.impl.openurl.KEVFormat;
import org.sakaiproject.citation.impl.openurl.ContextObject.Entity;

@Slf4j
public class GeneralOpenUrlTests extends BaseCitationServiceSupport {
	public void testCreateCollection() {
		BaseCitationService api = createCitationService();
		
		BookConverter bookConverter = new BookConverter();
		bookConverter.setCitationService(api);
		
		CitationCollection collection = api.addCollection();
		Citation book = api.addCitation("book");
		book.setCitationProperty(Schema.CREATOR, "An Author");
		book.setCitationProperty(Schema.TITLE, "Book Title & More");
		book.setCitationProperty(Schema.ISN, "123456789X");
		
		ContextObjectEntity bookEntity = bookConverter.convert(book);
		ContextObject contextObject = new ContextObject();
		contextObject.getEntities().put(Entity.REFERENT, bookEntity);
		
		KEVFormat formatter = new KEVFormat();
		String output = formatter.encode(contextObject);
		assertNotNull(output);
		log.debug(output);
		ContextObject parsed = formatter.parse("&ctx_id=Z39.88-2004&rft_val_fmt=info:ofi/fmt:kev:mtx:ctx&rft.genre=book&rft.btitle=Book%20Title&rft.au=An%20Author");
		log.debug("{}", parsed.getEntities().get(Entity.REFERENT).getValues().get("btitle"));
		api.save(collection);
	}
}

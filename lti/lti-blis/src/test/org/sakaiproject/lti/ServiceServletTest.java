/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lti;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tsugi.lti.POXRequestHandler;

public class ServiceServletTest {

	@Test
	public void isPoxXmlContentTypeAcceptsXmlMediaTypes() {
		assertTrue(POXRequestHandler.isXmlContentType("application/xml"));
		assertTrue(POXRequestHandler.isXmlContentType("application/xml; charset=UTF-8"));
		assertTrue(POXRequestHandler.isXmlContentType("text/xml"));
		assertTrue(POXRequestHandler.isXmlContentType("text/xml; charset=UTF-8"));
		assertTrue(POXRequestHandler.isXmlContentType(" APPLICATION/XML ; charset=UTF-8"));
	}

	@Test
	public void isPoxXmlContentTypeRejectsNonXmlMediaTypes() {
		assertFalse(POXRequestHandler.isXmlContentType(null));
		assertFalse(POXRequestHandler.isXmlContentType(""));
		assertFalse(POXRequestHandler.isXmlContentType("application/json"));
		assertFalse(POXRequestHandler.isXmlContentType("application/xml-dtd"));
		assertFalse(POXRequestHandler.isXmlContentType("text/xml-external-parsed-entity"));
	}
}

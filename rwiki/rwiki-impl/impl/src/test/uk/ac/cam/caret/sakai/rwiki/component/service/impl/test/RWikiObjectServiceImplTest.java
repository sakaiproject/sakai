/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package uk.ac.cam.caret.sakai.rwiki.component.service.impl.test;

import java.util.HashSet;

import uk.ac.cam.caret.sakai.rwiki.component.service.impl.RWikiObjectServiceImpl;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectImpl;
import junit.framework.TestCase;

public class RWikiObjectServiceImplTest extends TestCase {

	RWikiObjectServiceImpl rwosi = null;
	RWikiCurrentObjectImpl rwco = new RWikiCurrentObjectImpl();
	
	public RWikiObjectServiceImplTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		rwosi = new RWikiObjectServiceImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testExtractReferencesEmptyConstructor() {
		assertNotNull(rwosi);
	}
	
	// can deal with page with no references
	public void testExtractReferencesExtractReferencesEmpty() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet hs = new HashSet();
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assert(sb.length() == 2);
	}

	// can deal with page with one references
	public void testExtractReferencesExtractReferencesOneReference() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet<String> hs = new HashSet<String>();
		hs.add("oneReference");
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assertEquals("one reference string",16,sb.length());
	}
	
	// can deal with page with multiple references
	public void testExtractReferencesExtractReferencesTwoReference() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet<String> hs = new HashSet<String>();
		hs.add("oneReference");
		hs.add("twoReference");
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assertEquals("two reference strings",30,sb.length());
		
	}
	
	// can deal with page with too many references
	public void testExtractReferencesExtractReferencesTooLong() {
		StringBuffer sb;
		rwco = new RWikiCurrentObjectImpl();
		HashSet<String> hs = new HashSet<String>();
		
		hs.add(longString(3990));
		sb = rwosi.extractReferences(rwco, hs);
		assertEquals("two reference strings pass",3994,sb.length());
		
		hs.add("A");
		sb = rwosi.extractReferences(rwco, hs);
		assertEquals("two reference strings pass",3997,sb.length());
		
		hs.add("twoReference");
		sb = rwosi.extractReferences(rwco, hs);
		assertEquals("three reference strings last one too long",3997,sb.length());

		
	}
	
	// make an arbitrarily long string
	String longString(int size) {
		StringBuffer sb  = new StringBuffer();
		int i = 0;
		while(i++ < size) {
			sb.append("X");
		}
		return sb.toString();
	}

}

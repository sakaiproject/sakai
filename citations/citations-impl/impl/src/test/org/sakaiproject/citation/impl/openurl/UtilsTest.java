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
package org.sakaiproject.citation.impl.openurl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

	public void testGetValue() {
		Map<String, String[]> values = new HashMap<String, String[]>();
		values.put("1", new String[]{"a", "b", "c"}); // Normal
		values.put("2", new String[]{}); // Empty array.
		values.put("3", new String[]{"z"});
		values.put("4", null);
		values.put("5", new String[]{null});
		
		assertEquals("a", Utils.getValue(values, "1"));
		assertNull(Utils.getValue(values, "2"));
		assertEquals("z", Utils.getValue(values, "3"));
		assertNull(Utils.getValue(values, "4"));
		assertNull(Utils.getValue(values, "missing"));
		assertNull(Utils.getValue(values, "5"));
		
	}

	public void testSplit() {
		assertNotNull(Utils.split(null));
		assertEquals(0, Utils.split(null).size());
		assertNotNull(Utils.split(""));
		assertEquals(0, Utils.split("").size());
		
		Map<String, String[]> values;
		
		values = Utils.split("onePair=yes");
		assertEquals("yes", values.get("onePair")[0]);
		
		values = Utils.split("noValue");
		assertTrue(values.containsKey("noValue"));
		assertNull(values.get("noValue")[0]);
		
		values = Utils.split("noValue&aValue=value");
		assertNotNull(values.get("noValue"));
		assertEquals("value", values.get("aValue")[0]);
		
		values = Utils.split("&&&&&====&&");
		assertEquals(0, values.size());
		
		values = Utils.split("key=%20&key=%34");
		assertEquals(2, values.get("key").length);
		assertTrue(Arrays.asList(values.get("key")).contains("%20"));
	}
	
	public void testSplitPerf() {
		// Check performance is acceptable.
		String exampleString = "ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&ctx_tim=2010-10-20T13:27:00IST&url_ver=Z39.88-2004&url_ctx_fmt=infofi/fmt:kev:mtx:ctx&rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Journal-UkOxU&rft_val_fmt=info:ofi/fmt:kev:mtx:book&rft.genre=book&rft.atitle=&rft.jtitle=&rft.btitle=Linux%20in%20a%20nutshell&rft.aulast=Siever&rft.auinit=&rft.auinit1=&rft.auinitm=&rft.ausuffix=&rft.au=&rft.aucorp=&rft.volume=&rft.issue=&rft.part=&rft.quarter=&rft.ssn=&rft.spage=&rft.epage=&rft.pages=&rft.artnum=&rft.issn=&rft.eissn=&rft.isbn=9780596154486&rft.sici=&rft.coden=&rft_id=info:doi/&rft.object_id=&rft_dat=<UkOxU>UkOxUb17140770</UkOxU>&rft.eisbn=";
		for (int i = 0; i< 1000; i++) {
			Map<String, String[]> values = Utils.split(exampleString);
			assertEquals("Z39.88-2004", values.get("ctx_ver")[0]);
		}
	}

	public void testDecode() {
		Map<String, String[]> raw = new HashMap<String, String[]>();
		raw.put("1", new String[]{"a", "b", "c"}); //No need to decode.
		raw.put("2", new String[]{}); // Nothing to decode.
		raw.put("3", new String[]{null});
		raw.put("4", new String[]{"Hello%20World"}); // ASCII, shouldn't matter.
		raw.put("5", new String[]{"A Greek chrarater %CE%86"}); // UTF-8
		raw.put("6", new String[]{"E with dots %CB"}); // ISO-8859-1
		
		Map<String, String[]> decoded;
		decoded = Utils.decode(raw, "UTF-8");
		assertEquals("a", decoded.get("1")[0]);
		assertEquals(0, decoded.get("2").length);
		assertEquals(1, decoded.get("3").length);
		assertEquals("Hello World", decoded.get("4")[0]);
		assertEquals("A Greek chrarater \u0386", decoded.get("5")[0]);
		
		decoded = Utils.decode(raw, "ISO-8859-1");
		assertEquals("E with dots \u00CB", decoded.get("6")[0]);
	}
	
	public void testLookForAuthor() {
		Map<String, List<String>> values = new HashMap<String, List<String>>();
		assertNull(Utils.lookForAuthor(values));
		
		values.put("au", Collections.singletonList("Buckett, Matthew"));
		assertNull(Utils.lookForAuthor(values));
		
		values.put("aufirst", Collections.singletonList("Matthew"));
		assertNull(Utils.lookForAuthor(values));
		
		values.put("auinit", Collections.singletonList("M"));
		assertNull(Utils.lookForAuthor(values));
		
		values.put("aulast", Collections.singletonList("Buckett"));
		assertNull(Utils.lookForAuthor(values));
		
		// Now switch to a different author
		values.put("au", Collections.singletonList("Smith, John"));
		assertEquals("Buckett, Matthew", Utils.lookForAuthor(values));
		
		values.remove("aufirst");
		assertEquals("Buckett, M", Utils.lookForAuthor(values));
		
		values.put("auinitm", Collections.singletonList("A"));
		values.remove("auinit");
		assertEquals("Buckett", Utils.lookForAuthor(values));
		
		values.put("auinit1", Collections.singletonList("M"));
		assertEquals("Buckett, M A", Utils.lookForAuthor(values));
	}
	
	public void testLookForAuthorBad() {
		Map<String, List<String>> values = new HashMap<String, List<String>>();
		values.put("au", new ArrayList<String>());
		
		values.put("au", Collections.singletonList("Buckett, Matthew"));
		assertNull(Utils.lookForAuthor(values));
		
		values.put("aulast", new ArrayList<String>());
		assertNull(Utils.lookForAuthor(values));
	}

}

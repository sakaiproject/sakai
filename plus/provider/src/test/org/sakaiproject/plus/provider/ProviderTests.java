/*
 * Copyright (c) 2022- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.provider;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Test;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

@Slf4j
public class ProviderTests {

	@Test
	public void testJSONCopyOrReference() {
		// Do the simple JSON accessors retrieve a copy or a reference to 
		// the tree of values and can you change them to influence the underlying tree
		String jsonStr = "{\"title\":\"SakaiLMS\",\"exts\":[{\"plat\":\"sakailms.com\",\"places\":[{\"text\":\"text 1\"},{\"text\":\"text 2\"}]}]}";
		JSONObject json1 = (JSONObject) JSONValue.parse(jsonStr);
		assertNotNull(json1);
		JSONArray exts = (JSONArray) json1.get("exts");
		JSONObject firstext = (JSONObject) exts.get(0);
		JSONArray places = (JSONArray) firstext.get("places");
		JSONObject secondp = (JSONObject) places.get(0);
		JSONObject secondp2 = (JSONObject) places.get(0);
		secondp2.put("new", "42");
		assertTrue(secondp2.toString().contains("42"));
		assertTrue(secondp.toString().contains("42"));
		assertTrue(places.toString().contains("42"));
		assertTrue(json1.toString().contains("42"));
	}

}

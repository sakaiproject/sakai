/**
 * Copyright (c) 2009-2017 The Apereo Foundation
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
package org.sakaiproject.lti.util;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.json.simple.JSONObject;

import org.sakaiproject.util.Xml;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.tsugi.lti.LTIUtil;
import org.tsugi.lti.LTIConstants;
import org.tsugi.lti13.LTI13ConstantsUtil;

import org.apache.commons.text.StringEscapeUtils;

@Slf4j
public class SakaiLTIUtilTest {

	public static String [] shouldBeTheSame = {
		null,
		"",
		"     ",
		" \n \n",
		"42",
		"x=1",
		"x=1;",
		"x=1;\nx=2;",
		"x=1; ",
		"x=1;y=2;99;z=3", // Can have only 1 semicolon between = signs
		"x=1;42",
		"x=1;y=2z=3;",
		"x;19=1;42",
		"x=1\ny=2\nz=3",
		"x=1;\ny=2\nz=3"
	};

	public static Set<String> projectRoles = Set.<String>of("access", "maintain");
	public static Set<String> courseRoles = Set.<String>of("Student", "Instructor", "Teaching Assistant"); // Keep the blank!
	public static Set<String> ltiRoles = Set.<String>of("Instructor", "Teaching Assistant",
		"ContentDeveloper", "Faculty", "Member", "Learner", "Mentor", "Staff", "Alumni", "ProspectiveStudent", "Guest",
		"Other", "Administrator", "Manager", "Observer", "Officer", "None"
	);

	@Before
	public void setUp() throws Exception {
	}

	/**
         * If it is null, blank, or has no equal signs return unchanged
         * If there is one equal sign return unchanged
         * If there is a new line anywhere in the string after trim, return unchanged
         * If we see ..=..;..=..;..=..[;] - we replace ; with \n
	 */

	@Test
	public void testStrings() {
		String adj = null;
		for(String s: shouldBeTheSame) {
			adj = SakaiLTIUtil.adjustCustom(s);
			assertEquals(s, adj);
		}

		adj = SakaiLTIUtil.adjustCustom("x=1;y=2;z=3");
		assertEquals(adj,"x=1;y=2;z=3".replace(';','\n'));
		adj = SakaiLTIUtil.adjustCustom("x=1;y=2;z=3;");
		assertEquals(adj,"x=1;y=2;z=3;".replace(';','\n'));
	}
	@Test
	public void testStringGrade() {
		String grade="";
		try {
			grade = SakaiLTIUtil.getRoundedGrade(0.57,100.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}

		assertEquals(grade,"57.0");

		try {
			grade = SakaiLTIUtil.getRoundedGrade(0.5655,100.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}

		assertEquals(grade,"56.55");
	}

	// Something like: 4bd442a8b27e647e:2803e729800336b20a77d61b2da6db3f:790b8098f8bb4407f96304e701eeb58e:AES/CBC/PKCS5Padding
	// But each encryption is distinct
	public boolean goodEncrypt(String enc) {
		String [] pieces = enc.split(":");
		if ( pieces.length != 4 ) {
			System.out.println("Bad encryption - too few pieces\n"+enc);
			return false;
		}
		if ( ! "AES/CBC/PKCS5Padding".equals(pieces[3]) ) {
			System.out.println("Bad encryption - must end with AES/CBC/PKCS5Padding\n"+enc);
			return false;
		}
		return true;
	}

	@Test
	public void testEncryptDecrypt() {
		String plain = "plain";
		String key = "bob";
		String encrypt1 = SakaiLTIUtil.encryptSecret(plain, key);
		assertFalse(plain.equals(encrypt1));
		assertTrue(goodEncrypt(encrypt1));
		// No double encrypt
		String encrypt2 = SakaiLTIUtil.encryptSecret(encrypt1, key);
		assertTrue(goodEncrypt(encrypt2));
		assertEquals(encrypt1, encrypt2);
		boolean checkonly = false;
		String decrypt = SakaiLTIUtil.decryptSecret(encrypt2, key, checkonly);
		assertEquals(plain, decrypt);
	}

	@Test
	public void testLaunchCodes() {
		Map<String, Object> content = new TreeMap<String, Object>();
		content.put(LTIService.LTI_ID, "42");
		content.put(LTIService.LTI_PLACEMENTSECRET, "xyzzy");

		String launch_code_key = SakaiLTIUtil.getLaunchCodeKey(content);
		assertEquals(launch_code_key,"launch_code:42");

		String launch_code = SakaiLTIUtil.getLaunchCode(content);
		assertTrue(SakaiLTIUtil.checkLaunchCode(content, launch_code));

		content.put(LTIService.LTI_PLACEMENTSECRET, "wrong");
		assertFalse(SakaiLTIUtil.checkLaunchCode(content, launch_code));

		// Correct password different id
		content.put(LTIService.LTI_ID, "43");
		content.put(LTIService.LTI_PLACEMENTSECRET, "xyzzy");
		assertFalse(SakaiLTIUtil.checkLaunchCode(content, launch_code));
	}

	@Test
	public void testConvertLong() {
		Long l = LTIUtil.toLongNull(Long.valueOf(2));
		assertEquals(l, Long.valueOf(2));
		l = LTIUtil.toLongNull(Double.valueOf(2.2));
		assertEquals(l, Long.valueOf(2));
		l = LTIUtil.toLongNull(null);
		assertEquals(l, null);
		l = LTIUtil.toLongNull("fred");
		assertEquals(l, null);
		l = LTIUtil.toLongNull("null");
		assertEquals(l, null);
		l = LTIUtil.toLongNull("NULL");
		assertEquals(l, null);
		// This one is a little weird but it is how it was written - double is different
		l = LTIUtil.toLongNull("");
		assertEquals(l, null);
		l = LTIUtil.toLongNull("2");
		assertEquals(l, Long.valueOf(2));
		l = LTIUtil.toLongNull("2.5");
		assertEquals(l, null);
		l = LTIUtil.toLongNull(Float.valueOf(3.1f));
		assertEquals(l, Long.valueOf(3));
		// Casting truncates
		l = LTIUtil.toLongNull(Float.valueOf(3.9f));
		assertEquals(l, Long.valueOf(3));
		l = LTIUtil.toLongNull(Integer.valueOf(3));
		assertEquals(l, Long.valueOf(3));
	}

	@Test
	public void testConvertDouble() {
		Double d = LTIUtil.toDoubleNull(Double.valueOf(2.0));
		assertEquals(d, Double.valueOf(2.0));
		d = LTIUtil.toDoubleNull(Double.valueOf(2.5));
		assertEquals(d, Double.valueOf(2.5));
		d = LTIUtil.toDoubleNull(null);
		assertEquals(d, null);
		d = LTIUtil.toDoubleNull("fred");
		assertEquals(d, null);
		d = LTIUtil.toDoubleNull("null");
		assertEquals(d, null);
		d = LTIUtil.toDoubleNull("NULL");
		assertEquals(d, null);
		d = LTIUtil.toDoubleNull("");
		assertEquals(d, null);
		d = LTIUtil.toDoubleNull("2.0");
		assertEquals(d, Double.valueOf(2.0));
		d = LTIUtil.toDoubleNull("2.5");
		assertEquals(d, Double.valueOf(2.5));
		d = LTIUtil.toDoubleNull("2");
		assertEquals(d, Double.valueOf(2.0));
		d = LTIUtil.toDoubleNull(Long.valueOf(3));
		assertEquals(d, Double.valueOf(3.0));
		d = LTIUtil.toDoubleNull(Integer.valueOf(3));
		assertEquals(d, Double.valueOf(3.0));
	}

	@Test
	public void testFindBestTool() {
		List<Map<String,Object>> tools = new ArrayList<Map<String,Object>>();
		Map<String,Object> tool = new HashMap<String,Object>();

		String [] toolUrls = {
			"https://www.py4e.com/",
			"https://www.py4e.com/mod/",
			"https://www.py4e.com/mod/gift/",
			"https://www.py4e.com/mod/gift/?quiz=123"
		};

		String siteId = "tsugi-site";
		String leastSpecific = toolUrls[0];
		String mostSpecific = toolUrls[3];
		String bestSite;
		String bestLaunch;

		Map<String,Object> bestTool = null;

		tools = new ArrayList<Map<String,Object>>();
		// Lets make some globals in least specific to most specific
		for(String s: toolUrls) {
			tool.put(LTIService.LTI_LAUNCH, s);
			tool.put(LTIService.LTI_SITE_ID, ""); // Global
			tools.add(tool);

			bestTool = SakaiLTIUtil.findBestToolMatch(s, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals("", bestSite);

			bestTool = SakaiLTIUtil.findBestToolMatch(mostSpecific, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals("", bestSite);
		}


		tools = new ArrayList<Map<String,Object>>();
		// Lets make some globals in least specific to most specific
		for(String s: toolUrls) {
			tool.put(LTIService.LTI_LAUNCH, s);
			tool.put(LTIService.LTI_SITE_ID, ""); // Global
			tools.add(tool);

			bestTool = SakaiLTIUtil.findBestToolMatch(s, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals("", bestSite);

			bestTool = SakaiLTIUtil.findBestToolMatch(mostSpecific, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals("", bestSite);
		}

		// Lets add a local low priority - see if it wins
		tool.put(LTIService.LTI_LAUNCH, leastSpecific);
		tool.put(LTIService.LTI_SITE_ID, siteId);
		tools.add(tool);

		bestTool = SakaiLTIUtil.findBestToolMatch(mostSpecific, null, tools);
		bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
		bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
		assertEquals(leastSpecific, bestLaunch);
		assertEquals(siteId, bestSite);

		// Lets make locals and globals, and make sure we never get a global
		tools = new ArrayList<Map<String,Object>>();
		for(String s: toolUrls) {
			tool.put(LTIService.LTI_LAUNCH, s);
			tool.put(LTIService.LTI_SITE_ID, ""); // Global
			tools.add(tool);
			tool.put(LTIService.LTI_LAUNCH, s);
			tool.put(LTIService.LTI_SITE_ID, siteId); // Local
			tools.add(tool);

			bestTool = SakaiLTIUtil.findBestToolMatch(s, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals(siteId, bestSite);

			bestTool = SakaiLTIUtil.findBestToolMatch(mostSpecific, null, tools);
			bestLaunch = (String) bestTool.get(LTIService.LTI_LAUNCH);
			bestSite = (String) bestTool.get(LTIService.LTI_SITE_ID);
			assertEquals(s, bestLaunch);
			assertEquals(siteId, bestSite);
		}

	}

	/* Quick story.  When reviewing PR#8884 - the collective wisdom was not to
	 * just scan for a question mark and chop.  MJ said use the URI builder.
	 * CS was worried that it would do weird things to the string like add or
	 * remove a :443 in an attempt to make the URL "better".
	 * So I wrote a unit test to defend against that eventuality and here it is.
	 */
	public String crudeButEffectiveStripOffQuery(String urlString)
	{
		if ( urlString == null ) return null;
        String retval = urlString;
        int pos = retval.indexOf('?');
        if ( pos > 1 ) {
            retval = retval.substring(0,pos);
        }
        return retval;
	}

	@Test
	public void testStripOffQuery() {
		String testUrls[] = {
			"http://localhost:8080",
			"http://localhost:8080/",
			"http://localhost:8080/zap",
			"http://localhost:8080/zap/",
			"http://localhost:8080/zap/bob.php?x=1234",
			"https://www.py4e.com",
			"https://www.py4e.com/",
			"https://www.py4e.com/zap/",
			"https://www.py4e.com/zap/bob.php?x=1234",
			"https://www.py4e.com:443/zap/bob.php?x=1234",
			"https://www.sakailms.org/"
		};

		for(String s: testUrls) {
			assertEquals(SakaiLTIUtil.stripOffQuery(s), crudeButEffectiveStripOffQuery(s));
		}
	}

	@Test
	public void testTrackResourceLinkID() {
		Map<String, Object> oldContent = new TreeMap<String, Object> ();
		JSONObject old_json = LTIUtil.parseJSONObject("");
		old_json.put(LTIService.LTI_ID_HISTORY,"content:1,content:2");
		oldContent.put(LTIService.LTI_SETTINGS, old_json.toString());
		oldContent.put(LTIService.LTI_ID, "4");

		String post = SakaiLTIUtil.trackResourceLinkID(oldContent);
		assertEquals(post, "content:1,content:2,content:4");

		Map<String, Object> newContent = new TreeMap<String, Object> ();
		JSONObject new_json = LTIUtil.parseJSONObject("");
		new_json.put(LTIService.LTI_ID_HISTORY,"content:2,content:3");
		newContent.put(LTIService.LTI_SETTINGS, new_json.toString());

		boolean retval = SakaiLTIUtil.trackResourceLinkID(newContent, oldContent);
		assertTrue(retval);

		post = (String) newContent.get(LTIService.LTI_SETTINGS);
		JSONObject post_json = LTIUtil.parseJSONObject(post);
		String post_history = (String) post_json.get(LTIService.LTI_ID_HISTORY);
		assertEquals(post_history, "content:1,content:2,content:3,content:4");

		// Verify no double add
		retval = SakaiLTIUtil.trackResourceLinkID(newContent, oldContent);
		assertFalse(retval);

		// Have an empty settings in the newContent item (typical use case);
		newContent.remove(LTIService.LTI_SETTINGS);
		retval = SakaiLTIUtil.trackResourceLinkID(newContent, oldContent);

		post = (String) newContent.get(LTIService.LTI_SETTINGS);
		post_json = LTIUtil.parseJSONObject(post);
		post_history = (String) post_json.get(LTIService.LTI_ID_HISTORY);
		assertEquals(post_history, "content:1,content:2,content:4");
	}

	// TODO: For now make sure this does not blow up - later check the actual output :)
	@Test
	public void testConvertRoleMapPropToMap() {
		String roleMap = "sakairole1:ltirole1,sakairole2:ltirole2";
		Map retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

        // * Using semicolon as the delimiter allows you to indicate more than one IMS role.
		roleMap = "sakairole4:ltirole4,ltirole5;sakairole6:ltirole6";
		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

		roleMap = "maintain:"+LTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN+ ";sakairole6:ltirole6";
		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

		// Semicolon at end
		roleMap = "maintain:"+LTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN+ ";sakairole6:ltirole6;";
		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

		// Semicolon at beginning
		roleMap = ";maintain:"+LTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN+ ";sakairole6:ltirole6";
		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

		// Many semicolon in the middle
		roleMap = "maintain:"+LTIConstants.MEMBERSHIP_ROLE_CONTEXT_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_SYSTEM_ADMIN +
                "," + LTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN+ ";;;;sakairole6:ltirole6";
		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(roleMap);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 2);

		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(null);
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 0);

		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap("");
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 0);

		retval = SakaiLTIUtil.convertOutboundRoleMapPropToMap(" ");
		assertTrue(retval instanceof Map);
		assertTrue(retval.size() == 0);
	}

	@Test
	public void testDefaultRoleMap() {
		Map<String, String> roleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(SakaiLTIUtil.LTI_OUTBOUND_ROLE_MAP_DEFAULT);

		assertTrue(roleMap instanceof Map);
		assertEquals(9, roleMap.size());
		assertTrue(roleMap.get("Yada") == null);
		assertEquals(roleMap.get("access"),      "Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
		assertEquals(roleMap.get("maintain"),    "Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
		assertEquals(roleMap.get("Student"),     "Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
		assertEquals(roleMap.get("Learner"),     "Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
		assertEquals(roleMap.get("Instructor"),  "Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
		// Blanks are really important below
		assertEquals(roleMap.get("Teaching Assistant"),    "TeachingAssistant,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant");
	}

	@Test
	public void testInboundRoleMap() {
		Map<String, String> legacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(SakaiLTIUtil.LTI_LEGACY_ROLE_MAP_DEFAULT);
		assertTrue(legacyMap instanceof Map);
		assertEquals(legacyMap.size(), 10);
		assertTrue(legacyMap.get("Yada") == null);

		Map<String, List<String>> roleMap = SakaiLTIUtil.convertInboundRoleMapPropToMap(SakaiLTIUtil.LTI_INBOUND_ROLE_MAP_DEFAULT);
		assertTrue(roleMap instanceof Map);
		assertEquals(roleMap.size(), 9);
		assertTrue(roleMap.get("Yada") == null);

		List<String> roleList = roleMap.get("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
		assertTrue(roleList.contains("Student"));
		assertTrue(roleList.contains("Learner"));
		assertTrue(roleList.contains("access"));

		roleList = roleMap.get(legacyMap.get("Learner"));
		assertTrue(roleList.contains("Student"));
		assertTrue(roleList.contains("Learner"));
		assertTrue(roleList.contains("access"));

		roleList = roleMap.get("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
		assertTrue(roleList.contains("Instructor"));
		assertTrue(roleList.contains("maintain"));

		roleList = roleMap.get(legacyMap.get("Instructor"));
		assertTrue(roleList.contains("Instructor"));
		assertTrue(roleList.contains("maintain"));

		roleList = roleMap.get("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant");
		assertTrue(roleList.contains("Teaching Assistant")); // The blank is really important
		assertTrue(roleList.contains("Instructor"));
		assertTrue(roleList.indexOf("Instructor") > roleList.indexOf("Teaching Assistant"));
		assertTrue(roleList.contains("maintain"));
		assertTrue(roleList.indexOf("maintain") > roleList.indexOf("Instructor"));

	}

	// Local so as not to call ServerConfigurationService
	public static String mapOutboundRole(String sakaiRole, String toolOutboundMapStr)
	{
		Map<String, String> propLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(
			"urn:lti:instrole:dude=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#Dude"
		);
		Map<String, String> defaultLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(SakaiLTIUtil.LTI_LEGACY_ROLE_MAP_DEFAULT);

		Map<String, String> toolRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(toolOutboundMapStr);

		Map<String, String> propRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(
			"Dude:Dude,http://purl.imsglobal.org/vocab/lis/v2/institution/person#Abides;"
			+ "Staff:Staff,Dude,http://purl.imsglobal.org/vocab/lis/v2/institution/person#Staff;"
		);
		Map<String, String> defaultRoleMap = SakaiLTIUtil.convertOutboundRoleMapPropToMap(SakaiLTIUtil.LTI_OUTBOUND_ROLE_MAP_DEFAULT);

		return SakaiLTIUtil.mapOutboundRole(sakaiRole, toolRoleMap, propRoleMap, defaultRoleMap, propLegacyMap, defaultLegacyMap);
	}

	// https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
	@Test
	public void testOutbound() {
		String toolProp = "ToolI:Instructor;ToolM:Instructor,Learner;ToolA:"+LTIConstants.MEMBERSHIP_ROLE_INSTITUTION_ADMIN+";";

		String imsRole = mapOutboundRole("maintain", toolProp);
		assertEquals("Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", imsRole);

		imsRole = mapOutboundRole("Instructor", toolProp);
		assertEquals("Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", imsRole);

		imsRole = mapOutboundRole("Baby Yoda", toolProp);
		assertTrue(imsRole == null);

		imsRole = mapOutboundRole("TeachingAssistant", toolProp);
		assertTrue(imsRole == null);

		imsRole = mapOutboundRole("Teaching Assistant", toolProp);
		assertEquals("TeachingAssistant,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant", imsRole);

		imsRole = mapOutboundRole("admin", toolProp);
		assertTrue(imsRole.contains("Instructor"));
		assertTrue(imsRole.contains("Administrator"));

		// Extra from properties
		imsRole = mapOutboundRole("Dude", toolProp);
		assertEquals("Dude,http://purl.imsglobal.org/vocab/lis/v2/institution/person#Abides", imsRole);

		// Tool maps to legacy Instructor - upconverted
		imsRole = mapOutboundRole("ToolI", toolProp);
		assertEquals("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", imsRole);

		// Tool maps to legacy admin - upconverted
		imsRole = mapOutboundRole("ToolA", toolProp);
		assertEquals("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator", imsRole);

		// Tool maps to legacy admin - upconverted
		imsRole = mapOutboundRole("ToolM", toolProp);
		assertEquals("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", imsRole);
	}

	// Local to avoid ServerConfiguration process
	public static String mapInboundRole(String incomingRoles, Set<String> siteRoles, String tenantInboundMapStr)
	{
		// Helps upgrade legacy roles like Instructor or urn:lti:sysrole:ims/lis/Administrator
		Map<String, String> propLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(
			"urn:lti:instrole:dude=http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#Dude"
		);
		Map<String, String> defaultLegacyMap = SakaiLTIUtil.convertLegacyRoleMapPropToMap(SakaiLTIUtil.LTI_LEGACY_ROLE_MAP_DEFAULT);

		Map<String, List<String>> tenantInboundMap = SakaiLTIUtil.convertInboundRoleMapPropToMap(tenantInboundMapStr);
		Map<String, List<String>> propInboundMap = null; // SakaiLTIUtil.convertInboundRoleMapPropToMap( ServerConfigurationService.getString(SakaiLTIUtil.LTI_INBOUND_ROLE_MAP));
		Map<String, List<String>> defaultInboundMap = SakaiLTIUtil.convertInboundRoleMapPropToMap(SakaiLTIUtil.LTI_INBOUND_ROLE_MAP_DEFAULT);

		return SakaiLTIUtil.mapInboundRole(incomingRoles, siteRoles, tenantInboundMap, propInboundMap, defaultInboundMap, propLegacyMap, defaultLegacyMap);
	}

	@Test
	public void testInbound() {

		String sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", projectRoles, null);
		assertEquals("maintain", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", projectRoles, null);
		assertEquals("access", sakaiRole);

		sakaiRole = mapInboundRole("urn:canvas:instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,urn:sakai:dude", projectRoles, null);
		assertEquals("maintain", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", projectRoles, null);
		assertEquals("access", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", courseRoles, null);
		assertEquals("Instructor", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", courseRoles, null);
		assertEquals("Student", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor#TeachingAssistant", courseRoles, null);
		assertEquals("Teaching Assistant", sakaiRole);

		sakaiRole = mapInboundRole("urn:canvas:instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,urn:sakai:dude", courseRoles, null);
		assertEquals("Instructor", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", courseRoles, null);
		assertEquals("Student", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", ltiRoles, null);
		assertEquals("Instructor", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", ltiRoles, null);
		assertEquals("Learner", sakaiRole);

		sakaiRole = mapInboundRole("urn:canvas:instructor,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor,urn:sakai:dude", ltiRoles, null);
		assertEquals("Instructor", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", ltiRoles, null);
		assertEquals("Learner", sakaiRole);

		sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner", ltiRoles, null);
		assertEquals("Learner", sakaiRole);

		// Context roles from https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
		for (String s : "ContentDeveloper,Instructor,Learner,Mentor,Manager,Member,Officer".split(",") ) {
			sakaiRole = mapInboundRole("http://purl.imsglobal.org/vocab/lis/v2/membership#" + s, ltiRoles, null);
			assertEquals(s, sakaiRole);
		}

		// We ignore institution roles from https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies
		for (String s : "Faculty,Guest,None,Other,Staff,Person,Student,Alumni,Observer,ProspectiveStudent".split(",") ) {
			String ltiRole = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#" + s;
			sakaiRole = mapInboundRole(ltiRole, ltiRoles, null);
			if ( sakaiRole != null ) log.warn("LTI Role [{}] should map to Ignore or null instead of [{}]", ltiRole, sakaiRole);
			assertTrue(sakaiRole==null);
		}
	}

	@Test
	public void testInception() {
		String imsRole;
		String sakaiRole;

		// Institutional roles - Just say no
		for (String roundTrip : "Faculty,Member,Alumni,ProspectiveStudent,Guest,Other".split(",") ) {
			imsRole = mapOutboundRole(roundTrip, null);
			assertTrue(imsRole==null);
		}

		// Institutional roles do not round trip - Faculty, Member, Staff, Alumni, ProspectiveStudent, Guest, Other
		for (String roundTrip : "Instructor,Learner,Teaching Assistant,Mentor".split(",") ) {
			imsRole = mapOutboundRole(roundTrip, null);
			sakaiRole = mapInboundRole(imsRole, ltiRoles, null);
			assertEquals(roundTrip, sakaiRole);
			imsRole = mapOutboundRole(sakaiRole, null);
			sakaiRole = mapInboundRole(imsRole, ltiRoles, null);
			assertEquals(roundTrip, sakaiRole);
		}

		for (String roundTrip : "Instructor,Student,Teaching Assistant".split(",") ) {
			imsRole = mapOutboundRole(roundTrip, null);
			sakaiRole = mapInboundRole(imsRole, courseRoles, null);
			assertEquals(roundTrip, sakaiRole);
			imsRole = mapOutboundRole(sakaiRole, null);
			sakaiRole = mapInboundRole(imsRole, courseRoles, null);
			assertEquals(roundTrip, sakaiRole);
		}

		for (String roundTrip : "access,maintain".split(",") ) {
			imsRole = mapOutboundRole(roundTrip, null);
			sakaiRole = mapInboundRole(imsRole, projectRoles, null);
			assertEquals(roundTrip, sakaiRole);
			imsRole = mapOutboundRole(sakaiRole, null);
			sakaiRole = mapInboundRole(imsRole, projectRoles, null);
			assertEquals(roundTrip, sakaiRole);
		}
	}

	public String compileJavaScript(String extraJS) {
		long count = extraJS.chars().filter(ch -> ch == '{').count();
		long count2 = extraJS.codePoints().filter(ch -> ch == '}').count();
		if ( count != count2 ) {
			System.out.println(extraJS);
			return "{} mismatch";
		}
		count = extraJS.chars().filter(ch -> ch == '(').count();
		count2 = extraJS.codePoints().filter(ch -> ch == '(').count();
		if ( count != count2 ) {
			System.out.println(extraJS);
			return "() mismatch";
		}
		count = extraJS.chars().filter(ch -> ch == '"').count();
		assertEquals(count % 2, 0);
		if ( count % 2 != 0 ) {
			System.out.println(extraJS);
			return " \" mismatch";
		}
		count = extraJS.chars().filter(ch -> ch == '\'').count();
		if ( count % 2 != 0 ) {
			System.out.println(extraJS);
			return " ' mismatch";
		}
		return "success";
	}

	@Test
	public void testFormPost() {
		boolean autosubmit = true;
		String submit_form_id = "42";
		String extraJS = SakaiLTIUtil.getLaunchJavaScript(submit_form_id, autosubmit);
		assertTrue(extraJS.contains("document.getElementById"));
		assertEquals(compileJavaScript(extraJS), "success");

		autosubmit = false;
		extraJS = SakaiLTIUtil.getLaunchJavaScript(submit_form_id, autosubmit);
		assertFalse(extraJS.contains("document.getElementById"));
		assertEquals(compileJavaScript(extraJS), "success");

		String launch_url = "https://www.tsugicloud.org/lti/store";
		String jws = "IAMJWS";
		String ljs = "{ \"key\": \"Value\"} ";
		String state = "42";
		String launch_error = "Dude abides";

		boolean dodebug = false;
		String form = SakaiLTIUtil.getJwsHTMLForm(launch_url, "id_token", jws, ljs, state, launch_error, dodebug);
		assertEquals(compileJavaScript(form), "success");
		assertTrue(form.contains("document.getElementById"));

		dodebug = true;
		form = SakaiLTIUtil.getJwsHTMLForm(launch_url, "id_token", jws, ljs, state, launch_error, dodebug);
		assertEquals(compileJavaScript(form), "success");
		assertFalse(form.contains("document.getElementById"));
	}

	@Test
	public void testGetNewpage() {
		Map<String, Object> tool = new HashMap();
		Map<String, Object> content = new HashMap();

		// Run default tests
		boolean retval = SakaiLTIUtil.getNewpage(null, null, true);
		assertEquals(retval, true);
		retval = SakaiLTIUtil.getNewpage(null, null, false);
		assertEquals(retval, false);

		// No data means default comes through
		retval = SakaiLTIUtil.getNewpage(tool, content, true);
		assertEquals(retval, true);
		retval = SakaiLTIUtil.getNewpage(tool, content, false);
		assertEquals(retval, false);

		// Only content
		content.put(LTIService.LTI_NEWPAGE, 0L);
		retval = SakaiLTIUtil.getNewpage(tool, content, true);
		assertEquals(retval, false);
		content.put(LTIService.LTI_NEWPAGE, 1L);
		retval = SakaiLTIUtil.getNewpage(tool, content, false);
		assertEquals(retval, true);

		// Tool wins
		tool.put(LTIService.LTI_NEWPAGE, LTIService.LTI_TOOL_NEWPAGE_OFF);
		retval = SakaiLTIUtil.getNewpage(tool, content, false);
		assertEquals(retval, false);

		tool.put(LTIService.LTI_NEWPAGE, Long.valueOf(LTIService.LTI_TOOL_NEWPAGE_ON));
		retval = SakaiLTIUtil.getNewpage(tool, content, false);
		assertEquals(retval, true);

		// Let content win
		tool.put(LTIService.LTI_NEWPAGE, Long.valueOf(LTIService.LTI_TOOL_NEWPAGE_CONTENT));
		retval = SakaiLTIUtil.getNewpage(tool, content, false);
		assertEquals(retval, true);
		content.put(LTIService.LTI_NEWPAGE, 0L);
		retval = SakaiLTIUtil.getNewpage(tool, content, true);
		assertEquals(retval, false);
	}

	@Test
	public void testGetFrameHeight() {
		Map<String, Object> tool = new HashMap();
		Map<String, Object> content = new HashMap();
		String retval = SakaiLTIUtil.getFrameHeight(null, null, "1200px");
		assertEquals(retval, "1200px");

		// Both are empty
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "1200px");

		content.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "42px");

		// Tool is empty
		content.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "42px");

		// Strings work as well - just in case
		content.put(LTIService.LTI_FRAMEHEIGHT, "44");
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "44px");

		tool.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(100));
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "44px");

		// Content takes precedence 
		retval = SakaiLTIUtil.getFrameHeight(tool, content, "1200px");
		assertEquals(retval, "44px");
	}

	@Test
	public void testArchiveMergeContent() {
		Document doc = Xml.createDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);

		Map<String, Object> content = new HashMap();
		content.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		content.put(LTIService.LTI_LAUNCH, "http://localhost:a-launch?x=42");
		content.put(LTIService.LTI_TITLE, "An LTI title");
		content.put(LTIService.LTI_DESCRIPTION, "An LTI DESCRIPTION");
		content.put(LTIService.LTI_NEWPAGE, 1L);
		content.put(LTIService.LTI_TOOL_ID, 42L); // Should not come back

		Element element = SakaiLTIUtil.archiveContent(doc, content, null);
		root.appendChild(element);
		String xmlOut = Xml.writeDocumentToString(doc);
		assertEquals(xmlOut, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><sakai-lti-content><title>An LTI title</title><description>An LTI DESCRIPTION</description><frameheight>42</frameheight><newpage>1</newpage><launch>http://localhost:a-launch?x=42</launch></sakai-lti-content></root>");

		Map<String, Object> content2  = new HashMap();
		SakaiLTIUtil.mergeContent(element, content2, null);
		assertNotEquals(content, content2);
		content.remove(LTIService.LTI_TOOL_ID);
		assertEquals(content, content2);
	}

	@Test
	public void testArchiveMergeTool() {
		Document doc = Xml.createDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);

		Map<String, Object> tool = new HashMap();
		tool.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		tool.put(LTIService.LTI13, Long.valueOf(LTIService.LTI13_LTI13));
		tool.put(LTIService.LTI_LAUNCH, "http://localhost:a-launch?x=42");
		tool.put(LTIService.LTI_TITLE, "An LTI title");
		tool.put(LTIService.LTI_DESCRIPTION, "An LTI DESCRIPTION");
		tool.put(LTIService.LTI_NEWPAGE, 1L);

		tool.put(LTIService.LTI_SENDNAME, "please");  // Wil export (bad data) will not import
		tool.put(LTIService.LTI_SECRET, "verysecure");  // Should not come back - Not archived
		tool.put(LTIService.LTI_CONSUMERKEY, "key12345");  // Should not come back - Not archived

		Element element = SakaiLTIUtil.archiveTool(doc, tool);
		root.appendChild(element);
		String xmlOut = Xml.writeDocumentToString(doc);
		assertEquals(xmlOut, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><sakai-lti-tool><title>An LTI title</title><description>An LTI DESCRIPTION</description><launch>http://localhost:a-launch?x=42</launch><newpage>1</newpage><frameheight>42</frameheight><sendname>please</sendname><lti13>1</lti13><sakai_tool_checksum>85RP91sSzHqtxBIkTrknaShQlL52r0JLOfKvaLJT4Gc=</sakai_tool_checksum></sakai-lti-tool></root>");

		Map<String, Object> tool2 = new HashMap();
		SakaiLTIUtil.mergeTool(element, tool2);
		assertNotEquals(tool, tool2);
		assertEquals((String) (tool2.get(LTIService.SAKAI_TOOL_CHECKSUM)), "85RP91sSzHqtxBIkTrknaShQlL52r0JLOfKvaLJT4Gc=");
		tool.remove(LTIService.LTI_SECRET);
		assertNotEquals(tool, tool2);
		tool.remove(LTIService.LTI_CONSUMERKEY);
		assertNotEquals(tool, tool2);
		tool.remove(LTIService.LTI_SENDNAME);
		assertNotEquals(tool, tool2);
		tool2.remove(LTIService.SAKAI_TOOL_CHECKSUM);
		assertTrue(tool.equals(tool2));
		assertEquals(tool, tool2);
	}

	public void mapDump(String header, Map<String, Object> dump)
	{
		System.out.println(header);
		for (String key : dump.keySet()) {
			Object o = dump.get(key);
		    System.out.println("Key: " + key + " Value: " + o + " " + o.getClass().getName());
		}
	}

	@Test
	public void testArchiveMergeContentTool() {
		Document doc = Xml.createDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);

		Map<String, Object> content = new HashMap();
		content.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		content.put(LTIService.LTI_LAUNCH, "http://localhost:a-launch?x=42");
		content.put(LTIService.LTI_TITLE, "An LTI title");
		content.put(LTIService.LTI_DESCRIPTION, "An LTI DESCRIPTION");
		content.put(LTIService.LTI_NEWPAGE, 1L);

		Map<String, Object> tool = new HashMap();
		tool.put(LTIService.LTI_FRAMEHEIGHT, Long.valueOf(42));
		tool.put(LTIService.LTI13, Long.valueOf(LTIService.LTI13_LTI13));
		tool.put(LTIService.LTI_LAUNCH, "http://localhost:a-launch?x=42");
		tool.put(LTIService.LTI_TITLE, "An LTI title");
		tool.put(LTIService.LTI_DESCRIPTION, "An LTI DESCRIPTION");
		tool.put(LTIService.LTI_NEWPAGE, 1L);
		tool.put(LTIService.LTI_CONSUMERKEY, "key12345");  // Should not come back - Not archived
		tool.put(LTIService.LTI_SECRET, "sec12345");  // Should not come back - Not archived

		Element element = SakaiLTIUtil.archiveContent(doc, content, tool);
		root.appendChild(element);
		String xmlOut = Xml.writeDocumentToString(doc);
		assertEquals(xmlOut, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><sakai-lti-content><title>An LTI title</title><description>An LTI DESCRIPTION</description><frameheight>42</frameheight><newpage>1</newpage><launch>http://localhost:a-launch?x=42</launch><sakai-lti-tool><title>An LTI title</title><description>An LTI DESCRIPTION</description><launch>http://localhost:a-launch?x=42</launch><newpage>1</newpage><frameheight>42</frameheight><lti13>1</lti13><sakai_tool_checksum>ASRHSVgrhBjDbhkLEZpWa/IueI9FwFwTxSjnU5uNCB0=</sakai_tool_checksum></sakai-lti-tool></sakai-lti-content></root>");

		Map<String, Object> content2  = new HashMap();
		Map<String, Object> tool2  = new HashMap();
		SakaiLTIUtil.mergeContent(element, content2, tool2);
		assertEquals(content, content2);
		assertNotEquals(tool, tool2);
		tool.remove(LTIService.LTI_CONSUMERKEY);
		tool.remove(LTIService.LTI_SECRET);
		tool2.remove(LTIService.SAKAI_TOOL_CHECKSUM);
		assertEquals(tool, tool2);
	}

	@Test
	public void testToolCheckSum() {
		String test = SakaiLTIUtil.computeToolCheckSum(null);
		assertEquals(test, null);

		Map<String, Object> tool = new HashMap();
		test = SakaiLTIUtil.computeToolCheckSum(tool);
		assertEquals(test, null);
		tool.put(LTIService.LTI_SECRET, "sec12345");
		tool.put(LTIService.LTI_LAUNCH, "http://localhost:a-launch?x=42");
		tool.put(LTIService.LTI_TITLE, "An LTI title");
		tool.put(LTIService.LTI_DESCRIPTION, "An LTI DESCRIPTION");
		tool.put(LTIService.LTI_NEWPAGE, 1L);

		test = SakaiLTIUtil.computeToolCheckSum(tool);
		assertEquals(test, null);

		tool.put(LTIService.LTI_CONSUMERKEY, "key12345");

		test = SakaiLTIUtil.computeToolCheckSum(tool);
		assertEquals(test, "ASRHSVgrhBjDbhkLEZpWa/IueI9FwFwTxSjnU5uNCB0=");
	}

	@Test
	public void testLTIUrls() {
		String launchUrl = SakaiLTIUtil.getContentLaunch(null);
		assertEquals(launchUrl, null);
		launchUrl = SakaiLTIUtil.getToolLaunch(null, null);
		assertEquals(launchUrl, null);
		launchUrl = SakaiLTIUtil.getToolLaunch(null, "siteid-was-here");
		assertEquals(launchUrl, null);

		Map<String, Object> tool = new HashMap();
		tool.put(LTIService.LTI_ID, Long.valueOf(42));
		launchUrl = SakaiLTIUtil.getToolLaunch(tool, "siteid-was-here");
		assertEquals(launchUrl, "/access/lti/site/siteid-was-here/tool:42");

		Map<String, Object> content = new HashMap();
		content.put(LTIService.LTI_ID, Long.valueOf(43));
		launchUrl = SakaiLTIUtil.getContentLaunch(content);
		assertEquals(launchUrl, null);
		content.put(LTIService.LTI_SITE_ID, "siteid-was-here");
		launchUrl = SakaiLTIUtil.getContentLaunch(content);
		assertEquals(launchUrl, "/access/lti/site/siteid-was-here/content:43");

		Long contentKey = SakaiLTIUtil.getContentKeyFromLaunch(launchUrl);
		assertEquals(contentKey, Long.valueOf(43));
	}

	@Test
	public void testExtractLtiLaunchUrls() {
		// Test various URL patterns
		String html = "Here are some URLs:\n" +
			"http://localhost:8080/access/lti/site/abc-123/content:1 \n" +
			"https://localhost:8080/access/lti/site/def-456/content:2 \n" +
			"http://localhost:8080/access/blti/site/ghi-789/content:3 \n" +
			"https://localhost:8080/access/blti/site/jkl-012/content:4 \n" +
			"This is not a valid URL: http://localhost:8080/access/other/site/mno-345/content:5";

		List<String> urls = SakaiLTIUtil.extractLtiLaunchUrls(html);
		assertEquals(4, urls.size());
		assertEquals("http://localhost:8080/access/lti/site/abc-123/content:1", urls.get(0));
		assertEquals("https://localhost:8080/access/lti/site/def-456/content:2", urls.get(1));
		assertEquals("http://localhost:8080/access/blti/site/ghi-789/content:3", urls.get(2));
		assertEquals("https://localhost:8080/access/blti/site/jkl-012/content:4", urls.get(3));

		// Test with no matching URLs
		String html2 = "No matching URLs here: http://example.com http://test.com/lti";
		List<String> urls2 = SakaiLTIUtil.extractLtiLaunchUrls(html2);
		assertEquals(0, urls2.size());

		// Test with null input
		List<String> urls3 = SakaiLTIUtil.extractLtiLaunchUrls(null);
		assertEquals(0, urls3.size());

		// Test with empty string
		List<String> urls4 = SakaiLTIUtil.extractLtiLaunchUrls("");
		assertEquals(0, urls4.size());

		// Test URLs embedded in HTML
		String html5 = "<p>Some text</p><p><a href=\"https://localhost:8080/access/blti/site/mno-345/content:6\">Link</a></p>" +
			"<p><a href=\"http://localhost:8080/access/lti/site/pqr-678/content:7\">Another Link</a></p>";
		List<String> urls5 = SakaiLTIUtil.extractLtiLaunchUrls(html5);
		assertEquals(2, urls5.size());
		assertEquals("https://localhost:8080/access/blti/site/mno-345/content:6", urls5.get(0));
		assertEquals("http://localhost:8080/access/lti/site/pqr-678/content:7", urls5.get(1));
	}

	@Test
	public void testGetContentKeyAndSiteId() {
		// Test standard LTI URL
		String html1 = "http://localhost:8080/access/lti/site/7d529bf7/content:1";
		String[] result1 = SakaiLTIUtil.getContentKeyAndSiteId(html1);
		assertNotNull(result1);
		assertEquals("7d529bf7", result1[0]);
		assertEquals("1", result1[1]);

		// Test BLTI URL
		String html2 = "https://localhost:8080/access/blti/site/abc123def/content:42";
		String[] result2 = SakaiLTIUtil.getContentKeyAndSiteId(html2);
		assertNotNull(result2);
		assertEquals("abc123def", result2[0]);
		assertEquals("42", result2[1]);

		// Test URL embedded in HTML
		String html3 = "<p><a href=\"https://localhost:8080/access/lti/site/xyz789/content:99\">Link</a></p>";
		String[] result3 = SakaiLTIUtil.getContentKeyAndSiteId(html3);
		assertNotNull(result3);
		assertEquals("xyz789", result3[0]);
		assertEquals("99", result3[1]);

		// Test invalid URL
		String html4 = "http://localhost:8080/access/other/site/invalid/content:1";
		String[] result4 = SakaiLTIUtil.getContentKeyAndSiteId(html4);
		assertNull(result4);

		// Test null input
		String[] result5 = SakaiLTIUtil.getContentKeyAndSiteId(null);
		assertNull(result5);

		// Test empty string
		String[] result6 = SakaiLTIUtil.getContentKeyAndSiteId("");
		assertNull(result6);
	}
}



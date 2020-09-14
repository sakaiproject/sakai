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
package org.sakaiproject.basiclti.util;

import static org.junit.Assert.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import java.util.TreeMap;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

@Slf4j
public class SakaiBLTIUtilTest {

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
			adj = SakaiBLTIUtil.adjustCustom(s);
			assertEquals(s, adj);
		}
			
		adj = SakaiBLTIUtil.adjustCustom("x=1;y=2;z=3");
		assertEquals(adj,"x=1;y=2;z=3".replace(';','\n'));
		adj = SakaiBLTIUtil.adjustCustom("x=1;y=2;z=3;");
		assertEquals(adj,"x=1;y=2;z=3;".replace(';','\n'));
	}
	@Test
	public void testStringGrade() {
		String grade="";
		try {
			grade = SakaiBLTIUtil.getRoundedGrade(0.57,100.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}
			
		assertEquals(grade,"57.0");

		try {
			grade = SakaiBLTIUtil.getRoundedGrade(0.5655,100.0);
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
		String encrypt1 = SakaiBLTIUtil.encryptSecret(plain, key);
		assertFalse(plain.equals(encrypt1));
		assertTrue(goodEncrypt(encrypt1));
System.err.println("encrypt1="+encrypt1);
		// No double encrypt
		String encrypt2 = SakaiBLTIUtil.encryptSecret(encrypt1, key);
		assertTrue(goodEncrypt(encrypt2));
		assertEquals(encrypt1, encrypt2);
		String decrypt = SakaiBLTIUtil.decryptSecret(encrypt2, key);
		assertEquals(plain, decrypt);
	}

	@Test
	public void testLaunchCodes() {
		Map<String, Object> content = new TreeMap<String, Object>();
		content.put(LTIService.LTI_ID, "42");
		content.put(LTIService.LTI_PLACEMENTSECRET, "xyzzy");

		String launch_code_key = SakaiBLTIUtil.getLaunchCodeKey(content);
		assertEquals(launch_code_key,"launch_code:42");

		String launch_code = SakaiBLTIUtil.getLaunchCode(content);
		assertTrue(SakaiBLTIUtil.checkLaunchCode(content, launch_code));

		content.put(LTIService.LTI_PLACEMENTSECRET, "wrong");
		assertFalse(SakaiBLTIUtil.checkLaunchCode(content, launch_code));
	}

	@Test
	public void testConvertLong() {
		Long l = SakaiBLTIUtil.getLongNull(new Long(2));
		assertEquals(l, new Long(2));
		l = SakaiBLTIUtil.getLongNull(new Double(2.2));
		assertEquals(l, new Long(2));
		l = SakaiBLTIUtil.getLongNull(null);
		assertEquals(l, null);
		l = SakaiBLTIUtil.getLongNull("fred");
		assertEquals(l, null);
		l = SakaiBLTIUtil.getLongNull("null");
		assertEquals(l, null);
		l = SakaiBLTIUtil.getLongNull("NULL");
		assertEquals(l, null);
		// This one is a little weird but it is how it was written - double is different
		l = SakaiBLTIUtil.getLongNull("");
		assertEquals(l, new Long(-1));
		l = SakaiBLTIUtil.getLongNull("2");
		assertEquals(l, new Long(2));
		l = SakaiBLTIUtil.getLongNull("2.5");
		assertEquals(l, null);
		l = SakaiBLTIUtil.getLongNull(new Float(3.1));
		assertEquals(l, new Long(3));
		// Casting truncates
		l = SakaiBLTIUtil.getLongNull(new Float(3.9));
		assertEquals(l, new Long(3));
		l = SakaiBLTIUtil.getLongNull(new Integer(3));
		assertEquals(l, new Long(3));
	}

	@Test
	public void testConvertDouble() {
		Double d = SakaiBLTIUtil.getDoubleNull(new Double(2.0));
		assertEquals(d, new Double(2.0));
		d = SakaiBLTIUtil.getDoubleNull(new Double(2.5));
		assertEquals(d, new Double(2.5));
		d = SakaiBLTIUtil.getDoubleNull(null);
		assertEquals(d, null);
		d = SakaiBLTIUtil.getDoubleNull("fred");
		assertEquals(d, null);
		d = SakaiBLTIUtil.getDoubleNull("null");
		assertEquals(d, null);
		d = SakaiBLTIUtil.getDoubleNull("NULL");
		assertEquals(d, null);
		d = SakaiBLTIUtil.getDoubleNull("");
		assertEquals(d, null);
		d = SakaiBLTIUtil.getDoubleNull("2.0");
		assertEquals(d, new Double(2.0));
		d = SakaiBLTIUtil.getDoubleNull("2.5");
		assertEquals(d, new Double(2.5));
		d = SakaiBLTIUtil.getDoubleNull("2");
		assertEquals(d, new Double(2.0));
		d = SakaiBLTIUtil.getDoubleNull(new Long(3));
		assertEquals(d, new Double(3.0));
		d = SakaiBLTIUtil.getDoubleNull(new Integer(3));
		assertEquals(d, new Double(3.0));
	}

}


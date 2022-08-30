/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Test;

/** 
 ** JUnit PasswordCheck test cases
 **/
public class PasswordCheckTest {

	/* for checking strength */
	private String passwordVeryWeak = "a";
	private String passwordWeak = "test";
	private String passwordMediocre = "IloveT3sts";
	private String passwordStrong = "IloveT3sts!";
	private String passwordVeryStrong = "I_r3a11y_loveT3sts!";
	
	/* for checking length */
	private String passwordShort = "test";
	private String passwordLong = "098f6bcd4621d373cade4e832627b4f6";
	private int zero=0;
	private int six=6;
	private int eight=8;
	private int thirty=30;
	
	/* general empty string */
	private String passwordEmpty = "";

	@Test
	public void testLengthPasswordZeroToThirty() {
		Assert.assertTrue(PasswordCheck.isAcceptableLength(passwordShort, zero, thirty));
	}

	@Test
	public void testLengthLongPasswordZeroToEight() {
		Assert.assertFalse(PasswordCheck.isAcceptableLength(passwordLong, zero, eight));
	}

	@Test
	public void testLengthShortPasswordSixToEight() {
		Assert.assertFalse(PasswordCheck.isAcceptableLength(passwordShort, six, eight));
	}

	@Test
	public void testLengthLongPasswordSixToEight() {
		Assert.assertFalse(PasswordCheck.isAcceptableLength(passwordLong, six, eight));
	}

	@Test
	public void testLengthEmptyPassword() {
		Assert.assertFalse(PasswordCheck.isAcceptableLength(passwordEmpty, zero, eight));
	}

	@Test
	public void testStrengthVeryWeak() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordVeryWeak), PasswordCheck.VERY_WEAK);
	}

	@Test
	public void testStrengthWeak() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordWeak), PasswordCheck.VERY_WEAK);
	}

	@Test
	public void testStrengthMediocre() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordMediocre), PasswordCheck.MEDIOCRE);
	}		

	@Test
	public void testStrengthStrong() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordStrong), PasswordCheck.STRONG);
	}

	@Test
	public void testStrengthVeryStrong() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordVeryStrong), PasswordCheck.VERY_STRONG);
	}

	@Test
	public void testStrengthNone() {
		Assert.assertEquals(PasswordCheck.getPasswordStrength(passwordEmpty), PasswordCheck.NONE);
	}	
}

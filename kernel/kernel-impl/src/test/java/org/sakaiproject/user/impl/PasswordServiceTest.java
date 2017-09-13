/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.user.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.user.impl.PasswordService;

/**
 * Test the new password code.
 * Needs to be in this package as it uses package visibilty methods.
 * @author buckett
 *
 */
public class PasswordServiceTest {
	
	PasswordService pwdService;

	@Before
	public void setUp() throws Exception {
		pwdService = new PasswordService();
	}

	@Test
	public void testGoodPassword() {
		Assert.assertTrue(pwdService.check("ThisPasswordIsGood",
			"PBKDF2:5RrskmenJ8ZwPeQvMqCMSw==:ZOF8+Y17j9SQy9yNG0IZ5w=="));
	}

	@Test
	public void testEncrpt() {
		Assert.assertTrue(pwdService.check("password", pwdService.encrypt("password")));
	}

	@Test
	public void testEncryptOk() {
		// Check salting is working.
		Assert.assertNotSame(pwdService.encrypt("admin"), pwdService.encrypt("admin"));
	}

	@Test
	public void testEncryptFail() {
		Assert.assertFalse(pwdService.check("admin", "doesn't match"));
	}

	@Test
	public void testOldPassword() {
		// Test of old password.
		Assert.assertTrue(pwdService.check("admin", "ISMvKXpXpadDiUoOSoAf"));
		Assert.assertTrue(pwdService.check("admin", "ISMvKXpXpadDiUoOSoAfww=="));
	}

	@Test
	public void testMigratedPasswords(){
		// Test of migrated passwords
		Assert.assertTrue(pwdService.check("admin", "MD5-SALT-SHA256:W2vRdA==:8QkvjZDLkqy5RoQUkRfOTG+C2FEhuq4sQyNxP7XKCvg=")); // MD5 admin password migrated.
		Assert.assertTrue(pwdService.check("admin", "MD5TRUNC-SALT-SHA256:pgO3lQ==:KRWu18xxI1fJPeULNQeBUyL4FN3YMBShtkjf3PW4sSk=")); // MD5 trunc admin password migrated.
	}

	@Test
	public void testRoundTripMigrated() {
		// Round trip migration
		Assert.assertTrue(pwdService.check("admin", PasswordService.MD5_SALT_SHA256 + pwdService.oldEncrpt("ISMvKXpXpadDiUoOSoAfww==")));
		Assert.assertTrue(pwdService.check("admin", PasswordService.MD5TRUNC_SALT_SHA256 + pwdService.oldEncrpt("ISMvKXpXpadDiUoOSoAf")));
		Assert.assertFalse(pwdService.check("admin", PasswordService.MD5_SALT_SHA256 + pwdService.oldEncrpt("Doesn't match.")));
		Assert.assertFalse(pwdService.check("admin", PasswordService.MD5TRUNC_SALT_SHA256 + pwdService.oldEncrpt("Not the same")));
	}

	@Test
	public void testUnsaltedSHA256() {
		// Test of unsalted passwords (we don't create these).
		Assert.assertTrue(pwdService.check("secret", pwdService.hash("secret", "SHA-256")));
		Assert.assertFalse(pwdService.check("secret", pwdService.hash("different Secret", "SHA-256")));
	}

	@Test
	public void testCheckCharacterRange() {
		// Build the string or strange characters.
		StringBuilder password = new StringBuilder(10000);
		for (char ch = 0; ch < 10000; ch++) {
			password.append(ch);
		}
		String encrypted = pwdService.encrypt(password.toString());
		Assert.assertTrue(pwdService.check(password.toString(), encrypted));
	}

}

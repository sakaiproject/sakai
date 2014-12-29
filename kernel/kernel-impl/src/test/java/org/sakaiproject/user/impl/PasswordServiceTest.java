package org.sakaiproject.user.impl;

import org.sakaiproject.user.impl.PasswordService;

import junit.framework.TestCase;

/**
 * Test the new password code.
 * Needs to be in this package as it uses package visibilty methods.
 * @author buckett
 *
 */
public class PasswordServiceTest extends TestCase {
	
	PasswordService pwdService;
	
	public void setUp() throws Exception {
		super.setUp();
		pwdService = new PasswordService();
	}

	public void testEncryptOk() {
		// Check salting is working.
		assertNotSame(pwdService.encrypt("admin"), pwdService.encrypt("admin"));
	}

	public void testEncryptFail() {
		assertFalse(pwdService.check("admin", "doesn't match"));
	}

	public void testMigratedPassword() {
		// Test of old password.
		assertTrue(pwdService.check("admin", "ISMvKXpXpadDiUoOSoAf"));
		assertTrue(pwdService.check("admin", "ISMvKXpXpadDiUoOSoAfww=="));
		
		// Test of migrated passwords
		assertTrue(pwdService.check("admin", "MD5-SALT-SHA256:W2vRdA==:8QkvjZDLkqy5RoQUkRfOTG+C2FEhuq4sQyNxP7XKCvg=")); // MD5 admin password migrated.
		assertTrue(pwdService.check("admin", "MD5TRUNC-SALT-SHA256:pgO3lQ==:KRWu18xxI1fJPeULNQeBUyL4FN3YMBShtkjf3PW4sSk=")); // MD5 trunc admin password migrated.
	}

	public void testRoundTripMigrated() {
		// Round trip migration
		assertTrue(pwdService.check("admin", PasswordService.MD5_SALT_SHA256 + pwdService.encrypt("ISMvKXpXpadDiUoOSoAfww==")));
		assertTrue(pwdService.check("admin", PasswordService.MD5TRUNC_SALT_SHA256 + pwdService.encrypt("ISMvKXpXpadDiUoOSoAf")));
		assertFalse(pwdService.check("admin", PasswordService.MD5_SALT_SHA256 + pwdService.encrypt("Doesn't match.")));
		assertFalse(pwdService.check("admin", PasswordService.MD5TRUNC_SALT_SHA256 + pwdService.encrypt("Not the same")));
	}

	public void testUnsaltedSHA256() {
		// Test of unsalted passwords (we don't create these).
		assertTrue(pwdService.check("secret", pwdService.hash("secret", "SHA-256")));
		assertFalse(pwdService.check("secret", pwdService.hash("different Secret", "SHA-256")));
	}

	public void testCheckCharacterRange() {
		// Build the string or strange characters.
		StringBuilder password = new StringBuilder(10000);
		for (char ch = 0; ch < 10000; ch++) {
			password.append(ch);
		}
		String encrypted = pwdService.encrypt(password.toString());
		assertTrue(pwdService.check(password.toString(), encrypted));
	}

}

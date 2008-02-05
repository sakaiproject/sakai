package edu.amc.sakai.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RegexpBlacklistEidValidatorTest extends TestCase {

	private RegexpBlacklistEidValidator validator;

	protected void setUp() throws Exception {
		validator = new RegexpBlacklistEidValidator();
		super.setUp();
	}
	
	public void testInvalidatesNullOrWhitespaceEids() {
		Collection<String> regexps = new HashSet<String>(1);
		regexps.add("some-user-id");
		validator.setEidBlacklist(regexps); // ensure it's not refusing just b/c the blacklist is empty
		assertFalse(validator.isSearchableEid(null));
		assertFalse(validator.isSearchableEid(""));
		assertFalse(validator.isSearchableEid(" "));
	}
	
	public void testValidatesAnyNonNullNonWhitespaceEidIfNoBlacklistSpecified() {
		validator.setEidBlacklist(null); // ensure the blacklist is empty
		assertTrue(validator.isSearchableEid("some-eid"));
	}
	
	public void testInvalidatesEidsMatchesByBlacklist() {
		final String validEid = "valid-eid";
		final String invalidEid1 = "invalid-eid-1";
		final String invalidEid2 = "invalid-eid-2";
		final String invalidEid3 = "dangerous-eid";
		Collection<String> regexps = new HashSet<String>(2);
		regexps.add("^invalid-eid.*$");
		regexps.add("^dangerous.*$");
		validator.setEidBlacklist(regexps);
		assertTrue(validator.isSearchableEid(validEid));
		assertFalse(validator.isSearchableEid(invalidEid1));
		assertFalse(validator.isSearchableEid(invalidEid2));
		assertFalse(validator.isSearchableEid(invalidEid3));
	}
	
	public void testRespectsConfiguredPatternFlags() {
		final String validEid = "valid-eid";
		final String lowerCaseInvalidEid = "invalid-eid";
		final String upperCaseInvalidEid = lowerCaseInvalidEid.toUpperCase();
		Collection<String> regexps = new HashSet<String>(1);
		regexps.add("^invalid-eid$");
		validator.setRegexpFlags(Pattern.CASE_INSENSITIVE);
		validator.setEidBlacklist(regexps);
		assertTrue(validator.isSearchableEid(validEid));
		assertFalse(validator.isSearchableEid(lowerCaseInvalidEid));
		assertFalse(validator.isSearchableEid(upperCaseInvalidEid));
	}
	
	
}

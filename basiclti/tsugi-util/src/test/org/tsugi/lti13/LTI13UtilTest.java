package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.security.*;
import java.util.regex.Pattern;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.LTI11Transition;

import java.security.interfaces.RSAPublicKey;

// import org.bouncycastle.jce.provider.BouncyCastleProvider;
public class LTI13UtilTest {

	// Close to a base64 pattern
	Pattern base64_pattern = Pattern.compile("^[-A-Za-z0-9+/=]+$");

	Pattern upper_hex_pattern = Pattern.compile("^[A-F0-9]+$");
	Pattern lower_hex_pattern = Pattern.compile("^[a-f0-9]+$");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRSAFromString() throws
			NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException {
		String serialized = "-----BEGIN PUBLIC KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgviDRUN1Z6hIOBg5uj1k\n"
				+ "KSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l/gnZ0vIaCuf4G9mkphc/y\n"
				+ "V0cgFY65wQmecPxv3IZ77wbJ+g5lL5vuCVTbh55nD++cj/hSBznXecQTXQNV9d51r\n"
				+ "Ca65+PQ+YL1oRnrpUuLNPbdnc8kT/ZUq5Ic0WJM+NprN1tbbn2LafBY+igqbRQVox\n"
				+ "It75B8cd+35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNv\n"
				+ "w3ykMoVh1pq7zwL4P0IHXevvPnja+PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTwIDAQ\n"
				+ "AB\n"
				+ "-----END PUBLIC KEY-----\n";

		Key publicKey = LTI13Util.string2PublicKey(serialized);
		// Make sure casting works
		RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;
		String newSer = LTI13Util.getPublicEncoded(rsaPublic);
		if ( ! newSer.equals(serialized) ) {
			System.out.println("New serialized key");
			System.out.println(newSer);
		}
		assertEquals(serialized, newSer);
	}

	@Test
	public void testNullAndBlank() {
		Key x = LTI13Util.string2PublicKey(null);
		x = LTI13Util.string2PublicKey("");
		assertNull(x);
		x = LTI13Util.string2PublicKey(" ");
		assertNull(x);
		x = LTI13Util.string2PublicKey("\n");
		assertNull(x);

		x = LTI13Util.string2PrivateKey(null);
		x = LTI13Util.string2PrivateKey("");
		assertNull(x);
		x = LTI13Util.string2PrivateKey(" ");
		assertNull(x);
		x = LTI13Util.string2PrivateKey("\n");
		assertNull(x);
	}

	@Test
	public void testSHA256() {
		String hash = LTI13Util.sha256("Yada");
		assertNotNull(hash);
		assertEquals("Imdd9E/bze9+h6T8tofJSwKbLKbiKSaX45BquGq8tNk=", hash);
	}

	@Test
	public void testHMACSHA256() {
		String secret = "my-lti11-secret";
		String message = "179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z";
		String hash = LTI13Util.compute_HMAC_SHA256(message, secret);
		assertNotNull(hash);
		assertEquals("lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps=", hash);
	}

	// https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	/*
		sign=base64(hmac_sha256(utf8bytes('179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z'), utf8bytes('my-lti11-secret')))

		{
			"nonce": "172we8671fd8z",
			"iat": 1551290796,
			"exp": 1551290856,
			"iss": "https://lmsvendor.com",
			"aud": "PM48OJSfGDTAzAo",
			"sub": "3",
			"https://purl.imsglobal.org/spec/lti/claim/deployment_id": "689302",
			"https://purl.imsglobal.org/spec/lti/claim/lti1p1": {
				"user_id": "34212",
				"oauth_consumer_key": "179248902",
				"oauth_consumer_key_sign": "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps="
			}
		}

	 */

	@Test
	public void testSignLTI11Transition() {
		LaunchJWT lj = new LaunchJWT();
		lj.nonce = "172we8671fd8z";
		lj.issued = 1551290796L;
		lj.expires = 1551290856L;
		lj.issuer = "https://lmsvendor.com";
		lj.audience = "PM48OJSfGDTAzAo";  // Client Id
		lj.subject = "3";
		lj.deployment_id = "689302";

		String oauth_consumer_key = "179248902";
		String secret = "my-lti11-secret";
		String expected = "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps=";

		String base = LTI13Util.getLTI11TransitionBase(lj);
		assertNull(base); // Incomplete

		String signature = LTI13Util.signLTI11Transition(lj, secret);
		assertNull(signature); // Incomplete

		lj.lti11_transition = new LTI11Transition();
		lj.lti11_transition.user_id = "34212";
		lj.lti11_transition.oauth_consumer_key = oauth_consumer_key;

		base = LTI13Util.getLTI11TransitionBase(lj);
		assertEquals(base, "179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z");

		signature = LTI13Util.signLTI11Transition(lj, secret);
		assertEquals(signature, "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps=");

		lj.lti11_transition.oauth_consumer_key_sign = signature;

		boolean check = LTI13Util.checkLTI11Transition(lj, oauth_consumer_key, secret);
		assertTrue(check);

		check = LTI13Util.checkLTI11Transition(lj, oauth_consumer_key, "badsecret");
		assertFalse(check);

		check = LTI13Util.checkLTI11Transition(lj, "badkey", secret);
		assertFalse(check);
	}
	@Test
	public void testSerializeKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair tokenKeyPair = keyGen.genKeyPair();
		String publicStr = LTI13Util.getPublicEncoded(tokenKeyPair);
		String privateStr = LTI13Util.getPrivateEncoded(tokenKeyPair);

		assertNotEquals(publicStr, privateStr);
		String publicB64 = LTI13Util.getPublicB64(tokenKeyPair);
		String privateB64 = LTI13Util.getPrivateB64(tokenKeyPair);
		assertNotEquals(publicB64, privateB64);

		assertNotEquals(publicB64, publicStr);
		assertNotEquals(privateB64, privateStr);

		// If you want a sample, B64 format public and private key,
		// uncomment these two lines, run the unit tests, and look at
		// ./target/surefire-reports/org.tsugi.lti13.LTI13UtilTest-output.txt
		// System.out.println("privateB64="+privateB64);
		// System.out.println("publicB64="+publicB64);
	}

	@Test
	public void testDeSerializeKeyPair() throws NoSuchAlgorithmException {
		String publicB64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3YjlvUlElXQ8/xYg1mOKpQLDySbdy0lY1GgxdHnwmOQeXAma6wRHH3Aiv9gGj8Hr2Lml75jjwzMiyeML9bx3sLIRJ8j835cHQjDZDPddIiQlfeLBLuyTeQVxW//L/EIaNsRrXNLiw1kmTcJ7XYtj6PSsWIm77KBPSoSPpoNfHaO2kPQL4mHOKi62Deovk7tRn1qGnbau6AEvy8/8x5q9oN79JxL9dT2O06pgH4BN+0PSly27W+0KlAnrH82oHjJX0dJNO1Slzl0iFiXpuXGNzrNRdLTWxqwkf2wnjd3p/9MbQuOq3bsCV2N5XO1+pK5kb2U6n9vMtdBocZcGHszUXwIDAQAB";

		String privateB64 = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDdiOW9SUSVdDz/FiDWY4qlAsPJJt3LSVjUaDF0efCY5B5cCZrrBEcfcCK/2AaPwevYuaXvmOPDMyLJ4wv1vHewshEnyPzflwdCMNkM910iJCV94sEu7JN5BXFb/8v8Qho2xGtc0uLDWSZNwntdi2Po9KxYibvsoE9KhI+mg18do7aQ9AviYc4qLrYN6i+Tu1GfWoadtq7oAS/Lz/zHmr2g3v0nEv11PY7TqmAfgE37Q9KXLbtb7QqUCesfzageMlfR0k07VKXOXSIWJem5cY3Os1F0tNbGrCR/bCeN3en/0xtC46rduwJXY3lc7X6krmRvZTqf28y10GhxlwYezNRfAgMBAAECggEAAvphJD9E5cFvRHqWrGsgJQG5gg28s4vj3s8bwxYUjumaIuCaLYicMCcAwJV12R7hPcOZIHRss2YPlKkxsvb6oOxz4JYKGDNejJw9frggj2HvAxc2Q5w4i0rwcC0rH9P/qDbRvqizFa8wHaF8qRBDopEPdA098nthNX48tB9V+v7/APsyvQVvmd9pJVPSNbwB0nVvZYpYGnejdn1CPlXv9DFKdQWevWnKj3wqy6Kv4GaiT3LU3MBCBXmkxQxL6kbT0+0hWm+L4bhh+EKTQUjX6itcYHGOc52va4VZ9jSe6W+Jt7ozJ7h5s9gO9T/LZXQKcvfEdFrjNPQTLilEAZstqQKBgQDx6W50n6gjoAFLBrQTZtw6tbeZNN2ItRWQ+E0NvLHgtEz1G4+EGbOwNnHtGBkkAeHbb+mr/vy44qs5Lg/JSMqIGa03NIeZmZlQ1tyepV2byktTYa1o3EJ3xvx1FU0NcZnmJohM6bLKGLHAYyyvDCAyJXOiEOaMDI0f3Mf8UFa+4wKBgQDqb6wHXD7cbfzi0qBOSLQ2c0vLtaLqGpY1491hpga1Xh2ccJzS2J+rrrwABFtkWt5qx5/K6GG2BSdp+GfeqsPlAM0//vwHere0AKdKibvXkiDuuwDGyGATPCKNsjjIMtnaX4EtKLv3D/sBh+sMQUE4rdcH0IeKg7I+7G8ZPpIxVQKBgD2x9NOVbEI5qlrCCObx0rS+Z2aRTvZwiGGCkdQxDYRrNz1DnCRqkaMdH40CGldOi5V1Qzj0POvCEzzv/GKvR8fSFJUBtpt7ytgNFBL5xZfXzcIkJR+bHPCDNRt/JTmKgV9q8FP9PfXAuoSARoMWKpffaVAp00JgaRV5Lx+wNY9VAoGAOqTYubafmvCAlBTwOOUC/6Pzz++N41XnQW/Qzz6C6xex/JJHCI9b0cySBscQPZhljvyEwnNDDg9X/rA++3poKn762Qll7lEKZyLKtmCh9pj9V/q7hP5W2jjuw9dTUPE3geLmI2/PJUyAE6/2Ykhokk8aPUAyx7mN+yOV3xDB7UkCgYB2+4MJpP5rd7NsLS+bY49etfcE38nywz2HQQMOw2spGnwAN4nsemh29sTu+ux9HQjLkhH1suCOfWazorgs9bWVZklyFqqEzvfRU8a9QIL4lF4PwTitvpJf/TPTZpl5a7SBW4q8UhbkVwoi+1RbSg49x3qVkJiFzsd6eWJTtqq7Pw==";

		String pubEncoded = LTI13Util.getPublicEncoded(publicB64);
		String privEncoded = LTI13Util.getPrivateEncoded(privateB64);
		assertNotNull(pubEncoded);
		assertNotNull(privEncoded);
		assertTrue(pubEncoded.startsWith("-----BEGIN "));
		assertTrue(privEncoded.startsWith("-----BEGIN "));

		// Don't double encode
		String pubEncoded2	= LTI13Util.getPublicEncoded(pubEncoded);
		String privEncoded2 = LTI13Util.getPrivateEncoded(privEncoded);

		assertEquals(pubEncoded, pubEncoded2);
		assertEquals(privEncoded, privEncoded2);

		Key pubKey = LTI13Util.string2PublicKey(pubEncoded);
		Key privKey = LTI13Util.string2PrivateKey(privEncoded);

		assertNotNull(pubKey);
		assertNotNull(privKey);

		KeyPair pair = LTI13Util.strings2KeyPair(publicB64, privateB64);
		assertNotNull(pair);

		String newPublicB64 = LTI13Util.getPublicB64(pair);
		String newPrivateB64 = LTI13Util.getPrivateB64(pair);

		assertEquals(publicB64, newPublicB64);
		assertEquals(privateB64, newPrivateB64);
	}

	@Test
	public void testToNull() {
		String d = LTI13Util.toNull("bob");
		assertEquals(d,"bob");
		d = LTI13Util.toNull("");
		assertNull(d);
		d = LTI13Util.toNull(null);
		assertNull(d);
	}

	@Test
	public void testConvertInt() {
		Integer d = LTI13Util.getInt(new Integer(2));
		assertEquals(d, new Integer(2));
		d = LTI13Util.getInt(new Double(2.5));
		assertEquals(d, new Integer(2));
		d = LTI13Util.getInt(null);
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("fred");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("null");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("NULL");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("2.0");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("2.5");
		assertEquals(d, new Integer(-1));
		d = LTI13Util.getInt("2");
		assertEquals(d, new Integer(2));
		d = LTI13Util.getInt(new Long(3));
		assertEquals(d, new Integer(3));
		d = LTI13Util.getInt(new Integer(3));
		assertEquals(d, new Integer(3));
	}

	@Test
	public void testConvertLong() {
		Long l = LTI13Util.getLongNull(new Long(2));
		assertEquals(l, new Long(2));
		l = LTI13Util.getLongNull(new Double(2.2));
		assertEquals(l, new Long(2));
		l = LTI13Util.getLongNull(null);
		assertEquals(l, null);
		l = LTI13Util.getLongNull("fred");
		assertEquals(l, null);
		l = LTI13Util.getLongNull("null");
		assertEquals(l, null);
		l = LTI13Util.getLongNull("NULL");
		assertEquals(l, null);
		// This one is a little weird but it is how it was written - double is different
		l = LTI13Util.getLongNull("");
		assertEquals(l, new Long(-1));
		l = LTI13Util.getLongNull("2");
		assertEquals(l, new Long(2));
		l = LTI13Util.getLongNull("2.5");
		assertEquals(l, null);
		l = LTI13Util.getLongNull(new Float(3.1));
		assertEquals(l, new Long(3));
		// Casting truncates
		l = LTI13Util.getLongNull(new Float(3.9));
		assertEquals(l, new Long(3));
		l = LTI13Util.getLongNull(new Integer(3));
		assertEquals(l, new Long(3));
	}

	@Test
	public void testConvertDouble() {
		Double d = LTI13Util.getDoubleNull(new Double(2.0));
		assertEquals(d, new Double(2.0));
		d = LTI13Util.getDoubleNull(new Double(2.5));
		assertEquals(d, new Double(2.5));
		d = LTI13Util.getDoubleNull(null);
		assertEquals(d, null);
		d = LTI13Util.getDoubleNull("fred");
		assertEquals(d, null);
		d = LTI13Util.getDoubleNull("null");
		assertEquals(d, null);
		d = LTI13Util.getDoubleNull("NULL");
		assertEquals(d, null);
		d = LTI13Util.getDoubleNull("");
		assertEquals(d, null);
		d = LTI13Util.getDoubleNull("2.0");
		assertEquals(d, new Double(2.0));
		d = LTI13Util.getDoubleNull("2.5");
		assertEquals(d, new Double(2.5));
		d = LTI13Util.getDoubleNull("2");
		assertEquals(d, new Double(2.0));
		d = LTI13Util.getDoubleNull(new Long(3));
		assertEquals(d, new Double(3.0));
		d = LTI13Util.getDoubleNull(new Integer(3));
		assertEquals(d, new Double(3.0));
	}

	@Test
	public void testTimeStamp() {
		String token = LTI13Util.timeStamp("xyzz");
		boolean good = LTI13Util.timeStampCheck(token, 100);
		assertTrue(good);
		token = "1"+token;
		good = LTI13Util.timeStampCheck(token, 100);
		assertFalse(good);
	}

	@Test
	public void testTimeStampSign() {
		String token = LTI13Util.timeStampSign("xyzzy", "secret");
		assertTrue(token.contains(":xyzzy:"));
		assertFalse(token.contains("secret"));
		boolean good = LTI13Util.timeStampCheckSign(token, "secret", 100);
		assertTrue(good);
		good = LTI13Util.timeStampCheckSign(token, "xsecret", 100);
		assertFalse(good);
		String token_2020 = "1607379725:xyzzy:CVHUHPEIfW2zTwMAnhxQprbZWvAr58V34NprcdMP4gE=";

		// If this breaks - Hi 2040, 2020 says 'hi' -- Chuck
		int twenty_years = 20*365*24*60*60;
		good = LTI13Util.timeStampCheckSign(token_2020, "secret", twenty_years);
		assertTrue(good);
		good = LTI13Util.timeStampCheckSign(token_2020, "xsecret", twenty_years);
		assertFalse(good);

		// Bad signature
		String bad_token_2020 = "1607379725:xyzzy:CVHUHxxxxxxxxxxAnhxQprbZWvAr58V34NprcdMP4gE=";
		good = LTI13Util.timeStampCheckSign(bad_token_2020, "secret", twenty_years);
		assertFalse(good);

		// In the past
		bad_token_2020 = "1507379725:xyzzy:CVHUHPEIfW2zTwMAnhxQprbZWvAr58V34NprcdMP4gE=";
		good = LTI13Util.timeStampCheckSign(bad_token_2020, "secret", twenty_years);
		assertFalse(good);
	}

}

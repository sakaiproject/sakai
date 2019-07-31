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

}

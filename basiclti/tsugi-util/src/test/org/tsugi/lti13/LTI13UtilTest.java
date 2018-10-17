package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.security.*;
import java.util.regex.Pattern;

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
	public void testSHA256() {
		String hash = LTI13Util.sha256("Yada");
		System.out.println("Yada "+hash);
		assertNotNull(hash);
		assertEquals("Imdd9E/bze9+h6T8tofJSwKbLKbiKSaX45BquGq8tNk=", hash);
	}
}

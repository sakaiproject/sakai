/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tsugi.lti13;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author csev
 */
public class LTI13KeySetTest {

	@Test
	public void testKeySets() throws
			NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException {

		String serialized = "-----BEGIN PUBLIC KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgviDRUN1Z6hIOBg5uj1k\n"
				+ "KSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l/gnZ0vIaCuf4G9mkphc/y\n"
				+ "V0cgFY65wQmecPxv3IZ77wbJ+g5lL5vuCVTbh55nD++cj/hSBznXecQTXQNV9d51r\n"
				+ "Ca65+PQ+YL1oRnrpUuLNPbdnc8kT/ZUq5Ic0WJM+NprN1tbbn2LafBY+igqbRQVox\n"
				+ "It75B8cd+35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNv\n"
				+ "w3ykMoVh1pq7zwL4P0IHXevvPnja+PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTwIDAQ\n"
				+ "AB\n"
				+ "-----END PUBLIC KEY-----";

		Key publicKey = LTI13Util.string2PublicKey(serialized);
		// Cast
		RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;

		String keySetJSON = LTI13KeySetUtil.getKeySetJSON(rsaPublic);
		boolean good = keySetJSON.contains("{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",");
		if (!good) {
			System.out.println("keyset JSON is bad\n");
			System.out.println(keySetJSON);
		}
		assertTrue(good);

		// Now do it with an array
		publicKey = LTI13Util.string2PublicKey(serialized);
		// Cast
		rsaPublic = (RSAPublicKey) publicKey;
		Map<String, RSAPublicKey> keys = new TreeMap<>();
		String kid = LTI13KeySetUtil.getPublicKID(rsaPublic);
		keys.put(kid, rsaPublic);

		String keySetJSON2 = LTI13KeySetUtil.getKeySetJSON(keys);
		good = keySetJSON2.contains("{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",");
		if (!good) {
			System.out.println("keyset JSON is bad\n");
			System.out.println(keySetJSON);
		}
		assertTrue(good);

		assertEquals(keySetJSON, keySetJSON2);
	}
	public void testKID() throws
			NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException {

		String serialized = "-----BEGIN PUBLIC KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgviDRUN1Z6hIOBg5uj1k\n"
				+ "KSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l/gnZ0vIaCuf4G9mkphc/y\n"
				+ "V0cgFY65wQmecPxv3IZ77wbJ+g5lL5vuCVTbh55nD++cj/hSBznXecQTXQNV9d51r\n"
				+ "Ca65+PQ+YL1oRnrpUuLNPbdnc8kT/ZUq5Ic0WJM+NprN1tbbn2LafBY+igqbRQVox\n"
				+ "It75B8cd+35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNv\n"
				+ "w3ykMoVh1pq7zwL4P0IHXevvPnja+PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTwIDAQ\n"
				+ "AB\n"
				+ "-----END PUBLIC KEY-----";

		Key publicKey = LTI13Util.string2PublicKey(serialized);
		// Cast
		RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;

		String keySetKID = LTI13KeySetUtil.getPublicKID(rsaPublic);
		assertEquals("42", keySetKID);
	}
}

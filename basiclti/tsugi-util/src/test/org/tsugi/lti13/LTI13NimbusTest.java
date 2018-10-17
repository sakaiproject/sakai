package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.security.*;
import java.util.regex.Pattern;

import java.security.interfaces.RSAPublicKey;

// import org.bouncycastle.jce.provider.BouncyCastleProvider;
public class LTI13NimbusTest {

	// Close to a base64 pattern
	Pattern base64_pattern = Pattern.compile("^[-A-Za-z0-9+/=]+$");

	Pattern upper_hex_pattern = Pattern.compile("^[A-F0-9]+$");
	Pattern lower_hex_pattern = Pattern.compile("^[a-f0-9]+$");

	@Before
	public void setUp() throws Exception {
	}

	// https://tools.ietf.org/html/rfc7517
	// https://connect2id.com/products/nimbus-oauth-openid-connect-sdk
	// https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt/4.3.1
	// http://www.javadoc.io/doc/com.nimbusds/nimbus-jose-jwt/4.3.1
	// http://static.javadoc.io/com.nimbusds/nimbus-jose-jwt/4.3.1/com/nimbusds/jose/jwk/RSAKey.html
	@Test
	public void testRSAPaulGray() throws
			NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException {

		// http://www.bouncycastle.org/wiki/display/JA1/Provider+Installation
		// Security.addProvider(new BouncyCastleProvider());
		KeyPair kp = LTI13Util.generateKeyPair();
		Key publicKey = kp.getPublic();
		java.security.interfaces.RSAPublicKey rsaPublicKey = (java.security.interfaces.RSAPublicKey) publicKey;
		// RSAKey rsaKey = new RSAKey.builder(rsaPublicKey);
		com.nimbusds.jose.jwk.RSAKey rsaKey = new com.nimbusds.jose.jwk.RSAKey.Builder(rsaPublicKey).build();
		String keyStr = rsaKey.toJSONString();
		/*
{"kty":"RSA","e":"AQAB","n":"rTbpgFsy-cho8KY7j9AtbAdQcVU6d1lYGobsOYs_gdgObf-_2uSEiPfHcs9Lz_v41XP2XToCbna1ejHj0xRds2LY3MpOHxy3UV3bk5GQp4c8eg1ydHEF4DpcIdzP2P3QDFeSppJjO3bakA0Atp20iubNZNmO0x42fbrRgYQmkPrpE-ShbNIWhq0FaRDPDg_o2R0rB9IliAfilZgwiGpzvWmOnmaB1maE4WpnWAo4gul8nMBQL0YDbIdCHi3qUx1cnXFgZwufMR27ZZ6xgvt_AY94_KBuuAC1XrQpqEpO5i7t3_tT2_OfAnh6GjXluAa06Iv3JNGfBox81a0h6qxfsw"}
		 */
		boolean good = keyStr.contains("{\"kty\":\"RSA\",\"e\":\"AQAB\",\"n\":");
		if (!good) {
			System.out.println("rsaKey\n" + keyStr);
		}
		assertTrue(good);
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
				+ "-----END PUBLIC KEY-----";

		Key publicKey = LTI13Util.string2PublicKey(serialized);
		// Cast
		RSAPublicKey rsaPublic = (RSAPublicKey) publicKey;

		com.nimbusds.jose.jwk.RSAKey rsaKey = new com.nimbusds.jose.jwk.RSAKey.Builder(rsaPublic).build();
		String keyStr = rsaKey.toJSONString();

		JSONArray jar = new JSONArray();
		JSONObject kobj = (JSONObject) JSONValue.parse(keyStr);
		jar.add(kobj);
		JSONObject keyset = new JSONObject();
		keyset.put("keys", jar);

		String keysetJSON = keyset.toString();
		/*
{"keys":[{"kty":"RSA","e":"AQAB","n":"pgviDRUN1Z6hIOBg5uj1kKSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l_gnZ0vIaCuf4G9mkphc_yV0cgFY65wQmecPxv3IZ77wbJ-g5lL5vuCVTbh55nD--cj_hSBznXecQTXQNV9d51rCa65-PQ-YL1oRnrpUuLNPbdnc8kT_ZUq5Ic0WJM-NprN1tbbn2LafBY-igqbRQVoxIt75B8cd-35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNvw3ykMoVh1pq7zwL4P0IHXevvPnja-PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTw"}]}
		 */
		boolean good = keysetJSON.contains("{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\",");
		if (!good) {
			System.out.println("keyset JSON is bad\n");
			System.out.println(keysetJSON);
		}
		assertTrue(good);
	}

	@Test
	public void testBrokenDeserialization() {
		String bad_serialized = "-----BEGIN PUBLIC KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgviDRUN1Z6hIOBg5uj1k\n"
				+ "KSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l/gnZ0vIaCuf4G9mkphc/y\n"
				+ "V0cgFY65wQmecPxv3IZ77-sakaiger-was-here--o++cj/hSBznXecQTXQNV9d51r\n"
				+ "Ca65+PQ+YL1oRnrpUuLNPbdnc8kT/ZUq5Ic0WJM+NprN1tbbn2LafBY+igqbRQVox\n"
				+ "It75B8cd+35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNv\n"
				+ "w3ykMoVh1pq7zwL4P0IHXevvPnja+PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTwIDAQ\n"
				+ "AB\n"
				+ "-----END PUBLIC KEY-----";

		Key publicKey = LTI13Util.string2PublicKey(bad_serialized);
		assertNull(publicKey);

		String yuck_but_works = "-----BEGIN OAUTH 1.0 KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApgviDRUN1Z6hIOBg5uj1k\n"
				+ "KSJjfJjayEJeJR7A06sm5K4QjYKYMve55LaD8CMqf98l/gnZ0vIaCuf4G9mkphc/y\n"
				+ "V0cgFY65wQmecPxv3IZ77wbJ+g5lL5vuCVTbh55nD++cj/hSBznXecQTXQNV9d51r\n"
				+ "Ca65+PQ+YL1oRnrpUuLNPbdnc8kT/ZUq5Ic0WJM+NprN1tbbn2LafBY+igqbRQVox\n"
				+ "It75B8cd+35iQAUm8B4sw8zGs1bFpBy3A8rhCYcBAOdK2iSSudK2WEfW1E7RWnnNv\n"
				+ "w3ykMoVh1pq7zwL4P0IHXevvPnja+PmAT9zTwgU8WhiiIKl7YtJzkR9pEWtTwIDAQ\n"
				+ "AB\n"
				+ "-----END OAUTH 1.0 KEY-----";

		publicKey = LTI13Util.string2PublicKey(yuck_but_works);
		assertNotNull(publicKey);

	}
}

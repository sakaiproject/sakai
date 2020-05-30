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

	@Test
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
		assertEquals("1171207714", keySetKID);
	}

	@Test
	public void testKeySetJSON() 
	    throws java.io.IOException, java.text.ParseException, com.nimbusds.jose.JOSEException, java.net.MalformedURLException
    {
        String kid = "49e4cfe6d3280fec019c92abe85b2747ffd98d19845b99373dbadc741286288c";

        String json = "{\n" +
            "\"keys\": [\n" +
                "{\n" +
                    "\"kty\": \"RSA\",\n" +
                    "\"alg\": \"RS256\",\n" +
                    "\"e\": \"AQAB\",\n" +
                    "\"n\": \"sEhARJcwaQwI1FyzNLrGN1gUklL8Dwqte2TzHdNztskzdwXhca5HDMwIWmQ6oLoPaoyP10BzNUdV8iyrKncDPc2eZRIOwNhHF2mmWr1Ed2d2uK5ME0CYSV2XXgUyFV2dyB8IQmP9QoPgRyLE1HvwkovB7N87xv21ACOqMCad5EXJH4SIltdAoYKjuDfTTQJbnWwO6LLK0jK2-H-bdqj7_EBAHLFiOs5g9_ndts-oGndC75wCIdEgAG77ZLWVZ7ikhKhlMTirxW-tDgDoLpzUNiS2x4mTM8omxtP8yfThKW-wUUtgSA0KCuM_PCA55IZa1d9HQQyQVBZ7Dt-EMaAW9Q\",\n" +
                    "\"kid\": \"49e4cfe6d3280fec019c92abe85b2747ffd98d19845b99373dbadc741286288c\",\n" +
                    "\"use\": \"sig\"\n" +
                "}\n" +
            "]\n" +
        "}\n";


        String actual_public_key_string = "-----BEGIN PUBLIC KEY-----\n"
            +"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsEhARJcwaQwI1FyzNLrG\n"
            +"N1gUklL8Dwqte2TzHdNztskzdwXhca5HDMwIWmQ6oLoPaoyP10BzNUdV8iyrKncD\n"
            +"Pc2eZRIOwNhHF2mmWr1Ed2d2uK5ME0CYSV2XXgUyFV2dyB8IQmP9QoPgRyLE1Hvw\n"
            +"kovB7N87xv21ACOqMCad5EXJH4SIltdAoYKjuDfTTQJbnWwO6LLK0jK2+H+bdqj7\n"
            +"/EBAHLFiOs5g9/ndts+oGndC75wCIdEgAG77ZLWVZ7ikhKhlMTirxW+tDgDoLpzU\n"
            +"NiS2x4mTM8omxtP8yfThKW+wUUtgSA0KCuM/PCA55IZa1d9HQQyQVBZ7Dt+EMaAW\n"
            +"9QIDAQAB\n"
            +"-----END PUBLIC KEY-----\n";

		/* Just in case - the corresponding private key

		    -----BEGIN PRIVATE KEY-----
			MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCwSEBElzBpDAjU
			XLM0usY3WBSSUvwPCq17ZPMd03O2yTN3BeFxrkcMzAhaZDqgug9qjI/XQHM1R1Xy
			LKsqdwM9zZ5lEg7A2EcXaaZavUR3Z3a4rkwTQJhJXZdeBTIVXZ3IHwhCY/1Cg+BH
			IsTUe/CSi8Hs3zvG/bUAI6owJp3kRckfhIiW10ChgqO4N9NNAludbA7ossrSMrb4
			f5t2qPv8QEAcsWI6zmD3+d22z6gad0LvnAIh0SAAbvtktZVnuKSEqGUxOKvFb60O
			AOgunNQ2JLbHiZMzyibG0/zJ9OEpb7BRS2BIDQoK4z88IDnkhlrV30dBDJBUFnsO
			34QxoBb1AgMBAAECggEANFXvBq4gSD+za2DuL2x4Jrw2t0592PVhle3do/BAPXc0
			mVScnwSPwYPdwwBYy5kRtJ9woQZYbev030ZVtvDIfJPMP9OBn81WJeRO6EeiyRRl
			keTlXx3hWH65zscaHG/DxUM5T4SiDxpZ/qSa7T6yqL9nXqzT8XsnPphYEZ2VaMy0
			6TsGLbNRrFvnV0+kAa/3S0hS66c8yoG/xSQ5w47YR36hARmvFZeJ5+/gyhPwFcxF
			0B+yKjWG5vAMHObDHsL2XeMq4S8YSCOJisf+78V+OElbvcAsV/TlfKq/MdROMjAZ
			WjGXb7G1R/7f1eKF1euqzwvcXmq+Y1Sq0xdcBKd6oQKBgQDeV7hrGFnR6iomBaIO
			kkqMh5oN6f9igSWD6JhXytk1kralBFs3xP0PYqnokPbXQMLLmSNbM1xsCufTclt5
			mhYVNTYcrJQXc9H78a1aBNXHULhwZ2136moF4vWxNOi8xJw+FV1C4WqFWOHsee57
			Cxpc9wPF50GFKfwQqYcY6Jr/WQKBgQDK95XPW2SjraDH3Kza7zc15Hj+HSGiAE/P
			kZS6+HHE1DRNECuItwRBwxNzf0e3Q1pKrU1fyqi9AVFGzRDqKtbzPcupz6cL+ws6
			2XDwaUTEiM065qFclY15vx39vI1OZfrbr3OFUBg5H8l1vy01vsj20nD9/EUoh8gn
			ljIalCQc/QKBgQDJAoMehyBE7691Omh9RfKRw9IZvC//bQRukayQyjm+/kmTPDH/
			ZF7oNumm8M/IYKr7CBJ31CI9J0rY1a0vbYeDWtz15WvpG/N+E8sF9BMVI7vrGwZT
			gCCj+8DlmRDD//fFBOmQmlwD/AwcD0vz/2CxJTxLAbwxWHii07Doavod2QKBgQC2
			zXr+QWBJEw5mM9jzbtSZE2ft5yrtj75SQtpj83P0RPO+HLw47C1HVX1lXtOmuWDP
			NXsktZB4eoFLdVKwtVjXx2ZxUPcxETAyFrljrkwjftrpG+NlUcUUTA+lAnd6jtr7
			lfBDt7m5aWm3RTJg3658r1jZSKa5NGIPVXVW/unvuQKBgAsID0lc2RNDX3EoYhJU
			fVty8VrzYHkLCkyLBKtgpdZhDw8A6oMaE9t13L0FLToKzDRdHuMHrkybeKF9q50P
			yORiyqeN5shmcafTaOTZoeyu3Vit0CcRwZehwBhvAH+JW9wRCBMlBuQoyn4sZIPn
			qKKGUvhdJB7OGrbiFUSJepNT
			-----END PRIVATE KEY-----
		*/

		RSAPublicKey publicKey = LTI13KeySetUtil.getKeyFromKeySetString(kid, json);

		RSAPublicKey publicKey2 = (RSAPublicKey) LTI13Util.string2PublicKey(actual_public_key_string);

		assertEquals(publicKey, publicKey2);

		// This is not really runnable in a unit test - but it uses local tsugi running on
		// public / private / kid setup

		/*
		String tsugi_keyset = "http://localhost:8888/py4e/tsugi/lti/keyset";
		RSAPublicKey publicKey3 = LTI13KeySetUtil.getKeyFromKeySet(kid, tsugi_keyset);
		System.out.println("publickey3="+publicKey3);
		assertEquals(publicKey3, publicKey2);
		*/

	}
}

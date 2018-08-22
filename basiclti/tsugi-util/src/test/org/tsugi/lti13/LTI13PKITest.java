package org.tsugi.lti13;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import org.json.simple.JSONArray;

import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.tsugi.lti13.LTI13Util;

// import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class LTI13PKITest {

	// Close to a base64 pattern
        Pattern base64_pattern = Pattern.compile("^[-A-Za-z0-9+/=]+$");

	Pattern upper_hex_pattern = Pattern.compile("^[A-F0-9]+$");
	Pattern lower_hex_pattern = Pattern.compile("^[a-f0-9]+$");

	@Before
	public void setUp() throws Exception {
	}

	// https://stackoverflow.com/questions/1709441/generate-rsa-key-pair-and-encode-private-as-string
	@Test
	public void testRSAKeyPublic1() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
		StringBuffer retString = new StringBuffer();
		for (int i = 0; i < publicKey.length; ++i) {
			retString.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
		}
		assertEquals(188, retString.length());
		Matcher m = lower_hex_pattern.matcher(retString);
		boolean good = m.find();
                if ( ! good ) System.out.println("Bad retString:\n"+retString);
                assertTrue(good);
	}

	// https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
	@Test
	public void testRSAKeyPrivate1() throws
		NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException
	{

		// http://www.bouncycastle.org/wiki/display/JA1/Provider+Installation
		// Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		Key privateKey = keyGen.genKeyPair().getPrivate();

		byte[] privateByte = privateKey.getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();
		String privateEncoded = encoder.encodeToString(privateByte);
		Matcher m = base64_pattern.matcher(privateEncoded);
		boolean good = m.find();
		if ( ! good ) System.out.println("Bad Encode: "+privateEncoded);

		assertTrue(good);
		String privateString = encoder.encodeToString(privateByte);
		String privatePKCS8 = "-----BEGIN RSA PRIVATE KEY-----\n" +
			privateString +
			"\n-----END RSA PRIVATE KEY-----\n";
		// assertEquals(1685, begin.length());

		String privateStringNew = LTI13Util.stripPKCS8(privatePKCS8);
		// https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
		if ( ! privateString.equals(privateStringNew) ) {
			System.out.println("privatePKCS8\n"+privatePKCS8);
			System.out.println("privateStringNew\n"+privateStringNew);
		}
		assertEquals(privateStringNew, privateString);
		byte[] newPrivateByte = Base64.getDecoder().decode(privateStringNew);

		assertEquals(privateByte.length,newPrivateByte.length);
		for(int i=0; i < privateByte.length; i++ ) {
			if ( privateByte[i] != newPrivateByte[i] ) {
				System.out.println("Error in position "+i+" of "+privateByte.length);
				assertEquals(privateByte[i], newPrivateByte[i]);
				break;
			}
		}

		// https://stackoverflow.com/questions/49330180/generating-a-jwt-using-an-existing-private-key-and-rs256-algorithm
		Key newKey = LTI13Util.string2PrivateKey(privateString);

                String jws = Jwts.builder().setSubject("Joe").signWith(privateKey).compact();
                String newJws = Jwts.builder().setSubject("Joe").signWith(newKey).compact();

		if ( ! jws.equals(newJws) ) {
			System.out.println("Mismatch\n");
			System.out.println("JWT with original key\n"+jws);
			System.out.println("JWT with serialized key\n"+newJws);
		}
		assertEquals(jws,newJws);



	}

	@Test
	public void testRSAKeyPublic2() throws NoSuchAlgorithmException, NoSuchProviderException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();

		String pemBase64 = javax.xml.bind.DatatypeConverter.printBase64Binary(publicKey);
		assertEquals(128, pemBase64.length());
		Matcher m = base64_pattern.matcher(pemBase64);
		assertTrue(m.find());

		String hex = javax.xml.bind.DatatypeConverter.printHexBinary(publicKey);
		assertEquals(188, hex.length());
		m = upper_hex_pattern.matcher(hex);
		assertTrue(m.find());
	}

}


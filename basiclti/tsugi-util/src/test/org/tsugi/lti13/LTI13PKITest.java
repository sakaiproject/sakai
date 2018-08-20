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
		System.out.println(retString);
		System.out.println(retString.length());
		assertEquals(188, retString.length());
		Matcher m = lower_hex_pattern.matcher(retString);
		assertTrue(m.find());
	}

	// https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
	@Test
	public void testRSAKeyPrivate1() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		byte[] privateKey = keyGen.genKeyPair().getPrivate().getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();
		String encoded = encoder.encodeToString(privateKey);
		Matcher m = base64_pattern.matcher(encoded);
		assertTrue(m.find());
		String begin = "-----BEGIN RSA PUBLIC KEY-----\n" + 
			encoder.encodeToString(privateKey) + 
			"\n-----END RSA PUBLIC KEY-----\n";
		// assertEquals(1685, begin.length());
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


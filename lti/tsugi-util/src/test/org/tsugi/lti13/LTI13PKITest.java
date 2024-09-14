package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

import io.jsonwebtoken.Jwts;

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
		if (!good) {
			System.out.println("Bad retString:\n" + retString);
		}
		assertTrue(good);
	}

	// https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
	@Test
	public void testRSAChrisMaurer() throws
			NoSuchAlgorithmException, NoSuchProviderException, java.security.spec.InvalidKeySpecException {

		// http://www.bouncycastle.org/wiki/display/JA1/Provider+Installation
		// Security.addProvider(new BouncyCastleProvider());
		KeyPair kp = LTI13Util.generateKeyPair();
		Key privateKey = kp.getPrivate();

		// Hand construct the PKCS8 string
		byte[] privateByte = privateKey.getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();
		String privateEncoded = encoder.encodeToString(privateByte);
		Matcher m = base64_pattern.matcher(privateEncoded);
		boolean good = m.find();
		if (!good) {
			System.out.println("Bad Encode: " + privateEncoded);
		}

		assertTrue(good);

		// Get the version with the -----BEGIN...
		String privatePKCS8 = LTI13Util.getPrivateEncoded(kp);

		// Go back to the PKCS8 format
		String privateEncoded2 = LTI13Util.stripPKCS8(privatePKCS8);

		if (!privateEncoded.equals(privateEncoded2)) {
			System.out.println("privateEncoded\n" + privateEncoded);
			System.out.println("privateEncoded2\n" + privateEncoded2);
		}
		assertEquals(privateEncoded, privateEncoded2);

		// Decode and check the bytes...
		byte[] newPrivateByte = Base64.getDecoder().decode(privateEncoded);

		assertEquals(privateByte.length, newPrivateByte.length);
		for (int i = 0; i < privateByte.length; i++) {
			if (privateByte[i] != newPrivateByte[i]) {
				System.out.println("Error in position " + i + " of " + privateByte.length);
				assertEquals(privateByte[i], newPrivateByte[i]);
				break;
			}
		}

		// De serialize the key
		Key newPrivateKey = LTI13Util.string2PrivateKey(privateEncoded);

		assertEquals(privateKey.getFormat(), newPrivateKey.getFormat());
		assertEquals(privateKey.getAlgorithm(), newPrivateKey.getAlgorithm());

		String jws = Jwts.builder().setSubject("Joe").signWith(privateKey).compact();
		String newJws = Jwts.builder().setSubject("Joe").signWith(newPrivateKey).compact();

		if (!jws.equals(newJws)) {
			System.out.println("Mismatch\n");
			System.out.println("JWT with original key\n" + jws);
			System.out.println("JWT with serialized key\n" + newJws);
		}
		assertEquals(jws, newJws);

		// Lets do Public Keys ------
		Key publicKey = kp.getPublic();

		// Hand construct the PKCS8 string
		byte[] publicByte = publicKey.getEncoded();
		String publicEncoded = encoder.encodeToString(publicByte);
		m = base64_pattern.matcher(publicEncoded);
		good = m.find();
		if (!good) {
			System.out.println("Bad Encode: " + publicEncoded);
		}

		assertTrue(good);

		// Get the version with the -----BEGIN...
		String publicPKCS8 = LTI13Util.getPublicEncoded(kp);

		// Go back to the PKCS8 format
		String publicEncoded2 = LTI13Util.stripPKCS8(publicPKCS8);

		if (!publicEncoded.equals(publicEncoded2)) {
			System.out.println("publicEncoded\n" + publicEncoded);
			System.out.println("publicEncoded2\n" + publicEncoded2);
		}
		assertEquals(publicEncoded, publicEncoded2);

		// Decode and check the bytes...
		byte[] newPublicByte = Base64.getDecoder().decode(publicEncoded);

		assertEquals(publicByte.length, newPublicByte.length);
		for (int i = 0; i < publicByte.length; i++) {
			if (publicByte[i] != newPublicByte[i]) {
				System.out.println("Error in position " + i + " of " + publicByte.length);
				assertEquals(publicByte[i], newPublicByte[i]);
				break;
			}
		}

		// De serialize the key
		Key newPublicKey = LTI13Util.string2PublicKey(publicEncoded);
		assertEquals(publicKey.getFormat(), newPublicKey.getFormat());
		assertEquals(publicKey.getAlgorithm(), newPublicKey.getAlgorithm());

		// Now lets verify the string....
		String subject = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(newPublicKey).parseClaimsJws(jws).getBody().getSubject();
		assertEquals("Joe", subject);

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

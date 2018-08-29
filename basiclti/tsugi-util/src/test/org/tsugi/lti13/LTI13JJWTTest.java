package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.UUID;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.Locale;

public class LTI13JJWTTest {

	public static final String LTI_13_ISSUER = "https://sakaiproject.org/";
	public static final String LTI_13_KEY_NONCE = "nonce";

	// Close to a base64 pattern
	Pattern base64_pattern = Pattern.compile("^[-A-Za-z0-9+/=]+$");
	Pattern base64url_pattern = Pattern.compile("^[A-Za-z0-9.\\-_]+$");

	Pattern upper_hex_pattern = Pattern.compile("^[A-F0-9]+$");
	Pattern lower_hex_pattern = Pattern.compile("^[a-f0-9]+$");
	Pattern jws_pattern = Pattern.compile("^[a-f0-9]+$");
	Pattern uuid_pattern = Pattern.compile("^[a-f0-9\\-]+$");

	// https://github.com/jwtk/jjwt
	@Test
	public void testOne() throws NoSuchAlgorithmException, NoSuchProviderException {
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		String jws = Jwts.builder().setSubject("Joe").signWith(key).compact();
		assertEquals(83, jws.length());
		Matcher m = base64url_pattern.matcher(jws);
		boolean good = m.find();
		if (!good) {
			System.out.println("Bad JWS:\n" + jws);
		}
		assertTrue(good);

		String subject = Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject();
		assertEquals("Joe", subject);
	}

	public void testTwo() throws NoSuchAlgorithmException, NoSuchProviderException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		Key key = keyGen.genKeyPair().getPrivate();

		String jws = Jwts.builder().setSubject("Joe").signWith(key).compact();
		assertEquals(83, jws.length());
		Matcher m = base64url_pattern.matcher(jws);
		boolean good = m.find();
		if (!good) {
			System.out.println("Bad JWS:\n" + jws);
		}
		assertTrue(good);

		String subject = Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject();
		assertEquals("Joe", subject);
	}

	// https://github.com/jwtk/jjwt#jws-create
	@Test
	public void testThree() throws NoSuchAlgorithmException, NoSuchProviderException {
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String jws = Jwts.builder()
				.setIssuer("me").setSubject("Bob").setAudience("you")
				.signWith(key).compact();

		String x = getBody(jws, key);
		assertEquals("{iss=me, sub=Bob, aud=you}", x);
	}

	public static String getBody(String jws, Key key) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().toString();
	}

	// https://gist.github.com/ewpreston/9878682d51c71c4d44d9790379b4bd39
	@Test
	public void testFour() throws NoSuchAlgorithmException, NoSuchProviderException {
		String nonce = UUID.randomUUID().toString();
		String client_id = "12345";
		String subject = "Bob";
		String locale = Locale.getDefault().getLanguage().replace('_', '-');

		Matcher m = uuid_pattern.matcher(nonce);
		boolean good = m.find();
		if (!good) {
			System.out.println("Bad UUID:\n" + nonce);
		}
		assertTrue(good);

		Date now = new Date();
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String jws = Jwts.builder()
				.setIssuer(LTI_13_ISSUER)
				.setSubject(subject)
				.setAudience(client_id)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + 600000L)) // Milliseconds
				.claim(LTI13ConstantsUtil.KEY_NONCE, nonce)
				.claim(LTI13ConstantsUtil.KEY_LOCALE, locale)
				.signWith(key)
				.compact();

		String body = getBody(jws, key);
		good = body.contains(LTI_13_ISSUER);
		if (!good) {
			System.out.println("Bad body: " + body);
		}
		assertTrue(good);
	}
}

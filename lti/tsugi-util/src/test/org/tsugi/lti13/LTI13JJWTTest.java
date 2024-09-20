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

	public static final String LTI_13_ISSUER = "https://www.sakailms.org/";
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

		String subject = Jwts.parser().setAllowedClockSkewSeconds(60).setSigningKey(key).parseClaimsJws(jws).getBody().getSubject();
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

	@Test
	public void testFive() throws NoSuchAlgorithmException, NoSuchProviderException {
		String inp = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODg4OFwvdHN1Z2kiLCJzdWIiOiJsdGkxM19odHRwczpcL1wvd3d3LnNha2FpcHJvamVjdC5vcmdcL181MmE2N2I2OS1jNTk4LTRjZWQtYWNiMy1kOTQ5MzJkZTJiMWEiLCJhdWQiOiJodHRwOlwvXC9sb2NhbGhvc3Q6ODA4MFwvaW1zYmxpc1wvbHRpMTNcL3Rva2VuXC85IiwiaWF0IjoxNTM2NDMzODUwLCJleHAiOjE1MzY0MzM5MTAsImp0aSI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4ODg4XC90c3VnaTViOTQxZWJhNWVkNjQifQ.JhwwgUEVV85HLteYmmSykQkMkmP-mcbV0R99tvP69hTFBJf3ZAS_uyfdXZoeRJaS5_hzwNf_b9HXYJWmZvYQK2NLt3s5GsW3h2pD4S3lVybIRXbpajr8NgeKA3BfsRLDoyKCLYn16BDR5w7ULZj0om8avVSFMUNbQYouc6XaTUPCZGfxPn-OPFYxX7SlDfIZjvbPWFxQh-cS90m_mKIcSYitoKrg9az59K6iGu-pq1PmZYSdt4xabh0_WoOiracvvJE6N1Um7A5enS3iXuHbCufKySIO2ykYtdRgVqhxP5YYPlar55nNRqEZtDgBgMMsneNePfMrifOvvFLkxnpefA";
		String header = LTI13JwtUtil.rawJwtHeader(inp);
		boolean good = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}".equals(header);
		if ( ! good ) System.out.println("Bad header:"+header);
		assertTrue(good);

		String body = LTI13JwtUtil.rawJwtBody(inp);
		assertNotNull(body);
		good = body.startsWith("{\"iss\":\"http:\\/\\/localhost:8888\\/tsugi\",\"sub\"");
		if ( ! good ) System.out.println("Bad body:"+body);
		assertTrue(good);

	}
}

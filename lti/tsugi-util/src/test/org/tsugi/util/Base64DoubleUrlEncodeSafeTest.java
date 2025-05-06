package org.tsugi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Test class for Base64DoubleUrlEncodeSafe utility.
 * This class tests the following aspects:
 * 1. URL-safe encoding and decoding of strings
 * 2. Handling of special characters and edge cases
 * 3. Double-safe decoding functionality
 * 4. Robustness against malformed input
 * 5. Performance with large strings
 * 6. Compatibility with standard Base64 encoding
 */
public class Base64DoubleUrlEncodeSafeTest {

	private Base64.Encoder standardUrlEncoder;
	private Base64.Encoder standardEncoder;

	@Before
	public void setUp() {
		standardUrlEncoder = Base64.getUrlEncoder();
		standardEncoder = Base64.getEncoder();
	}

	public static String[] testStrings = {
		"Hello, Base64DoubleUrlEncodeSafe!",
		"Testing encoding and decoding.",
		"12345",
		"This is a longer string with more characters.",
		"A tree 🌲 was here",
		"A",
		"https://www.tsugicloud.org/lti/store/?x=42&y=26",
		"https://www.tsugicloud.org/lti_store/?x=42&y=26",
		"https://chat.openai.com/failed/to_solve!this[problem]with{auto}generated:code;",
		" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~",
		"  !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~",
		"   !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~",
		"    !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~",

		// Lets throw in some mean Unicode stuff

		"ஹ௸௵꧄.ဪ꧅⸻𒈙𒐫﷽𒌄𒈟𒍼𒁎𒀱𒈓𒍙𒊎𒄡𒅌𒁏𒀰𒐪𒈙𒐫𱁬𰽔𪚥䨻龘䲜       𓀐𓂸😃⃢👍༼;´༎ຶ ۝ ༎ຶ༽",

		"hÃllo What is the weather tomorrow Göödnight 😊",
		"Ḽơᶉëᶆ ȋṕšᶙṁ ḍỡḽǭᵳ ʂǐť ӓṁệẗ, ĉṓɲṩḙċťᶒțûɾ ấɖḯƥĭṩčįɳġ ḝłįʈ, șếᶑ ᶁⱺ ẽḭŭŝḿꝋď ṫĕᶆᶈṓɍ ỉñḉīḑȋᵭṵńť ṷŧ ḹẩḇőꝛế éȶ đꝍꞎôꝛȇ ᵯáꞡᶇā ąⱡîɋṹẵ.",
		" ăѣ𝔠ծềſģȟᎥ𝒋ǩľḿꞑȯ𝘱𝑞𝗋𝘴ȶ𝞄𝜈ψ𝒙𝘆𝚣1234567890!@#$%^&*()-_=+[{]};:'\",<.>/?~𝘈Ḇ𝖢𝕯٤ḞԍНǏ𝙅ƘԸⲘ𝙉০Ρ𝗤Ɍ𝓢ȚЦ𝒱Ѡ𝓧ƳȤѧᖯć𝗱ễ𝑓𝙜Ⴙ𝞲𝑗𝒌ļṃŉо𝞎𝒒ᵲꜱ𝙩ừ𝗏ŵ𝒙𝒚ź1234567890!@#$%^&*()-_=+[{]};:'\",<.>/?~АḂⲤ𝗗𝖤𝗙ꞠꓧȊ𝐉𝜥ꓡ𝑀𝑵Ǭ𝙿𝑄Ŗ𝑆𝒯𝖴𝘝𝘞ꓫŸ𝜡ả𝘢ƀ𝖼ḋếᵮℊ𝙝Ꭵ𝕛кιṃդⱺ𝓅𝘲𝕣𝖘ŧ𝑢ṽẉ𝘅ყž1234567890!@#$%^&*()-_=+[{]};:'\",<.>/?~Ѧ𝙱ƇᗞΣℱԍҤ١𝔍К𝓛𝓜ƝȎ𝚸𝑄Ṛ𝓢ṮṺƲᏔꓫ𝚈𝚭𝜶Ꮟçძ𝑒𝖿𝗀ḧ𝗂𝐣ҝɭḿ𝕟𝐨𝝔𝕢ṛ𝓼тú𝔳ẃ⤬𝝲𝗓1234567890!@#$%^&*()-_=+[{]};:'\",<.>/?~𝖠Β𝒞𝘋𝙴𝓕ĢȞỈ𝕵ꓗʟ𝙼ℕ০𝚸𝗤ՀꓢṰǓⅤ𝔚Ⲭ𝑌𝙕𝘢𝕤 – Andrew Feb 10, 2019 at 14:41",
		// https://jeff.cis.cabrillo.edu/tools/homoglyphs
		"Ǐ ᴛ𝕙ȋṉ𝔨 Ꮥɑ𝝹𝛂îꝒⱡů𝒔 ị𝗌 𝑎 𝝂𝕖ᵳℽ ƈθꝋȴ ŵ𝕒ɣ ṫố ɪṅⱦẽ𝑔ṛѧȶể Ṥảⱪȁ𝗶 Ꭵℼṱᴏ Ḉ𝜶𝕟ṽ𝜶𝗌, 𝘿2𝕷, ąп𝔡 Бḻáċĸѣò𝐚ȑ𝒹",
		""
	};

	/**
	 * Tests that Java's built-in URL-safe Base64 encoding is not sufficient
	 * for our use case where the encoded string needs to be URL-safe even
	 * after URL encoding.
	 */
	@Test
	public void testVerifyJavaBase64UrlSafeEncodingIsNotSufficient() throws java.io.UnsupportedEncodingException {

		int failcount = 0;
		for (String input : testStrings) {
			String base64UrlEncoded = java.util.Base64.getUrlEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));

			// URL-encode the base64 URL encoded string
			String urlEncoded = java.net.URLEncoder.encode(base64UrlEncoded, StandardCharsets.UTF_8);

			// Compare the original encoded string with the URL-encoded string
			if ( ! base64UrlEncoded.equals(urlEncoded)) failcount ++;
		}
		assertFalse(failcount == 0);
	}

	/**
	 * Tests basic encoding and decoding functionality
	 */
	@Test
	public void testEncodeAndDecode() throws java.io.UnsupportedEncodingException {
		for (String input : testStrings) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
			assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);

			try {
				String urlEncoded = java.net.URLEncoder.encode(encoded, "UTF-8");
				assertEquals("UrlEncoding changed the string: " + encoded, encoded, urlEncoded);
			} catch (java.io.UnsupportedEncodingException e) {
				fail("Unxpected UnsupportedEncodingException for URLEncoder "+encoded);
			}
		}

		assertEquals("Encode of null should produce null: ", null, Base64DoubleUrlEncodeSafe.encode(null));
		assertEquals("Decode of null should produce null: ", null, Base64DoubleUrlEncodeSafe.decode(null));
	}

	/**
	 * Tests the double-safe decoding feature which should handle both
	 * regular Base64 and URL-safe Base64 encoded strings
	 */
	@Test
	public void testEncodeAndDoubleDecodeSafely() throws java.io.UnsupportedEncodingException {
		for (String input : testStrings) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			String decoded = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(encoded);
			assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);
			String decodedDouble = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(decoded);
			assertEquals("Double Decoding did not produce the original string for input: " + input, input, decoded);
			String encodedDouble = Base64DoubleUrlEncodeSafe.encode(encoded);
			decoded = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(Base64DoubleUrlEncodeSafe.decodeDoubleSafe(encodedDouble));
			assertEquals("Double encode / double decode did not produce the original string for input: " + input, input, decoded);
		}
	}

	/**
	 * Tests handling of replacement characters in the encoding/decoding process
	 */
	@Test
	public void testReplacementCharacter() throws java.io.UnsupportedEncodingException {

		for (String input : testStrings) {
			String encoded = java.util.Base64.getUrlEncoder().encodeToString(input.getBytes("UTF-8"));

			assertEquals("Encoded strings should never contain a "+Base64DoubleUrlEncodeSafe.REPLACEMENT_CHARACTER,
				encoded.indexOf(Base64DoubleUrlEncodeSafe.REPLACEMENT_CHARACTER), -1);
		}
	}

	/**
	 * Tests that decoding invalid characters fails as expected
	 */
	@Test(expected = java.lang.IllegalArgumentException.class)
	public void testDecodeInvalidCharactersFail() {
		String invalidBase64DoubleUrlEncodeSafeString = "InvalidString$#@!";

		Base64DoubleUrlEncodeSafe.decode(invalidBase64DoubleUrlEncodeSafeString);
		fail("Expected IllegalArgumentException for invalid Base64DoubleUrlEncodeSafe string");
	}

	/**
	 * Tests encoding and decoding of all non-surrogate Unicode code points
	 */
	@Test
	public void testAllNonSurrogateCodePoints() {
		char c;
		int i;
		StringBuffer sb = new StringBuffer();

		for(i=1;i<0xD800;i++) {
			c = (char) i;
			sb.append(c);
		}

		String input = sb.toString();
		String encoded = Base64DoubleUrlEncodeSafe.encode(input);
		String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
		assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);

		try {
			String urlEncoded = java.net.URLEncoder.encode(encoded, "UTF-8");
			assertEquals("UrlEncoding changed the string: " + encoded, encoded, urlEncoded);
		} catch (java.io.UnsupportedEncodingException e) {
			fail("Unxpected UnsupportedEncodingException for URLEncoder "+encoded);
		}
	}

	/**
	 * Tests that decodeDoubleSafe properly handles invalid input
	 */
	@Test
	public void testDecodeDoubleSafeWithInvalidInput() {
		// Test with invalid Base64 strings
		String[] invalidInputs = {
			"InvalidBase64String!@#$",
			"Not==Valid==Base64",
			"Partial/Valid+Base64WithInvalidChars!@#",
			"AB==CD", // Valid padding but in wrong position
		};
		
		for (String invalidInput : invalidInputs) {
			// Regular decode would throw an exception
			try {
				Base64DoubleUrlEncodeSafe.decode(invalidInput);
				fail("Expected IllegalArgumentException for invalid input: " + invalidInput);
			} catch (IllegalArgumentException e) {
				// Expected behavior
			}
			
			// But decodeDoubleSafe should safely return the original string
			String result = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(invalidInput);
			assertEquals("decodeDoubleSafe should return the original string for invalid input", 
					invalidInput, result);
		}
	}
	
	/**
	 * Test the specific case that prompted the fix - handling URL-distorted Base64 strings
	 * containing tilde characters that get modified during URL transmission
	 */
	@Test
	public void testLoginHintDistortionCase() {
		// Create a test case similar to the real-world issue
		String originalUrl = "/access/lti/site/~e9376efc-2102-4eb8-ac60-0e7ba383cb50/content:118";
		
		// Step 1: Encode with standard Base64
		String standardBase64 = new String(java.util.Base64.getEncoder()
				.encode(originalUrl.getBytes()));
		
		// Step 2: Manually create a distorted version similar to what was described in the issue
		// where the tilde character causes issues
		String distortedVersion = standardBase64.replace("~", " ").replace("/", " ").replace("+", " ");
		
		// Step 3: Attempt to decode with both methods
		try {
			// This would throw an exception due to invalid Base64 characters
			java.util.Base64.getDecoder().decode(distortedVersion);
			fail("Expected standard Base64 decode to fail on distorted input");
		} catch (IllegalArgumentException e) {
			// Expected behavior
		}
		
		// Step 4: But our decodeDoubleSafe should handle it gracefully
		String result = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(distortedVersion);
		// It won't recover the original string, but it should not throw an exception
		assertEquals("decodeDoubleSafe should return the distorted string when it can't decode properly",
				distortedVersion, result);
	}
	
	/**
	 * Test the encoding/decoding of strings with the URL-problematic characters
	 * that our implementation specifically handles (=, /, +)
	 */
	@Test
	public void testProblematicCharacters() {
		// Create strings with problematic characters for URL encoding
		String[] problematicStrings = {
			"String with equals sign = in it",
			"String with plus + character",
			"String with forward slash / character",
			"String with all three = + /",
			"String with multiple======++++/////"
		};
		
		for (String input : problematicStrings) {
			// 1. Standard URL-safe Base64 encode
			String standardUrlSafeEncoded = java.util.Base64.getUrlEncoder().encodeToString(input.getBytes());
			
			// 2. Our double-safe encode
			String doubleUrlSafeEncoded = Base64DoubleUrlEncodeSafe.encode(input);
			
			// 3. URL encode both results
            String urlEncodedStandard = java.net.URLEncoder.encode(standardUrlSafeEncoded, StandardCharsets.UTF_8);
            String urlEncodedDoubleSafe = java.net.URLEncoder.encode(doubleUrlSafeEncoded, StandardCharsets.UTF_8);

            // 4. The double-safe version should be identical before and after URL encoding
            assertEquals("Our double-safe encoding should survive URL encoding",
                    doubleUrlSafeEncoded, urlEncodedDoubleSafe);

            // 5. Verify we can decode properly
            String decoded = Base64DoubleUrlEncodeSafe.decode(doubleUrlSafeEncoded);
            assertEquals("Our encoding/decoding should recover the original string",
                    input, decoded);

            // 6. The double-safe decoder should also handle standard URL-safe encoded strings
            String decodedStandard = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(standardUrlSafeEncoded);
            assertEquals("Our decoder should handle standard URL-safe encoded strings",
                    input, decodedStandard);
        }
	}
	
	/**
	 * Test repeated encoding and decoding to verify robustness
	 */
	@Test
	public void testRepeatedEncodeAndDecode() {
		for (String input : testStrings) {
			if (input.isEmpty()) continue; // Skip empty string
			
			// Encode multiple times
			String encoded1 = Base64DoubleUrlEncodeSafe.encode(input);
			String encoded2 = Base64DoubleUrlEncodeSafe.encode(encoded1);
			String encoded3 = Base64DoubleUrlEncodeSafe.encode(encoded2);
			
			// Decode multiple times
			String decoded3 = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(encoded3);
			String decoded2 = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(decoded3);
			String decoded1 = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(decoded2);
			
			// Ensure we get back the original string
			assertEquals("Multiple encoding/decoding should recover the original string",
					input, decoded1);
		}
	}

	/**
	 * Test empty string and null handling
	 */
	@Test
	public void testEmptyAndNullStrings() {
		// Test empty string
		String emptyResult = Base64DoubleUrlEncodeSafe.encode("");
		assertEquals("Empty string should encode to empty string", "", emptyResult);
		assertEquals("Empty string should decode to empty string", "", Base64DoubleUrlEncodeSafe.decode(emptyResult));

		// Test null handling
		assertNull("Null should encode to null", Base64DoubleUrlEncodeSafe.encode(null));
		assertNull("Null should decode to null", Base64DoubleUrlEncodeSafe.decode(null));
		assertNull("Null should double-safe decode to null", Base64DoubleUrlEncodeSafe.decodeDoubleSafe(null));
	}

	/**
	 * Test very long strings to ensure no buffer overflow
	 */
	@Test
	public void testLongStrings() {
		StringBuilder longString = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			longString.append("This is a very long string that needs to be encoded properly. ");
		}
		String input = longString.toString();
		String encoded = Base64DoubleUrlEncodeSafe.encode(input);
		String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
		assertEquals("Long string should be encoded and decoded correctly", input, decoded);
	}

	/**
	 * Test boundary conditions for URL encoding
	 */
	@Test
	public void testUrlEncodingBoundaries() {
		String[] boundaryStrings = {
			"http://example.com/path?param=value&other=value2",
			"http://example.com/path#fragment",
			"http://example.com/path;param=value",
			"http://example.com/path space/file.txt",
			"http://example.com/path%20space/file.txt"
		};

		for (String input : boundaryStrings) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			try {
				String urlEncoded = java.net.URLEncoder.encode(encoded, StandardCharsets.UTF_8.name());
				assertEquals("URL encoding should not change the encoded string", encoded, urlEncoded);
				
				String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
				assertEquals("URL with special characters should round-trip correctly", input, decoded);
			} catch (java.io.UnsupportedEncodingException e) {
				fail("Unexpected encoding exception: " + e.getMessage());
			}
		}
	}

	/**
	 * Test that the encoder properly handles all ASCII characters
	 */
	@Test
	public void testAllAsciiCharacters() {
		StringBuilder asciiChars = new StringBuilder();
		// Generate all ASCII characters from 0 to 127
		for (int i = 0; i < 128; i++) {
			asciiChars.append((char) i);
		}
		String input = asciiChars.toString();
		String encoded = Base64DoubleUrlEncodeSafe.encode(input);
		String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
		assertEquals("All ASCII characters should be encoded and decoded correctly", input, decoded);
	}

	/**
	 * Tests handling of surrogate pairs in Unicode strings
	 */
	@Test
	public void testSurrogatePairs() {
		String[] surrogatePairs = {
			new String(new int[] { 0x1F600 }, 0, 1),  // 😀 Grinning Face
			new String(new int[] { 0x1F64F }, 0, 1),  // 🙏 Folded Hands
			new String(new int[] { 0x1F3F3 }, 0, 1)   // 🏳 White Flag
		};
		
		for (String input : surrogatePairs) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
			assertEquals("Surrogate pairs should be preserved", input, decoded);
		}
	}
	
	/**
	 * Tests handling of mixed ASCII and Unicode content
	 */
	@Test
	public void testMixedContent() {
		String[] mixedStrings = {
			"Hello 世界",
			"Test123 🌟 Star",
			"Mixed ASCII and ひらがな"
		};
		
		for (String input : mixedStrings) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
			assertEquals("Mixed content should be preserved", input, decoded);
			
			// Verify URL safety
			assertFalse("Encoded string should not contain URL-unsafe characters",
				encoded.contains("+") || encoded.contains("/") || encoded.contains("="));
		}
	}

}


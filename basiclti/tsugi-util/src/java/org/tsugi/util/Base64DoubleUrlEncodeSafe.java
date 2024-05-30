/**
 * When complex strings with URL sensitive characters are passed as URL parameters we could use
 * URLEncoding, but sometimes we end up double URL Encoding and that is generally seen as a security
 * problem and some firewalls remove double URL Encoding.
 *
 * And Base64 has a URL safe encoding - but it uses '=' for padding which does not survive double URL
 * Encoding.
 *
 * So our solution is to manually map '=' to '.' which produces strings which do not change when URL
 * Encoded - so double URL Encoding can be avoided.  The use of '.' as one of the "encoded" characters
 * is common in JWT encoders.  Sadly, we cannot use JWT encoder and decoder because it is specialized
 * for JSON so we cannot use it here.  Also, since the only compatibility that JWT encoders need to work
 * with are JWT decoders, so JWT dispenses with padding completely - hence you will never see an '='
 * in an encoded JWT (https://jwt.io/)
 */

package org.tsugi.util;

public class Base64DoubleUrlEncodeSafe {

	// https://datatracker.ietf.org/doc/html/rfc4648#section-3.2
	public static final char CHARACTER_TO_AVOID = '=';
	public static final char REPLACEMENT_CHARACTER = '.';

    /**
     * Encodes a given string into a Base64-Double-UrlEncode-Safe string.
     *
     * @param data The input string to be encoded.
     * @return The Base64-Double-UrlEncode-Safe encoded string.
     */
    public static String encode(String data) {
		if ( data == null ) return null;
		try {
			String encoded = java.util.Base64.getUrlEncoder().encodeToString(data.getBytes("UTF-8"));
			return encoded.replace(CHARACTER_TO_AVOID, REPLACEMENT_CHARACTER);
		} catch (java.io.UnsupportedEncodingException e ) {
			// Unlikely
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Decodes a Base64-Double-UrlEncode-Safe string into its original string representation.
     *
     * @param encoded The Base64-Double-UrlEncode-Safe string to be decoded.
     * @return The decoded original string.
     * @throws java.io.UnsupportedEncodingException If the input string contains invalid characters.
     */
    public static String decode(String encoded) {
		if ( encoded == null ) return null;
		return new String(java.util.Base64.getUrlDecoder().decode(encoded.replace(REPLACEMENT_CHARACTER, CHARACTER_TO_AVOID)));
    }

}

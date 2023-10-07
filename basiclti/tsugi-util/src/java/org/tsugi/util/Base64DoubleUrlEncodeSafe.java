/**
 * When complex strings with URL sensitive characters are passed as URL parameters we could use
 * URLEncoding, but sometimes we end up double URL Encoding and that is generally seen as a security
 * problem and some firewalls remove double URL Encoding.
 *
 * And Base64 has a URL safe encoding - but it uses '=' for padding which does not survive double URL
 * Encoding. 
 *
 * So our solution is to manually map '=' to '_' which produces strings which do not change when URL
 * Encoded - so double URL Encoding can be avoided.
 */

package org.tsugi.util;

public class Base64DoubleUrlEncodeSafe {

    /**
     * Encodes a given string into a Base64 Double UrlEncode Safe string.
     *
     * @param data The input string to be encoded.
     * @return The Base64 Double UrlEncode Safe encoded string.
     */
    public static String encode(String data) {
		if ( data == null ) return null;
		try {
			String encoded = java.util.Base64.getUrlEncoder().encodeToString(data.getBytes("UTF-8"));
			return encoded.replace('=', '_');
		} catch (java.io.UnsupportedEncodingException e ) {
			// unlikely 
			return null;
		}
    }

    /**
     * Decodes a Base64 Double UrlEncode Safe string into its original string representation.
     *
     * @param base62 The Base64 Double UrlEncode Safe string to be decoded.
     * @return The decoded original string.
     * @throws IllegalArgumentException If the input string contains invalid characters.
     */
    public static String decode(String decoded) {
		if ( decoded == null ) return null;
		return new String(java.util.Base64.getUrlDecoder().decode(decoded.replace('_', '=')));
    }

}

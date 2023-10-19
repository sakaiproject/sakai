/**
 * This class provides utility methods for encoding and decoding data using the Base62 encoding scheme.
 * Base62 is a binary-to-text encoding scheme that represents binary data using a set of 62 alphanumeric characters (A-Z, a-z, and 0-9).
 * It is useful for converting binary data (e.g., byte arrays) into strings that are safe for use in URLs and other text-based contexts.
 */

/* This code was written using the following ChatGPT prompts:
 *
 * Can you write a base62 encoder and decoder in java
 * Could you make this encode and decode strings and not numbers
 * Please complete the implementation details
 * Please write the JavaDoc for this class.
 * Please write a unit test for this class.
 */

package org.tsugi.util;

import java.util.HashMap;
import java.util.Map;

public class Base62 {

    /**
     * The characters used for Base62 encoding.
     */
    private static final String BASE62_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * A mapping of characters to their corresponding index in the Base62 character set.
     */
    private static final Map<Character, Integer> CHAR_TO_INDEX_MAP = new HashMap<>();

    /**
     * Initializes the CHAR_TO_INDEX_MAP with character-to-index mappings for Base62 characters.
     */
    static {
        for (int i = 0; i < BASE62_CHARS.length(); i++) {
            CHAR_TO_INDEX_MAP.put(BASE62_CHARS.charAt(i), i);
        }
    }

    /**
     * Encodes a given string into a Base62-encoded string.
     *
     * @param data The input string to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encode(String data) {
		if ( data == null ) return null;
        byte[] bytes = data.getBytes();
        StringBuilder encoded = new StringBuilder();
        long value = 0;
        int bits = 0;

        for (byte b : bytes) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;

            while (bits >= 6) {
                int index = (int) ((value >> (bits - 6)) & 0x3F);
                encoded.append(BASE62_CHARS.charAt(index));
                bits -= 6;
            }
        }

        if (bits > 0) {
            int index = (int) ((value << (6 - bits)) & 0x3F);
            encoded.append(BASE62_CHARS.charAt(index));
        }

        return encoded.toString();
    }

    /**
     * Decodes a Base62-encoded string into its original string representation.
     *
     * @param base62 The Base62-encoded string to be decoded.
     * @return The decoded original string.
     * @throws IllegalArgumentException If the input string contains invalid Base62 characters.
     */
    public static String decode(String base62) throws IllegalArgumentException {

		if ( base62 == null ) return null;

        long value = 0;
        int bits = 0;
        byte[] bytes = new byte[base62.length() * 6 / 8];
        int byteIndex = 0;

        for (int i = 0; i < base62.length(); i++) {
            char c = base62.charAt(i);
            int charValue = CHAR_TO_INDEX_MAP.getOrDefault(c, -1);
            if (charValue == -1) {
                throw new IllegalArgumentException("Invalid character in base62 string: " + c);
            }

            value = (value << 6) | charValue;
            bits += 6;

            while (bits >= 8) {
                bytes[byteIndex++] = (byte) ((value >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }

        return new String(bytes, 0, byteIndex);
    }

    /**
     * Decodes a Base62-encoded string into its original string representation or returning the original string
     *
	 * This is a way to easily recover from a double decode.  This is a little soft in its approach
	 * and only works if the strings being encoded and decoded have non-base62 characters.
	 *
     * @param base62 The Base62-encoded string to be decoded.
     * @return The decoded original string or the original string if the input string is not Base62
     */
    public static String decodeSafe(String base62) throws IllegalArgumentException {

		if ( base62 == null ) return null;

       try {
            return decode(base62);
        } catch (IllegalArgumentException e) {
			return base62;
        }
    }

}

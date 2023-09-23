package org.tsugi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.tsugi.util.Base62;

/* This code was written by ChatGPT with the following prompt
 * Please write a unit test for a Base62 class
 */

public class Base62Test {

    @Test
    public void testEncodeAndDecode() {
        String[] testStrings = {
            "Hello, Base62!",
            "Testing encoding and decoding.",
            "12345",
            "This is a longer string with more characters.",
            "A tree 🌲 was here",
            "A",
            ""
        };

        for (String input : testStrings) {
            String encoded = Base62.encode(input);
            String decoded = Base62.decode(encoded);

            assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);
        }

		assertEquals("Encode of null should produce null: ", null, Base62.encode(null));
		assertEquals("Decode of null should produce null: ", null, Base62.decode(null));
    }

    @Test
    public void testInvalidCharacters() {
        // Test invalid characters in decoding
        String invalidBase62String = "InvalidString$#@!";
        try {
            Base62.decode(invalidBase62String);
            fail("Expected IllegalArgumentException for invalid Base62 string");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
		assertEquals("Encode of null should produce null: ", null, Base62.encode(null));
    }

    @Test
    public void testInvalidCharactersSafe() {
        // Test invalid characters in decoding
        String invalidBase62String = "InvalidString$#@!";
        String decoded = Base62.decodeSafe(invalidBase62String);

		assertEquals("Safe decode of non-base22 string should return the string: ", invalidBase62String, decoded);

        String encoded = Base62.encode(invalidBase62String);
        decoded = Base62.decodeSafe(encoded);
		assertEquals("Safe decode of base22 string should return the decoded string: ", invalidBase62String, decoded);
    }
}


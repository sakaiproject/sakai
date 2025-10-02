/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for UrlUtility class
 */
public class UrlUtilityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEncodeDecodeRoundTrip() {
        // This test covers the logic that was previously in the main() method
        String sample = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï¶Ä©úÆûÂÉ¾Ö³²µ÷ÃÅ½";
        
        String encoded = UrlUtility.encodeUrl(sample);
        String decoded = UrlUtility.decodeUrl(encoded);
        
        // Test that encode/decode is a proper round-trip operation
        assertEquals("Encode/decode should be inverse operations", sample, decoded);
        
        // Verify that encoding actually changes the string (not a no-op)
        assertNotEquals("Encoding should change the string", sample, encoded);
    }

    @Test
    public void testEncodeDecodeWithSpecialCharacters() {
        // Test with various special characters that might cause issues
        String[] testStrings = {
            "simple",
            "with spaces",
            "with/slashes",
            "with+plus",
            "with=equals",
            "with?query=param",
            "with#fragment",
            "with%percent",
            "with&ampersand",
            "with\"quotes\"",
            "with'single'quotes",
            "with<tags>",
            "with[ brackets ]",
            "with{ braces }",
            "with| pipe |",
            "with\\ backslash",
            "with\n newline",
            "with\t tab",
            "with unicode: 中文 العربية русский",
            "" // empty string
        };
        
        for (String testString : testStrings) {
            String encoded = UrlUtility.encodeUrl(testString);
            String decoded = UrlUtility.decodeUrl(encoded);
            assertEquals("Round-trip should work for: " + testString, testString, decoded);
        }
    }

    @Test
    public void testRawEncodeDecode() {
        String sample = "test string";
        
        String encoded = UrlUtility.rawEncodeUrl(sample);
        String decoded = UrlUtility.rawDecodeUrl(encoded);
        
        assertEquals("Raw encode/decode should be inverse operations", sample, decoded);
    }

    @Test
    public void testCleanAndRepairUrl() {
        // Test with a Base64-encoded string that naturally has padding
        // This simulates what would happen in real usage
        String original = "dGVzdCtzdHJpbmcvd2l0aHNsYXNoZXM="; // Base64 for "test+string/withslashes"
        
        String cleaned = UrlUtility.cleanUrl(original);
        String repaired = UrlUtility.repairUrl(cleaned);
        
        assertEquals("Clean and repair should be inverse operations", original, repaired);
    }
}

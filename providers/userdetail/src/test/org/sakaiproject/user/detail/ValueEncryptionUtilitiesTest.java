/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.user.detail;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Check our encryption is reasonably sensible.
 */
public class ValueEncryptionUtilitiesTest {

    private ValueEncryptionUtilities service;

    @Before
    public void setUp() throws Exception {
        service = new ValueEncryptionUtilities();
        service.setKey("aas");
        service.init();
    }

    // Check that when we encrypt the same value twice we get different outputs.
    @Test
    public void testEncryptDifferent() {
        String o1 = service.encrypt("hello", 10);
        String o2 = service.encrypt("hello", 10);
        Assert.assertNotEquals(o1, o2);
    }

    // This should be reasonably quick as we will be doing lots of encryption on some requests.
    // Currently it's taking 4 seconds.
    @Test
    public void testRoundsEncryption() {
        for (int i = 0; i < 50000; i++) {
            String encrypted = service.encrypt("hello", 10);
            String decrypted = service.decrypt(encrypted);
            Assert.assertEquals("hello", decrypted);
        }
    }

    @Test
    public void testRoundTrip() {
        String encrypted = service.encrypt("Cheese", 10);
        String decrypted = service.decrypt(encrypted);
        Assert.assertEquals("Cheese", decrypted);
    }

    @Test
    public void testRoundTripSmall() {
        String encrypted = service.encrypt("", 10);
        String decrypted = service.decrypt(encrypted);
        Assert.assertEquals("", decrypted);
    }


    @Test
    public void testUnicode() {
        String original = "שלום עולם";
        String enrypted = service.encrypt(original, 20);
        String decrypted = service.decrypt(enrypted);
        Assert.assertEquals(original, decrypted);
    }

    @Test
    public void testSameLengths() {
        int empty = service.encrypt("", 26).length();
        int alphabet = service.encrypt("abcdefghijklmnopqrstuvwxyz", 26).length();
        Assert.assertEquals(empty, alphabet);
    }

    @Test
    public void testDifferentLengths() {
        // The difference has to be reasonable because of the AES block size which pads out small changes.
        int small = service.encrypt("hello", 5).length();
        int large = service.encrypt("hello", 25).length();
        Assert.assertNotEquals(small, large);
    }

    @Test
    public void testNoPadding() {
        int empty = service.encrypt("a", 0).length();
        int alphabet = service.encrypt("abcdefghijklmnopqrstuvwxyz", 0).length();
        Assert.assertNotEquals(empty, alphabet);
    }

    @Test
    public void testFailedDecryption() {
        Assert.assertNull(service.decrypt("noGoodCipherText"));
    }
}
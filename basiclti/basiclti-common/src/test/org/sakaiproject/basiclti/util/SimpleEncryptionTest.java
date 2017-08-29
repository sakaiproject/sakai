/**
 * Copyright (c) 2009-2017 The Apereo Foundation
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
package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SimpleEncryptionTest {


	private String CIPHER = "AES/CBC/PKCS5Padding";
	// Result of SimpleEncryption.encrypt("key", "plain text"));
	private String goodEncrypt = "0bdd94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;
	private String goodEncrypt3 = "0bdd94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f";
	private String badEncryptLength1 = "0bdd94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af:"+CIPHER;
	private String badEncryptLength2 = "dd94442e437fac:d8e4bee67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;
	private String badEncryptNotHex = "0bzz94442e437fac:d8e4be4ae67a7bdf8f0717cebf425832:133df2f919b2e686a0c4ed5451b5af6f:"+CIPHER;

	@Before
	public void setUp() throws Exception {
	}

	
	@Test(expected=Exception.class)
	public void testBadDecrypt() {
		SimpleEncryption.decrypt("a key", "bad text");
	}
	
	@Test(expected=Exception.class)
	public void testBadDecryptButSplits() {
		SimpleEncryption.decrypt("key", "salt:iv:data:"+CIPHER);
	}

	@Test(expected=Exception.class)
	public void testBadDecryptButSplits3() {
		SimpleEncryption.decrypt("key", "salt:iv:data");
	}
	
	
	@Test(expected=Exception.class)
	public void testBadDecryptLength1() {
		SimpleEncryption.decrypt("key", badEncryptLength1);
	}

	@Test(expected=Exception.class)
	public void testBadDecryptLength2() {
		SimpleEncryption.decrypt("key", badEncryptLength2);
	}

	@Test(expected=Exception.class)
	public void testBadDecryptNotHex() {
		SimpleEncryption.decrypt("key", badEncryptNotHex);
	}

	@Test()
	public void testBadKey() {
		String encrypt;
		// W
		for (int i = 0; i < 100; i++) {
			try {
				encrypt = SimpleEncryption.encrypt("goodkey", "Hello");
				// Sometimes this won't fail but the data we get out isn't good.
				String decrypt = SimpleEncryption.decrypt("badkey", encrypt);
				assertNotEquals("Hello", decrypt);
			} catch (Exception e) {
				// This is expected.
			}
		}
	}

	@Test
	public void test() {
		assertNotNull(SimpleEncryption.encrypt("key", "Hello").length());
		assertFalse("Hello".equals(SimpleEncryption.encrypt("key", "Hello")));
		assertEquals("Hello", SimpleEncryption.decrypt("key", SimpleEncryption.encrypt("key", "Hello")));
		assertEquals("plain text", SimpleEncryption.decrypt("key", goodEncrypt));
		assertFalse("wrong text".equals(SimpleEncryption.decrypt("key", goodEncrypt)));
	}
	
	@Test
	public void testSalted() {
		assertFalse(SimpleEncryption.encrypt("key", "Hello").equals(SimpleEncryption.encrypt("key", "Hello")));
	}
	
	@Test
	public void testLongerString() {
		String longer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus sit amet lectus et mi dictum dapibus. Aenean ac nibh non leo tristique ultricies ac eu tellus. Phasellus posuere arcu sollicitudin massa fringilla viverra. Donec sodales orci id odio dignissim eget bibendum odio rutrum. Cras id sem eget felis consequat bibendum. Nam id mauris nec lacus condimentum semper. Nullam eleifend risus et dui aliquam ut luctus tortor suscipit. Proin porta tellus tortor, eu pellentesque ante. Aenean sem eros, porttitor nec laoreet et, aliquam a justo. Vivamus condimentum risus quis lorem sodales viverra. Vivamus mollis lacinia congue. Nulla imperdiet enim sit amet ante sodales in euismod ante porta. Fusce in ullamcorper dolor. Nulla facilisi. Nullam risus nisi, pellentesque at convallis id, commodo a orci. Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		String encrypted = SimpleEncryption.encrypt("key", longer);
		assertEquals(longer, SimpleEncryption.decrypt("key", encrypted));
	}
	
	@Test
	public void testNullEncrypt() {
		assertNull(SimpleEncryption.encrypt("key", null));
	}
	
	@Test
	public void testNullDecrypt() {
		assertNull(SimpleEncryption.decrypt("key", null));
	}
}

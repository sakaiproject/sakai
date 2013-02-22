package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.basiclti.util.SimpleEncryption;


public class SimpleEncryptionTest {

	@Before
	public void setUp() throws Exception {
	}

	
	@Test(expected=Exception.class)
	public void testBadDecrypt() {
		SimpleEncryption.decrypt("a key", "bad text");
	}
	
	@Test(expected=Exception.class)
	public void testBadDecryptButSplits() {
		SimpleEncryption.decrypt("key", "salt:iv:data");
	}
	
	@Test(expected=Exception.class)
	public void testBadKey() {
		SimpleEncryption.decrypt("badkey", SimpleEncryption.encrypt("goodkey", "Hello"));
	}

	@Test
	public void test() {
		assertNotNull(SimpleEncryption.encrypt("key", "Hello").length());
		assertFalse("Hello".equals(SimpleEncryption.encrypt("key", "Hello")));
		assertEquals("Hello", SimpleEncryption.decrypt("key", SimpleEncryption.encrypt("key", "Hello")));
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

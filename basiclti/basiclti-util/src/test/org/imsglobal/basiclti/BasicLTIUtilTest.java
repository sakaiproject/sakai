package org.imsglobal.basiclti;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import org.imsglobal.basiclti.BasicLTIUtil;


public class BasicLTIUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetRealPath() {
        String fixed = BasicLTIUtil.getRealPath("http://localhost/path/blah/", "https://right.com");
        assertEquals("https://right.com/path/blah/",fixed);
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com");
        assertEquals("https://right.com/path/blah/",fixed);
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "http://right.com");
        assertEquals("http://right.com/path/blah/",fixed);

        // Test folks sending in URL with extra stuff...
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com/path/blah");
        assertEquals("https://right.com/path/blah/",fixed);
	}

	@Test
	public void testMergeCustom() {
		Properties custom = new Properties();
		BasicLTIUtil.mergeLTI1Custom(custom,"key=val;key2=keepme;");
		BasicLTIUtil.mergeLTI1Custom(custom,"key3=val3;key2=ignoreme;");
		BasicLTIUtil.mergeLTI2Custom(custom,"{ \"isbn\" : \"978-0321558145\", \"user_id\" : \"$User.id\"}");
		BasicLTIUtil.mergeLTI2Custom(custom,"{ \"isbn\" : \"i-ignoreme\", \"user_id\" : \"s-ignoreme\"}");
		BasicLTIUtil.mergeLTI2Parameters(custom,"[ { \"name\" : \"result_url\", \"variable\" : \"Result.url\" }, { \"name\" : \"discipline\", \"fixed\" : \"chemistry\" } ]");
		assertEquals("978-0321558145",custom.getProperty("isbn"));
		assertEquals("$User.id",custom.getProperty("user_id"));
		assertEquals("$Result.url",custom.getProperty("result_url"));
		assertEquals("keepme",custom.getProperty("key2"));
		assertEquals("val",custom.getProperty("key"));
		assertEquals("val3",custom.getProperty("key3"));
		System.out.println("Custom="+custom);
    }
	
}

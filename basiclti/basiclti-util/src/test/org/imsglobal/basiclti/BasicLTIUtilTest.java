package org.imsglobal.basiclti;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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
	
}

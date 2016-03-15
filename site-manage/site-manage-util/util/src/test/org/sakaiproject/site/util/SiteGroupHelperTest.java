package org.sakaiproject.site.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.sakaiproject.site.util.SiteGroupHelper.*;

/**
 * @author Matthew Buckett
 */
public class SiteGroupHelperTest {

	@Test
	public void testRoundTripSimple() {
		testRoundTrip("A", "B");
	}

	@Test
	public void testRoundTripEmpty() {
		testRoundTrip("", "", "A", "");
	}

	@Test
	public void testRoundTripNothing() {
		testRoundTrip("");
	}

	@Test
	public void testRoundTripSeperator() {
		testRoundTrip(SEPARATOR_STR, "A", SEPARATOR_STR);
	}

	@Test
	public void testRoundTripEscape() {
		testRoundTrip(ESCAPE_STR, "A", ESCAPE_STR);
	}

	@Test
	public void testRoundTripEmbedded() {
		testRoundTrip("Hello", SEPARATOR_STR +"middle"+ SEPARATOR_STR, ESCAPE_STR+"middle"+ESCAPE_STR);
	}

	@Test
	public void testRoundTripMess() {
		testRoundTrip(SEPARATOR_STR+ESCAPE_STR+ESCAPE_STR+SEPARATOR_STR);
	}

	public void testRoundTrip(String... parts) {
		Collection<String> source = Arrays.asList(parts);
		String packed = pack(source);
		Collection<String> unpacked = unpack(packed);
		assertEquals("RoundTriped value was : "+ packed +"\n", source, unpacked);
	}

	@Test
	public void testPackNull() {
		assertNull(pack(null));
	}

	@Test
	public void testUnpackNull() {
		assertEquals(Collections.emptyList(), unpack(null));
	}
}

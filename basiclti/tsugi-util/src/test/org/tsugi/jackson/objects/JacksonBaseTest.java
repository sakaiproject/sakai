package org.tsugi.jackson.objects;

import org.tsugi.jackson.objects.JacksonBase;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.Map;

public class JacksonBaseTest {

	@Test
	public void testOne() {
		JacksonBase jb = new JacksonBase();
		String o1 = jb.prettyPrintLog();
		assertEquals(o1, "{ }");
		assertNull(jb.getKey("answer"));
		jb.setKey("answer", "42");
		assertNotNull(jb.getKey("answer"));
		assertEquals(jb.getKey("answer"), "42");
		o1 = jb.prettyPrintLog();
		assertEquals(o1, "{\n  \"answer\" : \"42\"\n}");
		Map<String, Object> props = jb.getAdditionalProperties();
		assertEquals(props.size(), 1);
		assertEquals(props.get("answer"), "42");
		assertNull(props.get("else"));
	}
}

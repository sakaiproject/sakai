package org.tsugi.jackson;

import static org.junit.Assert.*;

import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class JacksonUtilTest {

	@Test
	public void testOne()
			throws com.fasterxml.jackson.core.JsonParseException, java.io.IOException,
			com.fasterxml.jackson.core.JsonProcessingException {
		String jsonString = "{\"k1\":\"v1\",\"k2\":\"v2\"}";

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(jsonString);

		assertNotNull(actualObj);

		String ljs = JacksonUtil.toString(actualObj);
		boolean good = ljs.equals(jsonString);
		if (!good) {
			System.out.println("Bad Payload: " + ljs);
		}
		assertTrue(good);

		String ljsp = JacksonUtil.prettyPrint(actualObj);
		good = ljsp.contains("v2");
		if (!good) {
			System.out.println("Bad pretty payload: " + ljsp);
		}
		assertTrue(good);

	}
}

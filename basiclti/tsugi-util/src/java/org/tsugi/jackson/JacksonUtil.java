
package org.tsugi.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * Some Tsugi Utility code for to make using Jackson easier to use.
 */
@SuppressWarnings("deprecation")
@Slf4j
public class JacksonUtil {

	// http://stackoverflow.com/questions/6176881/how-do-i-make-jackson-pretty-print-the-json-content-it-generates
	public static String prettyPrint(Object obj) 
		throws com.fasterxml.jackson.core.JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();

		// ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
		// ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		// return mapper.writeValueAsString(obj);

		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		return writer.writeValueAsString(obj);
	}

	public static String prettyPrintLog(Object obj) 
	{
		try {
			return prettyPrint(obj);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}

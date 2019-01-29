/*
 * Copyright (c) 2015- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */
package org.tsugi.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
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
			throws com.fasterxml.jackson.core.JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		// ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
		// ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		// return mapper.writeValueAsString(obj);
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		return writer.writeValueAsString(obj);
	}

	public static String prettyPrintLog(Object obj) {
		try {
			return prettyPrint(obj);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static String toString(Object jackson) {
		// https://www.baeldung.com/jackson-object-mapper-tutorial
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String retval = objectMapper.writeValueAsString(jackson);
			return retval;
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}

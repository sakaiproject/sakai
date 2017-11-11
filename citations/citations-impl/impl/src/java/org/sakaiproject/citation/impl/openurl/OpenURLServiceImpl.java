/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.citation.impl.openurl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.impl.openurl.ContextObject.Entity;

/**
 * This class exposes the services for OpenURLs, this isn't in the API as at the moment it
 * doesn't need to be used outside the citations implementation.
 * Most of the logic is in implementations and other classes.
 * @author buckett
 *
 */

public class OpenURLServiceImpl {

	private List<Transport> transports;
	private List<Format> formats;
	private List<Converter> converters;
	private Format urlFormat;
	private Transport urlTransport;
	
	public void setTransports(List<Transport> transports) {
		this.transports = transports;
	}

	public void setFormats(List<Format> formats) {
		this.formats = formats;
	}
	
	public void setConverters(List<Converter> converters) {
		this.converters = converters;
	}
	
	public void init() {
		// Find the 
		for(Transport transport: transports) {
			if (transport instanceof InlineHttpTransport) {
				urlTransport = transport;
				break;
			}
		}
		for(Format format : formats) {
			if (format.canHandle(KEVFormat.FORMAT_ID)) {
				urlFormat = format;
				break;
			}
		}
	}

	public ContextObject parse(HttpServletRequest request) {
		ContextObject contextObject = null;
		for(Transport transport : transports) {
			RawContextObject rawContextObject = transport.parse(request);
			if (rawContextObject != null && rawContextObject.getData() != null) {
				
				for (Format format: formats) {
					contextObject = format.parse(rawContextObject.getData());
					if (contextObject != null) {
						// TODO Should validate it.
						break;
					}
				}
			}
		}
		return contextObject;
	}
	
	/**
	 * This Converts a citation into a context object.
	 * @param citation
	 * @return
	 */
	public ContextObject convert(Citation citation) {
		ContextObject contextObject = null;
		if (citation != null && citation.getSchema() != null) {
			String schema = citation.getSchema().getIdentifier();
			for(Converter converter: converters) {
				if (converter.canConvertCitation(schema)) {
					contextObject = new ContextObject();
					contextObject.getEntities().put(Entity.REFERENT, converter.convert(citation));
					break;
				}
				
			}
		}
		return contextObject;
	}
	
	public Citation convert(ContextObject contextObject) {
		Citation citation = null;
		if (contextObject != null) {
			ContextObjectEntity referent = contextObject.getEntity(Entity.REFERENT);

			if (referent != null) {
				String format = referent.getFormat();
				for(Converter converter: converters) {
					if (converter.canConvertOpenUrl(format)) {
						citation = converter.convert(referent);
						// Basic validation.
						if (citation.hasCitationProperty("title")) {
							break;
						}
					}
				}
				if (citation == null) {
					// None of the converters matched.
					
				}
			}
		}
		return citation;
	}
	
	/**
	 * Generate an OpenURL based on the passed ContextObject.
	 * @param contextObject
	 * @return
	 */
	public String toURL(ContextObject contextObject) {
		String encodedCO = urlFormat.encode(contextObject);
		String url = urlTransport.encode(encodedCO);
		return url;
	}
}

/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.citation.impl.openurl.ContextObject.Entity;

/**
 * Class to deal with Key Encoded Value format of OpenURLs.
 * @author buckett
 */
@Slf4j
public class KEVFormat implements Format {
	private static List<EntityFormatter> entityFormatters = new ArrayList<EntityFormatter>();
	
	public static String FORMAT_ID = "info:ofi/fmt:kev:mtx:ctx";

	static {
		entityFormatters.add(new EntityFormatter("rfe", Entity.REFERRING_ENTITY));
		entityFormatters.add(new EntityFormatter("rft", Entity.REFERENT));
		entityFormatters.add(new EntityFormatter("req", Entity.REQUESTOR));
		entityFormatters.add(new EntityFormatter("svc", Entity.SERVICE_TYPE));
		entityFormatters.add(new EntityFormatter("res", Entity.RESOLVER));
		entityFormatters.add(new EntityFormatter("rfr", Entity.REFERRER));
	}

	public boolean canHandle(String format) {
		return FORMAT_ID.equals(format);
	}

	public ContextObject parse(String data) {
		if (data == null) {
			return null;
		}
		// We don't rely on the container todo this as it will probably decode it as 
		// UTF-8 or ISO-8859-1 which might not be correct.
		Map<String, String[]> source = Utils.split(data);
		String encoding = Utils.getValue(source, "ctx_enc");
		if (encoding == null) {
			encoding = "UTF-8";
		} else {
			// Primo OpenURLs contain a bug where they specify the encoding incorrectly.
			if (encoding.startsWith("info:ofi/enc:")) {
				encoding = encoding.substring("info:ofi/enc:".length());
			}
		}
		try {
			source = Utils.decode(source, encoding);
		} catch (IllegalArgumentException iae) {
			log.warn("Specified decoding failed. "+ iae.getMessage());
			// Fallback to UTF-8 so we can try and get somewhere.
			source = Utils.decode(source, "UTF-8");
		}
		ContextObject contextObject = new ContextObject();
		// Need to put them into a map so we know the encoding first.
		for(Map.Entry<String, String[]> entry: source.entrySet()) {
			
			String key = entry.getKey();
			for (String value: entry.getValue()) {
				if (key == null || key.length() < 1) {
					log.debug("Ignoring empty key with value: "+ value);
					continue;
				}
				if (key.equals("ctx_ver")) {
					if (ContextObject.VERSION.equals(value)) {
						log.warn("Context object doesn't match required version, continuing anyway.");
					}
				} else if (key.equals("ctx_tim")) {
					// The time.
				} else if (key.equals("ctx_id")) {
					// The ID.
				}
				for(EntityFormatter parser: entityFormatters) {
					// See if anyone wants to claim this key.
					if (parser.canParse(key)) {
						parser.parse(contextObject, key, value);
						break;
					}
				}
			}
		}
		return contextObject;
		
	}

	public String encode(ContextObject contextObject) {
		StringBuilder output = new StringBuilder();
		output.append("&"+"ctx_id"+ "="+ ContextObject.VERSION);
		output.append("&"+"ctx_enc"+ "="+ "UTF-8");
		for(EntityFormatter encoder: entityFormatters) {
			String coEncoded = encoder.encode(contextObject);
			if (coEncoded != null && coEncoded.length()>0) {
				output.append('&');
				output.append(coEncoded);
			}
		}
		return output.toString();
	}
	
	/**
	 * Parser/formatter for an entity.
	 * @author buckett
	 *
	 */
	private static class EntityFormatter {
		
		private static final String DAT_SUFFIX = "_dat";
		private static final String REF_SUFFIX = "_ref";
		private static final String REF_FMT_SUFFIX = "_ref_fmt";
		private static final String VAL_FMT_SUFFIX = "_val_fmt";
		private static final String ID_SUFFIX = "_id";
		
		private String prefix;
		private Entity type;
		
		public EntityFormatter(String prefix, Entity type) {
			this.type = type;
			this.prefix = prefix;
		}
		
		public boolean canParse(String key) {
			return key.startsWith(prefix);
		}
				
		public void parse(ContextObject co, String key, String value) {
			String keyTail = key.substring(prefix.length());
			ContextObjectEntity entity = co.getEntities().get(type);
			if (entity == null) {
				entity = new ContextObjectEntity();
				co.getEntities().put(type, entity);
			}
			if (keyTail.equals(ID_SUFFIX)) {
				entity.addId(value);
			} else if (keyTail.equals(VAL_FMT_SUFFIX)) {
				entity.setFormat(value);
			} else if (keyTail.equals(REF_FMT_SUFFIX)) {
				entity.setRefFormat(value);
			} else if (keyTail.equals(REF_SUFFIX)) {
				entity.setRef(value);
			} else if (keyTail.equals(DAT_SUFFIX)) {
				entity.setData(value);
			} else if (keyTail.startsWith(".")) {
				String metaKey = keyTail.substring(1);
				if (metaKey.length() > 0) {
					entity.addValue(metaKey, value);
				}
			} else {
				log.debug("Ignoring unreconised key: "+ key);
			}
		}
		
		public String encode(ContextObject co) {
			ContextObjectEntity entity = co.getEntities().get(type);
			if (entity == null){
				return "";
			}
			URLBuilder output = new URLBuilder("UTF-8");
			for (String id: entity.getIds()) {
				addKEV(output, ID_SUFFIX, id);
			}
			addKEV(output, VAL_FMT_SUFFIX, entity.getFormat());
			addKEV(output, REF_FMT_SUFFIX, entity.getRefFormat());
			addKEV(output, REF_SUFFIX, entity.getRef());
			addKEV(output, DAT_SUFFIX, entity.getData());
			for(Entry<String, List<String>> entry: entity.getValues().entrySet()) {
				for(String entryValue: entry.getValue()) {
					addKEV(output, "."+ entry.getKey(), entryValue);
				}
			}
			return output.toString();
		}
		
		public void addKEV(URLBuilder output, String key, Object value) {
			if (value != null) {
				output.append(prefix+key, value.toString());
			}
		}
	}
	

}

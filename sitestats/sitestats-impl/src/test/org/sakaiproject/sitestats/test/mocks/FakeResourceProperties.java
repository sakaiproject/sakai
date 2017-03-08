/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

public abstract class FakeResourceProperties implements ResourceProperties {
	private Map<String, String> map = new HashMap<String, String>();
	
	public void set(String displayName, boolean isCollection, String contentType) {
		map.put(PROP_DISPLAY_NAME, displayName);
		map.put(PROP_IS_COLLECTION, Boolean.toString(isCollection));
		map.put(PROP_CONTENT_TYPE, contentType);
	}
	public boolean getBooleanProperty(String key) throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
		return Boolean.parseBoolean(map.get(key));
	}
	public String getNamePropContentType() {
		return PROP_CONTENT_TYPE;
	}

	public String getNamePropIsCollection() {
		return PROP_IS_COLLECTION ;
	}

	public String getProperty(String key) {
		return map.get(key);
	}
}

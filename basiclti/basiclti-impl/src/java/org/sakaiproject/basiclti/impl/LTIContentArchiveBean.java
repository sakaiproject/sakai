/**
 * Copyright (c) 2009-2016 The Apereo Foundation
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
package org.sakaiproject.basiclti.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Slf4j
public class LTIContentArchiveBean {

        public final static String ALIAS = "LTIContent";

	private Map<String,Object> contents;

	private List<String> allowedFields = Arrays.asList(
		       	"id",
		       	"tool_id",
			"title",
			"description",
			"contentitem",
			"launch",
			"custom",
			"settings",
			"lti13",
			"lti13_settings",
			"newpage",
			"frameheight" );

	/* 
+--------------------+---------------+------+-----+---------+----------------+
| id                 | int(11)       | NO   | PRI | NULL    | auto_increment |
| tool_id            | int(11)       | YES  |     | NULL    |                |
| SITE_ID            | varchar(99)   | YES  |     | NULL    |                |
| title              | varchar(1024) | YES  |     | NULL    |                |
| frameheight        | int(11)       | YES  |     | NULL    |                |
| newpage            | tinyint(4)    | YES  |     | 0       |                |
| debug              | tinyint(4)    | YES  |     | 0       |                |
| custom             | mediumtext    | YES  |     | NULL    |                |
| launch             | text          | YES  |     | NULL    |                |
| xmlimport          | mediumtext    | YES  |     | NULL    |                |
| placement          | varchar(256)  | YES  |     | NULL    |                |
| created_at         | datetime      | NO   |     | NULL    |                |
| updated_at         | datetime      | NO   |     | NULL    |                |
| pagetitle          | varchar(1024) | YES  |     | NULL    |                |
| consumerkey        | varchar(1024) | YES  |     | NULL    |                |
| secret             | varchar(1024) | YES  |     | NULL    |                |
| settings           | mediumtext    | YES  |     | NULL    |                |
| placementsecret    | text          | YES  |     | NULL    |                |
| oldplacementsecret | text          | YES  |     | NULL    |                |
| resource_handler   | text          | YES  |     | NULL    |                |
| settings_ext       | mediumtext    | YES  |     | NULL    |                |
| FA_ICON            | varchar(1024) | YES  |     | NULL    |                |
| CONTENTITEM        | mediumtext    | YES  |     | NULL    |                |
| toolorder          | int(11)       | YES  |     | 0       |                |
| lti13              | tinyint(4)    | YES  |     | 0       |                |
| lti13_settings     | mediumtext    | YES  |     | NULL    |                |
| description        | mediumtext    | YES  |     | NULL    |                |
| protect            | tinyint(4)    | YES  |     | 0       |                |
+--------------------+---------------+------+-----+---------+----------------+
*/

        public LTIContentArchiveBean(Map<String,Object> itemFields)
        {
		// Create a new bean with a filtered list of items
		this.contents = new HashMap();

		for (String key : itemFields.keySet()) {
			if (allowedFields.contains(key)) {
				contents.put(key, itemFields.get(key));
			}
		}
        }
        
        public LTIContentArchiveBean(Node basicLTI) throws Exception
        {
		throw new UnsupportedOperationException("Not implemented");
        }

        public Node toNode(Document doc)
        {
        	Node node = null;
    		log.debug("Building node");

		// The alias is the name of the root element
		node = doc.createElement(LTIContentArchiveBean.ALIAS);
			
		// Set the id as attribute of the item element
		if (contents.get("id") != null) {
			Attr itemId = doc.createAttribute("id");
			itemId.setValue(contents.get("id").toString());
			node.getAttributes().setNamedItem(itemId);
		}

		for (String key: contents.keySet()) {
			Node property = doc.createElement(key);
			if (contents.get(key) != null) {
				property.setTextContent(contents.get(key).toString());
			}
			node.appendChild(property);
		}

        	return node;
        }
}

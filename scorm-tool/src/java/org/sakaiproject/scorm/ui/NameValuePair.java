/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui;

import java.io.Serializable;

public class NameValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String value;
	private String name;
	
	public NameValuePair(String value, String id) {
		this.value = value;
		this.name = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean equals(Object object) {
		if (object instanceof NameValuePair) {
			NameValuePair nvp = (NameValuePair)object;
			
			return name.equals(nvp.getName());
		}
		return false;
	}
	
	public String toString() {
		return name;
	}
}

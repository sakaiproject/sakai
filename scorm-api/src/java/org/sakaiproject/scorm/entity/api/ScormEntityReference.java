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
package org.sakaiproject.scorm.entity.api;

import org.sakaiproject.entitybroker.IdEntityReference;

public class ScormEntityReference extends IdEntityReference {
	public String key;
	
	public ScormEntityReference(String reference) {
		super(reference);
		key = getKey(reference);
	}

	public ScormEntityReference(String prefix, String id) {
		super(prefix, id);
	}
	
	public ScormEntityReference(String prefix, String id, String key) {
		super(prefix, id);
		this.key = key;
	}
	
	public static String getKey(String reference) {
		int spos = getKeySeparatorPos(reference);
		
		return spos == -1 ? "" : reference.substring(spos + 1);
		
		//int lspos = (reference.indexOf('/', spos + 1));
		//return lspos == -1? reference.substring(spos + 1):reference.substring(spos + 1, lspos);
	}
	
	public static int getKeySeparatorPos(String reference) {
		int firstSeparatorPos = getSeparatorPos(reference);
		
		if (reference == null || reference.length() < firstSeparatorPos + 1
				|| reference.charAt(0) != '/') {
			return -1;
		}
		
		return reference.indexOf('/', firstSeparatorPos + 1);
	}
	
	public String toString() {
		return new StringBuffer().append(prefix).append("/").append(id)
			.append("/").append(key).toString();
	}
	
}

/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sitemanage.impl;

import org.sakaiproject.sitemanage.api.SectionField;

public class SectionFieldImpl implements SectionField {
	protected String labelKey;
	protected String value;
	protected int maxSize;
	
	public SectionFieldImpl() {}
	public SectionFieldImpl(String labelKey, String value, int maxSize) {
		this.labelKey = labelKey;
		this.value = value;
		this.maxSize = maxSize;
	}
	
	public String getLabelKey() {
		return labelKey;
	}
	public void setLabelKey(String labelKey) {
		this.labelKey = labelKey;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

}

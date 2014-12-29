/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool;

/**
 * This class serves as an alternative to adding new columns to SimplePageItem,
 * which was getting a bit cumbersome.
 * 
 * You shouldn't have to modify anything in here, just get and set attributes
 * in SimplePageItem and they'll be saved in the database.
 *
 */

public class SimplePageItemAttributeImpl {
	private long id;
	private SimplePageItem itemId;
	private String attr;
	private String value;

	public SimplePageItemAttributeImpl() {}

	public SimplePageItemAttributeImpl(SimplePageItem itemId, String attr, String value) {
		this.itemId = itemId;
		this.attr = attr;
		this.value = value;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setItemId(SimplePageItem itemId) {
		this.itemId = itemId;
	}
	
	private SimplePageItem getItemId() {
		return itemId;
	}
	
	public boolean setAttr(String attr) {
		if(attr != null) {
			this.attr = attr;
			return true;
		}else {
			return false;
		}
	}
	
	public String getAttr() {
		return attr;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return value;
	}
}

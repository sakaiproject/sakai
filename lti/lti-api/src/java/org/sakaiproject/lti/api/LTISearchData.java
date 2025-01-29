/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.lti.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class is used to wrap a secured search clause.
 * A search clause will be formed by a list of FIELD+CONDITION and a list of values.
 * For example the search text will be something like : FIELD_NAME_1 CONDITION_1 ? AND FIELD_NAME_2 CONDITION_2 ? AND ... AND FIELD_NAME_N CONDITION_N ? (where CONDITION_X can be 'like', '=', '<', '>'...)
 * While the values list will be something like : VALUE_1, VALUE_2, ..., VALUE_N
 *
 */
public class LTISearchData {
	private String search = null;
	private List<Object> values = new ArrayList<Object>();
	
	public boolean hasValue() {
		return (search != null);
	}
	
	public void addSearchValue(Object value) {
		values.add(value);
	}
	
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
}

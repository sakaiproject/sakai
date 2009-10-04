/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class GradingScaleDefinition {
	private String uid;
	private String name;
	private List<String> grades;
	private List<Double> defaultBottomPercents;

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getGrades() {
		return grades;
	}
	public void setGrades(List<String> grades) {
		this.grades = grades;
	}
	public List<Double> getDefaultBottomPercents() {
		return defaultBottomPercents;
	}
	public void setDefaultBottomPercents(List<Object> defaultBottomPercents) {
		// Depending on how this was called, the list may
		// be of Double, String, emtpy String, or null objects. Convert the strings.
		List<Double> doubleScores = new ArrayList<Double>();
		for (Iterator iter = defaultBottomPercents.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof String) {
				String str = (String)obj;
				if (str.trim().length() == 0) {
					obj = null;
				} else {
					obj = Double.valueOf((String)obj);
				}
			}
			doubleScores.add((Double)obj);
		}
		this.defaultBottomPercents = doubleScores;
	}

}

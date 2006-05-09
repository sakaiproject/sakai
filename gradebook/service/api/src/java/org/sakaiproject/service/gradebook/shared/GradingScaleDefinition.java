/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
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
package org.sakaiproject.service.gradebook.shared;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class GradingScaleDefinition {
    private static final Log log = LogFactory.getLog(GradingScaleDefinition.class);
	private String uid;
	private String name;
	private List grades;
	private List defaultBottomPercents;

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
	public List getGrades() {
		return grades;
	}
	public void setGrades(List grades) {
		this.grades = grades;
	}
	public List getDefaultBottomPercents() {
		return defaultBottomPercents;
	}
	public void setDefaultBottomPercents(List defaultBottomPercents) {
		// Depending on how this was called, the list may
		// be of Double or String objects. Convert the strings.
		List doubleScores = new ArrayList();
		for (Iterator iter = defaultBottomPercents.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof String) {
				obj = new Double((String)obj);
			}
			doubleScores.add(obj);
		}
		this.defaultBottomPercents = doubleScores;
	}

}

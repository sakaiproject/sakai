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

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GradingScale implements Serializable, Comparable {
	private static final Log log = LogFactory.getLog(GradingScale.class);

	private Long id;
	private int version;

	private String uid;
	private String name;
	private List grades;
	private Map defaultBottomPercents;	// From grade to percentage
	private boolean unavailable;

	public Map getDefaultBottomPercents() {
		return defaultBottomPercents;
	}
	public void setDefaultBottomPercents(Map defaultBottomPercents) {
		this.defaultBottomPercents = defaultBottomPercents;
	}
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
	public boolean isUnavailable() {
		return unavailable;
	}
	public void setUnavailable(boolean unavailable) {
		this.unavailable = unavailable;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

    public int compareTo(Object o) {
        return getName().compareTo(((GradingScale)o).getName());
    }
    public String toString() {
        return new ToStringBuilder(this).
            append(getUid()).toString();
    }
}

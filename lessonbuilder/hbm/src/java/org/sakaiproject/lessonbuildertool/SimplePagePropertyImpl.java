/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
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

public class SimplePagePropertyImpl implements SimplePageProperty {
    private long id;
    private String attribute;
    private String value;

    public SimplePagePropertyImpl(String attribute, String value) {
	this.attribute = attribute;
	this.value = value;
    }

    public SimplePagePropertyImpl() {
    }

    public long getId() {
	return id;
    }

    public void setId(long i) {
	id = i;
    }

    public String getAttribute() {
	return attribute;
    }

    public void setAttribute (String a) {
	attribute = a;
    }

    public String getValue() {
	return value;
    }

    public void setValue(String v) {
	value = v;
    }

}

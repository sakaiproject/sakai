/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api.parser;

import java.io.Serializable;

public class EventParserTip implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private String forWhat;
	private String separator;
	private String index;
	
	public EventParserTip() {
		
	}
	
	public EventParserTip(String forWhat, String separator, String index) {
		super();
		this.forWhat = forWhat;
		this.separator = separator;
		this.index = index;
	}

	public String getFor() {
		return forWhat;
	}

	public void setFor(String forWhat) {
		this.forWhat = forWhat;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("For: "+getFor());
		buff.append(" | Separator: "+getSeparator());
		buff.append(" | Index: "+getIndex());
		
		return buff.toString();
	}

}
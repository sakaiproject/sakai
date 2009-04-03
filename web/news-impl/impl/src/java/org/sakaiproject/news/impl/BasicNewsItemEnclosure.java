/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.news.impl;

import org.sakaiproject.news.api.NewsItemEnclosure;

/**
 * <p>
 * BasicNewsItemEnclosure is default implementation of the Interface for a Sakai News message enclsoures.
 * </p>
 * <p>
 * The news message enclsure has header fields (type, size) and a url of the actual enclosed file. All fields are read only.
 * </p>
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu  alt^i
 */
public class BasicNewsItemEnclosure implements NewsItemEnclosure {

	/** the url of the enclosure */
	private String url;
	
	/** the type of the enclosure */
	private String type;
	
	/** the length in Bytes of the enclosure */
	private long length;
	
	/**
	 * Construct a BasicNewsItemEnclosure
	 * 
	 * @param url
	 * @param type
	 * @param length
	 */
	public BasicNewsItemEnclosure(String url, String type, long length) {
		this.url = url;
		this.type = type;
		this.length = length;
	}
	
	/**
	 * Access the length of the enclosure.
	 * 
	 * @return The length of the enclosure.
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Access the type of the enclosure.
	 * 
	 * @return The type of the enclosure.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Access the url of the enclosure.
	 * 
	 * @return The url of the enclosure.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the type of the Enclosure.
	 * 
	 * @param type
	 *        The type of the Enclosure.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the url of the Enclosure.
	 * 
	 * @param url
	 *        The url of the Enclosure.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Set the length of the Enclosure in Bytes.
	 * 
	 * @param length
	 *        The length of the Enclosure in Bytes.
	 */
	public void setLength(long length) {
		this.length = length;
	}
	
}

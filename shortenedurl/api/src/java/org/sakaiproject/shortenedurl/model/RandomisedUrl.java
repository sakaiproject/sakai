/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.shortenedurl.model;

import java.io.Serializable;

/**
 * POJO for persisting RandomisedUrl object
 * 
 * @author Steve Swinsburg (s.swinsburg@gmail.com)
 *
 */
public class RandomisedUrl implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String key;
	private String url;

	/**
	 * Default constructor
	 */
	public RandomisedUrl() {  
	}
  
	/**
	 * Simple constructor for making object in one go
	 * @param url
	 */
	public RandomisedUrl(String key, String url) {
		this.key = key;
		this.url = url;
	}
  

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}

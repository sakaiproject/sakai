/**
 * Copyright (c) 2005-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.print;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a> alt^I
 *
 * This class is basically just a conveinceince class for abstracting the creation of
 * PDF's from assessments
 * 
 */
public class PDFItemBean {

	private Long itemId = null; 

	private String content = null;

	private String meta = null;

	/**
	 * gets the item id
	 */
	public Long getItemId() {
		return itemId;
	}

	/**
	 * sets the item id
	 */
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	/**
	 * gets the raw generated html version of a question
	 * @return question html
	 */
	public String getContent() {
		return content;
	}

	/**
	 * sets the ray generated html version of a question
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * gets the Meta data section of an item
	 * @return Meta block string
	 */
	public String getMeta() {
		return meta;
	}

	/**
	 * sets the Meta dat section of an item
	 * @param meta
	 */
	public void setMeta(String meta) {
		this.meta = meta;
	}

}
/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

import java.util.List;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a>
 *
 * This class is basically just a conveinceince class for abstracting the creation of
 * PDF's from assessments
 * 
 */
public class PDFPartBean {

	private String sectionId;

	private List questions = null;

	private List resources = null;

	private boolean hasResources = false;

	private String intro = "";


	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public Boolean getHasResources() {
		return Boolean.valueOf(hasResources);
	}

	public void setHasResources(Boolean hasResources) {
		this.hasResources = hasResources.booleanValue();
	}

	/**
	 * gets the html Intro of a part
	 * @return
	 */
	public String getIntro() {
		return intro;
	}

	/**
	 * sets the html intro for a part
	 * @param intro
	 */
	public void setIntro(String intro) {
		this.intro = intro;
	}

	/**
	 * gets the Array of questions (PDFItemBean)
	 * @return
	 */
	public List getQuestions() {
		return questions;
	}

	/**
	 * sets the array of questions (PDFItemBean)
	 * @param questions
	 */
	public void setQuestions(List questions) {
		this.questions = questions;
	}

	/**
	 * gets the list of resources
	 *
	 * @return resource list
	 */
	public List getResources() {
		return resources;
	}

	/**
	 * sets the resource list
	 *
	 * @param resources
	 */
	public void setResources(List resources) {
		this.resources = resources;
	}
}
/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.site.util;

import java.util.List;
import java.util.Vector;

import org.sakaiproject.sitemanage.api.model.*;

/**
 * The SiteSetupQuestionTypeSet object is to store user-defined questions for a particular site type 
 * It could have a header as description for all following questions, a url for further information, and a list of SiteSetupQuestion objects
 * @author zqian
 *
 */
public class SiteSetupQuestionTypeSet
{
	/* the header */
	private String header;
	
	/* the url (optional) */
	private String url;
	
	/* the list of questions*/
	private List<SiteSetupQuestion> questions;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<SiteSetupQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<SiteSetupQuestion> questions) {
		this.questions = questions;
	}
	
	public void addQuestion(SiteSetupQuestion question) {
		List<SiteSetupQuestion> qList = getQuestions();
		if (qList == null)
		{
			qList = new Vector<SiteSetupQuestion>();
		}
		qList.add(question);
		setQuestions(qList);
	}
	
}
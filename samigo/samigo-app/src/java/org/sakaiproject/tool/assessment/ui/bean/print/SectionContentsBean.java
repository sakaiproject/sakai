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

import java.util.ArrayList;
import java.util.List;

public class SectionContentsBean extends
		org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List itemContents = null;
	
	public SectionContentsBean(org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean section) {
		this.setAttachmentList(section.getAttachmentList());
		this.setDescription(section.getDescription());
		List items = getItemContents();
		if (items == null) items = section.getItemContents();
		this.setItemContents(items);
		this.setItemContentsSize(section.getItemContentsSize());
		this.setMaxPoints(section.getMaxPoints());
		this.setNoQuestions(section.getNoQuestions());
		this.setNumber(section.getNumber());
		this.setNumbering(section.getNumbering());
		this.setNumberToBeDrawn(section.getNumberToBeDrawn());
		this.setNumParts(section.getNumParts());
		this.setPoints(section.getPoints());
		this.setPoolIdToBeDrawn(section.getPoolIdToBeDrawn());
		this.setPoolNameToBeDrawn(section.getPoolNameToBeDrawn());
		this.setQuestionOrdering(section.getQuestionOrdering());
		this.setSectionAuthorType(section.getSectionAuthorType());
		this.setQuestions(section.getQuestions());
		this.setSectionId(section.getSectionId());
		this.setText(section.getText());
		this.setTitle(section.getTitle());
		this.setUnansweredQuestions(section.getUnansweredQuestions());
	}
	
	/**
	 * Contents of part.
	 * @return item contents of part.
	 */
	public List getItemContents()
	{
		if (itemContents == null) {
			List items = new ArrayList();
			
		    if (getPoolIdToBeDrawn() != null) {
		    	items = super.getItemContentsForRandomDraw();
		    }
		    else {
		    	items = super.getItemContents();
		    }
		    itemContents = items;
		}
		
		return itemContents;
	}

	
}
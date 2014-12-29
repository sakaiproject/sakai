package org.sakaiproject.tool.assessment.ui.bean.print;

import java.util.ArrayList;

public class SectionContentsBean extends
		org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList itemContents = null;
	
	public SectionContentsBean(org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean section) {
		this.setAttachmentList(section.getAttachmentList());
		this.setDescription(section.getDescription());
		ArrayList items = getItemContents();
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
	public ArrayList getItemContents()
	{
		if (itemContents == null) {
			ArrayList items = new ArrayList();
			
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
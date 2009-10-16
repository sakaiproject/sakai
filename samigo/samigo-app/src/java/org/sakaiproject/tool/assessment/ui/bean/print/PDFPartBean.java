package org.sakaiproject.tool.assessment.ui.bean.print;

import java.util.ArrayList;

import org.sakaiproject.util.FormattedText;

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

	private ArrayList questions = null;

	private ArrayList resources = null;

	private boolean hasResources = false;

	private String intro = "";


	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public Boolean getHasResources() {
		return new Boolean(hasResources);
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
		this.intro = FormattedText.convertFormattedTextToPlaintext(intro);
	}

	/**
	 * gets the Array of questions (PDFItemBean)
	 * @return
	 */
	public ArrayList getQuestions() {
		return questions;
	}

	/**
	 * sets the array of questions (PDFItemBean)
	 * @param questions
	 */
	public void setQuestions(ArrayList questions) {
		this.questions = questions;
	}

	/**
	 * gets the list of resources
	 *
	 * @return resource list
	 */
	public ArrayList getResources() {
		return resources;
	}

	/**
	 * sets the resource list
	 *
	 * @param resources
	 */
	public void setResources(ArrayList resources) {
		this.resources = resources;
	}
}
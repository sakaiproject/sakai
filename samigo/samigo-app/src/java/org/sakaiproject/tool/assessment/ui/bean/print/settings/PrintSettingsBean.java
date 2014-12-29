package org.sakaiproject.tool.assessment.ui.bean.print.settings;

import java.io.Serializable;

/**
 * Holds the users settings for assessment print out
 *
 * @author Joshua Ryan  joshua.ryan@asu.edu  alt^I
 */
public class PrintSettingsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean showKeys = Boolean.FALSE;
	private String fontSize = "3";
	private Boolean showPartIntros = Boolean.TRUE;
	private Boolean showKeysFeedback = Boolean.FALSE;

	public PrintSettingsBean() {
		//nothing
	}

	/**
	 * gets the users font size;
	 * 
	 * @return the font size
	 */
	public String getFontSize() {
		return fontSize;
	}

	/**
	 * the users font size
	 * 
	 * @param fontSize
	 */
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return if the part intros should be shown or not
	 */
	public Boolean getShowPartIntros() {
		return showPartIntros;
	}

	/**
	 * @param if the part intros should be shown or not
	 */
	public void setShowPartIntros(Boolean partIntros) {
		this.showPartIntros = partIntros;
	}

	/**
	 * @return true if keys should be shown
	 */
	public Boolean getShowKeys() {
		return showKeys;
	}
	/**
	 * @param set if the keys should be show
	 */
	public void setShowKeys(Boolean hasKeys) {
		this.showKeys = hasKeys;
	}

	/**
	 * @return true if keys & feedback should be shown
	 */
	public Boolean getShowKeysFeedback() {
		return showKeysFeedback;
	}

	/**
	 * @param set if the keys & feedback should be show
	 */
	public void setShowKeysFeedback(Boolean showKeysFeedback) {
		this.showKeysFeedback = showKeysFeedback;
	}

}

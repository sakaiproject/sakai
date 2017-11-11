/**
 * Copyright (c) 2005-2015 The Apereo Foundation
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
	private Boolean showSequence = Boolean.FALSE;

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

    /**
	 * @return true if sequence should be shown
	 */
	public Boolean getShowSequence() {
		return showSequence;
	}

	/**
	 * @param set if the sequence should be shown
	 */
	public void setShowSequence(Boolean showSequence) {
		this.showSequence = showSequence;
	}

}

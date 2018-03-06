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


package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

public class ImageMapItemBean implements Serializable {

	private static final long serialVersionUID = 7526471155622776147L;
	public static final String CONTROLLING_SEQUENCE_DEFAULT = "*new*";

	private Long sequence;
	// private String corrfeedback;
	// private String incorrfeedback;
	private Boolean isCorrect;
	private String choice; //
	private String match;
	private String corrImageMapFeedback;
	private String incorrImageMapFeedback;
	private String sequenceStr;
	private String controllingSequence;

	public ImageMapItemBean() {
		// sequence = -1 for new items
		sequence = Long.valueOf(-1);
		sequenceStr = "-1";
		controllingSequence = CONTROLLING_SEQUENCE_DEFAULT;
	}

	public ImageMapItemBean(String serializedString) {
		// sequence = -1 for new items
		sequence = Long.valueOf(-1);
		sequenceStr = "-1";
		controllingSequence = CONTROLLING_SEQUENCE_DEFAULT;

		if (serializedString != null) {
			String[] tokens = serializedString.split("#:#");
			if (tokens.length >= 1) {
				choice = tokens[0];
				if (tokens.length == 2)
					match = tokens[1];
			}
		}
	}

	public String serialize() {
		StringBuffer ret = new StringBuffer();

		ret.append(getChoice());
		ret.append("#:#");
		ret.append(getMatch());

		return ret.toString();
	}

	/**
	 * controllingSequence determines if the choice for this matching bean is
	 * defined within the bean or within another bean in a list of beans. If the
	 * controllingSequence is "Self", the choice comes from within this bean. If
	 * the controllingSequence is "Distractor", there is no choice for this
	 * bean. Otherwise, use the value of controllingSequence to locate the bean
	 * that has the choice test.
	 * 
	 * @param controllingSequence
	 */
	public void setControllingSequence(String controllingSequence) {
		this.controllingSequence = controllingSequence;
	}

	public String getControllingSequence() {
		return this.controllingSequence;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
		this.sequenceStr = sequence.toString();
	}

	// used by jsf to check if the current pair is for editing
	public String getSequenceStr() {
		return sequenceStr;
	}

	public void setSequenceStr(String param) {
		this.sequenceStr = param;
	}

	public Boolean getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public String getChoice() {
		return choice;
	}

	public void setChoice(String param) {
		this.choice = param;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String param) {
		this.match = param;
	}

	public String getCorrImageMapFeedback() {
		return corrImageMapFeedback;
	}

	public void setCorrImageMapFeedback(String param) {
		this.corrImageMapFeedback = param;
	}

	public String getIncorrImageMapFeedback() {
		return incorrImageMapFeedback;
	}

	public void setIncorrImageMapFeedback(String param) {
		this.incorrImageMapFeedback = param;
	}
}

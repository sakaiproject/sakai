/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.ArrayList;
import java.util.Iterator;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

public class ImageMapQuestionBean {

	private ItemContentsBean parent;
	private ItemTextIfc itemText;
	private ItemGradingData data;
	private String response;
	private ArrayList choices;
	private String text;
	private String feedback;
	private AnswerIfc answer;
	private Boolean isCorrect;

	public String serialize() {
		StringBuffer ret = new StringBuffer();

		ret.append(getItemText().getId());
		ret.append("#:#");
		ret.append(getItemText().getText());
		if (getResponse() != null && !"".equals(getResponse())) {
			ret.append("#:#");
			ret.append(getResponse());
		}

		return ret.toString();
	}

	public ItemContentsBean getItemContentsBean() {
		return parent;
	}

	public void setItemContentsBean(ItemContentsBean bean) {
		parent = bean;
	}

	public ItemTextIfc getItemText() {
		return itemText;
	}

	public void setItemText(ItemTextIfc newtext) {
		itemText = newtext;
	}

	public ItemGradingData getItemGradingData() {
		return data;
	}

	public void setItemGradingData(ItemGradingData newdata) {
		data = newdata;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String newresp) {
		response = newresp;

		if (data == null) {
			data = new ItemGradingData();
			data.setPublishedItemId(parent.getItemData().getItemId());
			data.setPublishedItemTextId(itemText.getId());
			ArrayList items = (ArrayList) parent.getItemGradingDataArray();
			items.add(data);
			parent.setItemGradingDataArray(items);
		}

		data.setAnswerText(newresp);
	}

	public ArrayList getChoices() {
		return choices;
	}

	public void setChoices(ArrayList newch) {
		choices = newch;
	}

	public String getText() {
		return text;
	}

	public void setText(String newtext) {
		text = newtext;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String newfb) {
		feedback = newfb;
	}

	public void setAnswer(AnswerIfc answer) {
		this.answer = answer;
	}

	public AnswerIfc getAnswer() {
		return answer;
	}

	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public Boolean getIsCorrect() {
		return isCorrect;
	}
}
/**********************************************************************************
 * $URL$
 * $Id$
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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * This is the JSF backing bean for delivery, used for TF and MC questions
 * 
 * $Id$
 */
@Slf4j
public class SelectionBean {

	private ItemContentsBean parent;

	private ItemGradingData data;

	private boolean response;

	private AnswerIfc answer;

	private String feedback;


	public ItemContentsBean getItemContentsBean() {
		return parent;
	}

	public void setItemContentsBean(ItemContentsBean bean) {
		parent = bean;
	}

	public ItemGradingData getItemGradingData() {
		return data;
	}

	public void setItemGradingData(ItemGradingData newdata) {
		data = newdata;
	}

	public boolean getResponse() {
		return response;
	}

	public void setResponse(boolean newresp) {
		// this is called with mcsc and mcmc

		response = newresp;
		if (newresp) {
			ItemTextIfc itemText = (ItemTextIfc) parent.getItemData()
					.getItemTextSet().toArray()[0];
			if (data == null) {
				data = new ItemGradingData();
				data.setPublishedItemId(parent.getItemData().getItemId());
				data.setPublishedItemTextId(itemText.getId());
				List<ItemGradingData> items = parent.getItemGradingDataArray();
				items.add(data);
				parent.setItemGradingDataArray(items);
			}
			data.setPublishedAnswerId(answer.getId());
		} else if (data != null)
			data.setPublishedAnswerId(null);
	}

	public void setResponseFromCleanRadioButton() {
		response = false;
		data = null;
	}
		
	public AnswerIfc getAnswer() {
		return answer;
	}

	public void setAnswer(AnswerIfc newAnswer) {
		answer = newAnswer;
	}

	public String getFeedback() {
		if (feedback == null)
			return "";
		return feedback;
	}

	public void setFeedback(String newfb) {
		feedback = newfb;
	}

	public String getAnswerId() {
		return answer.getId().toString();
	}
}

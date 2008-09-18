/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
//import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>
 * This bean represents an item
 * </p>
 */

public class ItemContentsBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6270034338280029897L;

	private static Log log = LogFactory.getLog(ItemContentsBean.class);

	// private static ContextUtil cu;

	private boolean review;

	private boolean unanswered;

	private ItemDataIfc itemData;

	private String gradingComment;

	private String feedback;

	private String responseId = "2";

	private String responseText = "";

	private String[] responseIds = null;

	private float points;
	
	private float discount;

	private float maxPoints;

	private int number;

	private ArrayList itemGradingDataArray;

	private ArrayList answers;

	private String instruction;

	private String rationale;

	private ArrayList matchingArray;

	private ArrayList fibArray;

	private ArrayList finArray;

	private ArrayList selectionArray;

	private String key;

	private String sequence;

	private ArrayList shuffledAnswers;

	private ArrayList mediaArray;

	// for audio
	private Integer duration;

	private Integer triesAllowed;

	private Integer attemptsRemaining;

	// for display/hide score
	private boolean showStudentScore; // this is to show student assessment
										// score

	private boolean showStudentQuestionScore;

	private String pointsDisplayString;

	public ItemContentsBean() {
	}

	// added by daisyf on 11/22/04
	public ItemContentsBean(ItemDataIfc itemData) {
		this.itemData = itemData;
		setInstruction(this.itemData.getInstruction());
		Integer sequence = this.itemData.getSequence();
		if (sequence != null) {
			setNumber(sequence.intValue());
		} else {
			setNumber(1);
		}
	}

	/**
	 * In the case of an ordinary question, this will obtain the a set of text
	 * with one element and return it; in FIB return multiple elements separated
	 * by underscores.
	 * 
	 * @return text of question
	 */
	public String getText() {
		String text = "";

		if (itemData != null) {
			text = itemData.getText();
		}

		return text;
	}

	/**
	 * This strips text of tags for the table of contents.
	 */
	public String getStrippedText() {
		return strip(getText());

	}

	public boolean getModelAnswerIsNotEmpty() {
		String k = getKey();
		if (k != null)
			return isNotEmpty(strip(ContextUtil.stringWYSIWYG(k)));
		else
			return false;
	}
	
	public boolean getFeedbackIsNotEmpty() {
		return isNotEmpty(strip(ContextUtil.stringWYSIWYG(getFeedback())));
	}

	public boolean getGradingCommentIsNotEmpty() {
		return isNotEmpty(strip(getGradingComment()));
	}

	public String getStrippedKey() {
		return strip(getKey());
	}

	/**
	 * String representation of the rounded points.
	 * 
	 * @return String representation of the points.
	 */
	public float getPoints() {
		return SectionContentsBean.roundTo2Decimals(points);
	}

	/**
	 * String representation of the exact points (unrounded points)
	 * 
	 * @return String representation of the points.
	 */
	public float getExactPoints() {
		return points;
	}

	/**
	 * String representation of the points.
	 * 
	 * @param points
	 *            String representation of the points.
	 */
	public void setPoints(float points) {
		this.points = points;
	}

	/**
	 * Does this need review?
	 * 
	 * @return true if it is marked for review
	 */
	
	/**
	 * String representation of the rounded points.
	 * 
	 * @return String representation of the points.
	 */
	public String getPointsForEdit() {
		return Float.toString(getPoints());
	}

	/**
	 * String representation of the points.
	 * 
	 * @param points
	 *            String representation of the points.
	 */
	public void setPointsForEdit(String pointsForEdit) {
		if (pointsForEdit == null || pointsForEdit.equals("")) {
			pointsForEdit = "0";
		}
		setPoints(Float.parseFloat(pointsForEdit));
	}
	
    /**
     * String representation of the rounded discount.
     *
     * @return String representation of the discount.
     */
    public float getDiscount() {
    	 return SectionContentsBean.roundTo2Decimals(discount);
    }

    /**
     * String representation of the exact points (unrounded discount)
     *
     * @return String representation of the discount.
     */
    public float getExactDiscount() {
    	return discount;
    }

    /**
     * String representation of the Discount.
     *
     * @param discount
     *            String representation of the Discount.
     */
    public void setDiscount(float discount) {
    	this.discount = discount;
    }

    /**
     * String representation of the rounded Discount.
     *
     * @return String representation of the Discount.
     */
    public String getDiscountForEdit() {
    	return Float.toString(getDiscount());
    }
 
    /**
     * String representation of the discount.
     *
     * @param discount
     *            String representation of the discount.
     */
    public void setDiscountForEdit(String discountForEdit) {
    	if (discountForEdit == null || discountForEdit.equals("")) {
    		discountForEdit = "0";
    	}
    	setDiscount(Float.parseFloat(discountForEdit));
    }
	
	public boolean getReview() {
		if (getItemGradingDataArray().isEmpty()) {
			return false;
		}
		ItemGradingData data = (ItemGradingData) getItemGradingDataArray()
				.toArray()[0];
		if (data.getReview() == null) {
			return false;
		}
		return data.getReview().booleanValue();
	}

	/**
	 * Does this need review?
	 * 
	 * @param review
	 *            if true mark for review
	 */
	public void setReview(boolean preview) {
		log.debug("setReview():  preview = " + preview);
		if (getItemGradingDataArray().isEmpty()) {
			log.debug("setReview():  isEmpty = " + preview);
			ItemGradingData data = new ItemGradingData();
			data.setPublishedItemId(itemData.getItemId());
			if (itemData.getItemTextSet().size() > 0) {
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
						.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
			}
			ArrayList items = new ArrayList();
			items.add(data);
			setItemGradingDataArray(items);
		}
		Iterator iter = getItemGradingDataArray().iterator();
		log.debug("setReview():  getItemGradingDataArray size = "
				+ getItemGradingDataArray().size());
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			log.debug("setReview():  setreview at the end = " + preview);
			data.setReview(Boolean.valueOf(preview));
		}
	}

	/**
	 * unanswered?
	 * 
	 * @return
	 */
	public boolean isUnanswered() {
		if (getItemGradingDataArray().isEmpty()) {
			return true;
		}
		Iterator iter = getItemGradingDataArray().iterator();
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			if (getItemData().getTypeId().toString().equals("8")
					|| getItemData().getTypeId().toString().equals("11")) // fix
																			// for
																			// bug
																			// sam-330
			{
				if (data.getAnswerText() != null
						&& !data.getAnswerText().equals("")) {
					return false;
				}
			} else {
				if (data.getPublishedAnswerId() != null
						|| data.getAnswerText() != null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * unanswered?
	 * 
	 * @param unanswered
	 */
	public void setUnanswered(boolean unanswered) {
		this.unanswered = unanswered;
	}

	/**
	 * String representation of the max points available for this question.
	 * 
	 * @return String representation of the max points.
	 */
	public float getMaxPoints() {
		return maxPoints;
	}

	/**
	 * String representation of the max points available for this question.
	 * 
	 * @return String representation of the max points.
	 */
	public float getRoundedMaxPoints() {
		return SectionContentsBean.roundTo2Decimals(maxPoints);
	}

	/**
	 * String representation of the max points available for this question.
	 * 
	 * @param maxPoints
	 *            String representation of the max points available
	 */
	public void setMaxPoints(float maxPoints) {
		this.maxPoints = maxPoints;
	}

	/**
	 * question number
	 * 
	 * @return
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * question number
	 * 
	 * @param number
	 */
	public void setNumber(int number) {
		this.number = number;
		this.itemData.setSequence(new Integer(number));
	}

	/**
	 * the item data itself
	 * 
	 * @return
	 */
	public ItemDataIfc getItemData() {
		return itemData;
	}

	/**
	 * the item data itself
	 * 
	 * @param itemData
	 */
	public void setItemData(ItemDataIfc itemData) {
		this.itemData = itemData;
	}

	/**
	 * grading comment
	 * 
	 * @return grading comment
	 */
	public String getGradingComment() {
		if (gradingComment == null) {
			return "";
		}
		return gradingComment;
	}

	/**
	 * grading comment
	 * 
	 * @param gradingComment
	 *            grading comment
	 */
	public void setGradingComment(String gradingComment) {
		this.gradingComment = gradingComment;
	}

	/**
	 * item level feedback
	 * 
	 * @return the item level feedback
	 */
	public String getFeedback() {
		return feedback;
	}

	/**
	 * item level feedback
	 * 
	 * @param feedback
	 *            the item level feedback
	 */
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	/**
	 * If this is a true-false question return true if it is true, else false.
	 * If it is not a true-false question return false.
	 * 
	 * @return true if this is a true true-false question
	 */
	public boolean getIsTrue() {
		if (itemData != null) {
			return itemData.getIsTrue().booleanValue();
		}
		return false;
	}

	public ArrayList getItemGradingDataArray() {
		if (itemGradingDataArray == null) {
			// log.debug("getReview : getitemgradingdataarray is null, size =0
			// ");
			return new ArrayList();
		}
		// log.debug("getReview: getitemgradingdataarray size " +
		// itemGradingDataArray.size());
		return itemGradingDataArray;
	}

	public void setItemGradingDataArray(ArrayList newArray) {
		itemGradingDataArray = newArray;
	}

	/* These are helper methods to get data into the database */

  public String getResponseId()
  {
    try
    {
      if (selectionArray != null)
      {
        Iterator iter = selectionArray.iterator();
        while (iter.hasNext())
        {
          SelectionBean bean = (SelectionBean) iter.next();
          if (bean.getResponse())
          {
            return bean.getAnswer().getId().toString();
          }
        }
        return "";
      }
    }
    catch (Exception e)
    {
      log.debug("get ResponseId(), okay with t/f. " + e.getMessage());
      // True/false
    }

    try
    {
      String response = "";
      // String response = responseId;  //For testing
      Iterator iter = getItemGradingDataArray().iterator();
      if (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        if (data.getPublishedAnswerId() != null)
        {
          response = data.getPublishedAnswerId().toString();
        }
      }
      return response;
    }
    catch (Exception e)
    {
      log.debug("get ResponseId() , okay with t/f. " + e.getMessage());
      return responseId;
    }
  }

	public void setResponseId(String presponseId) {
		try {
			responseId = presponseId;

			if (selectionArray != null && presponseId != null
					&& !presponseId.trim().equals("")) {
				Iterator iter = selectionArray.iterator();
				while (iter.hasNext()) {
					SelectionBean bean = (SelectionBean) iter.next(); // this
																		// line
																		// will
																		// throw
																		// ClassCastException
																		// for
																		// True/False.

					if (bean.getAnswer().getId().toString().equals(presponseId)) {
						bean.setResponse(true);
					} else {
						bean.setResponse(false);
					}
				}
			}
			return;
		} catch (RuntimeException e) {
			// True/false
			log.debug("ClassCastException is okay, the question might be true/false. ");
		}


		try {
			Iterator iter = getItemGradingDataArray().iterator();
			if (!iter.hasNext()
					&& (presponseId == null || presponseId.equals(""))) {
				return;
			}
			ItemGradingData data = null;
			if (iter.hasNext()) {
				data = (ItemGradingData) iter.next();
			} else {
				data = new ItemGradingData();
				data.setPublishedItemId(itemData.getItemId());
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
						.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
				ArrayList items = new ArrayList();
				items.add(data);
				setItemGradingDataArray(items);
			}
			iter = ((ItemTextIfc) itemData.getItemTextSet().toArray()[0])
					.getAnswerSet().iterator();
			while (iter.hasNext()) {
				AnswerIfc answer = (AnswerIfc) iter.next();
				if (answer.getId().toString().equals(responseId)) {
					data.setPublishedAnswerId(answer.getId());
					break;
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

  public String[] getResponseIds()
  {
    try
    {
      /*
         ItemTextIfc text = (ItemTextIfc) itemData.getItemTextSet().toArray()[0];
           String[] response = new String[text.getAnswerArraySorted().size()];
           for (int i=0; i<response.length; i++)
           {
        Iterator iter = getItemGradingDataArray().iterator();
        while (iter.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter.next();
          if (data.getPublishedAnswerId() != null && data.getPublishedAnswerId().toString().equals(text.getAnswerArraySorted().toArray()[i]))
          {
            response[i] = data.getPublishedAnswerId().toString();
          }
        }
           } */

      String[] response = new String[getItemGradingDataArray().size()];
      Iterator iter = getItemGradingDataArray().iterator();
      int i = 0;
      while (iter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter.next();
        if (data.getPublishedAnswerId() != null)
        {
          response[i++] = data.getPublishedAnswerId().toString();
        }
        else
        {
          response[i++] = null;
        }
      }
      return response;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return new String[0];
    }
  }

	public void setResponseIds(String[] presponseIds) {
		try {
			ArrayList newItems = new ArrayList();
			responseIds = presponseIds;
			if (getItemGradingDataArray().isEmpty()
					&& (presponseIds == null || presponseIds.length == 0)) {
				return;
			}
			for (int i = 0; i < presponseIds.length; i++) {
				ItemGradingData data = null;
				Iterator iter = getItemGradingDataArray().iterator();
				while (iter.hasNext()) {
					ItemGradingData temp = (ItemGradingData) iter.next();
					if (temp.getPublishedAnswerId() != null
							&& temp.getPublishedAnswerId().toString().equals(
									presponseIds[i])) {
						data = temp;
					}
				}
				if (data == null) {
					data = new ItemGradingData();
					data.setPublishedItemId(itemData.getItemId());
					ItemTextIfc itemText = (ItemTextIfc) itemData
							.getItemTextSet().toArray()[0];
					data.setPublishedItemTextId(itemText.getId());
					Iterator iter2 = itemText.getAnswerSet().iterator();
					while (iter2.hasNext()) {
						AnswerIfc answer = (AnswerIfc) iter2.next();
						if (answer.getId().toString().equals(presponseIds[i])) {
							data.setPublishedAnswerId(answer.getId());
						}
					}
				}
				newItems.add(data);
			}
			setItemGradingDataArray(newItems);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getResponseText() {
		log.debug("itemcontentbean.getResponseText");
		try {
			String response = responseText;
			Iterator iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				ItemGradingData data = (ItemGradingData) iter.next();
				response = data.getAnswerText();
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return responseText;
		}
	}

	public void setResponseText(String presponseId) {
		log.debug("itemcontentbean.setResponseText");
		try {
			responseText = presponseId;
			Iterator iter = getItemGradingDataArray().iterator();
			if (!iter.hasNext()
					&& (presponseId == null || presponseId.equals(""))) {
				return;
			}
			ItemGradingData data = null;
			if (iter.hasNext()) {
				data = (ItemGradingData) iter.next();
			} else {
				data = new ItemGradingData();
				data.setPublishedItemId(itemData.getItemId());
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
						.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
				ArrayList items = new ArrayList();
				items.add(data);
				setItemGradingDataArray(items);
			}
			data.setAnswerText(presponseId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList getMatchingArray() {
		return matchingArray;
	}

	public void setMatchingArray(ArrayList newArray) {
		matchingArray = newArray;
	}

	public ArrayList getFibArray() {
		return fibArray;
	}

	public void setFibArray(ArrayList newArray) {
		fibArray = newArray;
	}

	public ArrayList getFinArray() {
		return finArray;
	}

	public void setFinArray(ArrayList newArray) {
		finArray = newArray;
	}

	public ArrayList getSelectionArray() {
		return selectionArray;
	}

	public void setSelectionArray(ArrayList newArray) {
		selectionArray = newArray;
	}

  public ArrayList getAnswers()
  {
    return answers;
  }

	public void setAnswers(ArrayList list) {
		answers = list;
	}

	// added by Daisy
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getInstruction() {
		return this.instruction;
	}

	public void setRationale(String newRationale) {
		int count = getItemGradingDataArray().size();
		if (count <= 0) {
		    // student didn't answer the question, so no itemgrading to save rationale, skip.
			return;
		}
		ItemGradingData data = (ItemGradingData) getItemGradingDataArray()
				.toArray()[count - 1];

		if ( getItemData().getTypeId().toString().equals(TypeIfc.TRUE_FALSE.toString())){
			// for True false  
			data.setRationale(newRationale);
		} 
		else if ( getItemData().getTypeId().toString().equals(TypeIfc.MULTIPLE_CORRECT.toString()) || getItemData().getTypeId().toString().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.toString())) {
			//   MCMC, need to update rationale in all  itemgrading records
			  Iterator iter = getItemGradingDataArray().iterator(); 
			  while (iter.hasNext()) { 
			  	ItemGradingData mcmcdata = (ItemGradingData) iter.next(); 

			mcmcdata.setRationale(newRationale);
		}
		} 
		else {
			// for MCSC
			if (data.getItemGradingId() == null) {
				// this is a new answer , now we just need to set the rationale

				data.setRationale(newRationale);

			} else {
				// the answer is the same, the student only changed the
				// rationale, not the MC selection
				// we need to create a new ItemGradingData. because
				// SubmitForGradingListener doesn't recognize that this is a
				// modified answer
				// unless the itemgradingid = null

				ItemGradingData newdata = new ItemGradingData();
				newdata.setPublishedItemId(data.getPublishedItemId());
				newdata.setPublishedItemTextId(data.getPublishedItemTextId());
				newdata.setRationale(newRationale);
				newdata.setPublishedAnswerId(data.getPublishedAnswerId());
				ArrayList items = getItemGradingDataArray();
				items.add(newdata);
				setItemGradingDataArray(items);
			}
		}


	}

	public String getRationale() {
		// Iterator iter = getItemGradingDataArray().iterator();
		// if (iter.hasNext())
		// {
		int count = getItemGradingDataArray().size();
		if (count > 0) {
			ItemGradingData data = (ItemGradingData) getItemGradingDataArray()
					.toArray()[count - 1];
			// ItemGradingData data = (ItemGradingData) iter.next();
			rationale = FormattedText.unEscapeHtml(data.getRationale());
		}
		return Validator.check(rationale, "");
	}

	public String getKey() {
		return key;
	}

	public void setKey(String newKey) {
		key = newKey;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String newSequence) {
		sequence = newSequence;
	}

	public ArrayList getShuffledAnswers() {
		return shuffledAnswers;
	}

	public void setShuffledAnswers(ArrayList newAnswers) {
		shuffledAnswers = newAnswers;
	}

	public Integer getTriesAllowed() {
		return triesAllowed;
	}

	public void setTriesAllowed(Integer param) {
		triesAllowed = param;
	}

  public Integer getAttemptsRemaining()
  {
    return attemptsRemaining;
  }

	public void setAttemptsRemaining(Integer param) {
		attemptsRemaining = param;
	}

  public Integer getDuration()
  {
    return duration;
  }

	public void setDuration(Integer param) {
		duration = param;
	}

	public ArrayList getMediaArray() {
		ArrayList mediaArray = new ArrayList();
		ItemGradingData itemGradingData = null;
		try {
			Iterator iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				itemGradingData = (ItemGradingData) iter.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (itemGradingData != null
				&& itemGradingData.getItemGradingId() != null) {
			GradingService service = new GradingService();
			mediaArray = service.getMediaArray(itemGradingData
					.getItemGradingId().toString());
                        // if question is audio, check time limit
                        ItemDataIfc item = getItemData();
                        if ((TypeIfc.AUDIO_RECORDING).equals(item.getTypeId()))
                        setDurationIsOver(item, mediaArray);
		}
		return mediaArray;
	}

  private void setDurationIsOver(ItemDataIfc item, ArrayList mediaList){
    // we set maxDurationAllowed = 60s for audio question published without
    // a duration. This is created from imported assessment when duration for
    // audio question was not set correctly. Note that this is just a work 
    // around. Ultimately, the importing procedure need to be fixed or the
    // publishing procedure need to be tightened. - daisyf
    int maxDurationAllowed = 60;
    try{
      maxDurationAllowed = item.getDuration().intValue();
    }
    catch(Exception e){
      log.info("**duration recorded is not an integer value="+e.getMessage());
      maxDurationAllowed = 60;
    }
    for (int i=0; i<mediaList.size(); i++){
      MediaIfc m = (MediaIfc) mediaList.get(i);
      float duration = (new Float(m.getDuration())).floatValue();
      if (duration > maxDurationAllowed)
        m.setDurationIsOver(true);
      else
        m.setDurationIsOver(false);
    }
  }
	/**
	 * Show the student score currently earned?
	 * 
	 * @return the score
	 */
	public boolean isShowStudentScore() {
		return showStudentScore;
	}

	/**
	 * Set the student score currently earned.
	 * 
	 * @param showStudentScore
	 *            true/false Show the student score currently earned?
	 */
	public void setShowStudentScore(boolean showStudentScore) {
		this.showStudentScore = showStudentScore;
	}

	/**
	 * Show the student question score currently earned?
	 * 
	 * @return the score
	 */
	public boolean isShowStudentQuestionScore() {
		return showStudentQuestionScore;
	}

	/**
	 * Set the student question score currently earned.
	 * 
	 * @param param
	 *            true/false Show the student score currently earned?
	 */
	public void setShowStudentQuestionScore(boolean param) {
		this.showStudentQuestionScore = param;
	}

	/**
	 * If we display the score, return it, followed by a slash.
	 * 
	 * @return either, a) the score followed by a slash, or, b) "" (empty
	 *         string)
	 */
	public String getPointsDisplayString() {
		String pointsDisplayString = "";
		if (showStudentQuestionScore) {
			pointsDisplayString = SectionContentsBean.roundTo2Decimals(points)
					+ "/";
		}
		return pointsDisplayString;
	}

	public String strip(String text) {
		if (text != null)
			text = text.replaceAll("<.*?>", " ");
		return text;

	}

	public boolean isNotEmpty(String wyzText) {

		if (wyzText != null && !wyzText.equals("null")) {
			int index = 0;
			String t = wyzText.trim();
			while (index < t.length()) {
				char c = t.charAt(index);
				if (Character.isLetterOrDigit(c)) {
					return true;
				}
				index++;
			}
		}
		return false;
	}

	public String getKeyInUnicode() {
		return ContextUtil.getStringInUnicode(getKey());
	}

  public boolean getHasAttachment(){
    boolean hasAttachment = false;
    if (itemData!=null){
      List l = itemData.getItemAttachmentList();
      if (l!=null && l.size() >0)
        hasAttachment = true;
    }
    return hasAttachment;
  }

  public Float getUpdatedScore () {
	  return itemData.getScore();
  }
	 
  public void setUpdatedScore(Float score) {
	  if (!score.equals(itemData.getScore())) {
		  AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		  ItemService itemService = null;
		  if (author.getIsEditPendingAssessmentFlow()) {
			  itemService = new ItemService();
		  }
		  else {
			  itemService = new PublishedItemService();
		  }
		  
          ItemFacade item = itemService.getItem(itemData.getItemId(), AgentFacade.getAgentString());
          item.setScore(score);

          ItemDataIfc data = item.getData();
          Set itemTextSet = data.getItemTextSet();
          Iterator iter = itemTextSet.iterator();
          while (iter.hasNext()) {
              ItemTextIfc itemText = (ItemTextIfc) iter.next();
              Set answerSet = itemText.getAnswerSet();
              Iterator iter2 = answerSet.iterator();
              while (iter2.hasNext()) {
                  AnswerIfc answer = (AnswerIfc)iter2.next();
                  log.debug("old value " + answer.getScore() +
                                     "new value " + score);
                  answer.setScore(score);
              }
              EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.revise", "itemId=" + itemData.getItemId(), true));
          }
          itemService.saveItem(item);
          itemData.setScore(score);
	  }
  }

  public boolean getHasNoMedia() {
	return getMediaArray().size() < 1;
}

}

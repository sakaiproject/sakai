/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.assessment.util.AttachmentUtil;
import org.sakaiproject.tool.assessment.util.ItemCancellationUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

/* For delivery: ItemContents backing bean. */
@Slf4j
@ManagedBean(name="itemContents")
@SessionScoped
public class ItemContentsBean implements Serializable {

	private static final long serialVersionUID = 6270034338280029897L;

	private final ResourceLoader rb;

	@Getter @Setter private ItemDataIfc itemData;
	@Getter @Setter private String calculatedQuestionAnswer;
	@Getter @Setter private String feedback;
	@Getter @Setter private String feedbackValue;
	@Getter @Setter private String imageAltText = "";
	@Getter @Setter private String incorrectFeedbackValue;
	@Getter @Setter private String instruction;
	@Getter @Setter private double maxPoints;
	@Getter @Setter private double points;
	@Getter @Setter private int relativeWidth;
	@Getter private Set<ItemTagIfc> tagsList;
	@Getter private int number;
	@Setter @Getter private Integer attemptsRemaining;
	@Setter @Getter private Integer duration;
	@Setter @Getter private Integer triesAllowed;
	// Holds SelectionBean (multiple choice/correct), SelectItem (true/false), or plain String
	// (matching/EMI/image map) per answer, depending on the item's question type. JSF pages
	// bind directly to this (e.g. <f:selectItems value="#{question.answers}"/> for true/false,
	// which requires literal SelectItem instances), so it cannot be a wrapper type.
	@Setter @Getter private List<Object> answers;
	@Setter @Getter private List<FibBean> fibArray;
	@Setter @Getter private List<MatchingEntry> matchingArray;
	@Setter @Getter private List<AnswerIfc> shuffledAnswers;
	@Setter @Getter private List<Integer> columnIndexList;
	@Setter @Getter private List<ItemGradingAttachment> itemGradingAttachmentList;
	@Setter @Getter private Long itemGradingIdForFilePicker;
	@Setter @Getter private String associatedRubricType;
	@Setter @Getter private String commentField;
	@Setter @Getter private String imageSrc = "";
	@Setter @Getter private String key;
	@Setter @Getter private String rubricStateDetails;
	@Setter @Getter private String saCharCount;
	@Setter @Getter private String sequence;
	@Setter @Getter private String tagsListToJson;
	@Setter @Getter private String[] columnArray;
	@Setter @Getter private boolean hasAssociatedRubric;
	@Setter @Getter private boolean showStudentQuestionScore;
	@Setter @Getter private boolean showStudentScore;
	@Setter private Date attemptDate;
	@Getter @Setter private List<FinBean> finArray;
	@Getter @Setter private List<MatrixSurveyBean> matrixArray;
	@Setter private List<ItemGradingData> itemGradingDataArray;
	@Setter private String gradingComment;
	@Setter private boolean addComment;
	@Setter private boolean forceRanking;
	@Setter private boolean unanswered;
	@Setter private double discount;
    private List<SelectionBean> selectionArray;
	private String leadInText;
    private String rationale;
	private String responseId = "2";
	private String responseText = "";
	private String studentComment;
	private String themeText;
    private boolean isInvalidFinInput;
	private boolean isInvalidSALengthInput;
	private boolean isMultipleItems = false;
    private int answerCounter = 1;

	public ItemContentsBean() {
		 this(new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages"));
	}

	public ItemContentsBean(ResourceLoader resourceLoader) {
		rb = resourceLoader;
	}

	public ItemContentsBean(ItemDataIfc itemData) {
		this();
		this.itemData = itemData;
		setInstruction(this.itemData.getInstruction());
		Integer sequence = this.itemData.getSequence();
		if (sequence != null) {
			setNumber(sequence.intValue());
		} else {
			setNumber(1);
		}
		this.tagsList = itemData.getItemTagSet();
		this.tagsListToJson = tagListToJsonString(this.tagsList);
	}

	private String tagListToJsonString(Set<ItemTagIfc> tagsListToConvert){

		String tagsListToJson = "[";
		if (tagsListToConvert!=null) {
			Iterator<ItemTagIfc> i = tagsListToConvert.iterator();
			Boolean more = false;
			while (i.hasNext()) {
				if (more) {
					tagsListToJson += ",";
				}
				ItemTagIfc tagToShow = (ItemTagIfc) i.next();
				String tagId = tagToShow.getTagId();
				String tagLabel = tagToShow.getTagLabel();
				String tagCollectionName = tagToShow.getTagCollectionName();
				tagsListToJson += "{\"tagId\":\"" + tagId + "\",\"tagLabel\":\"" + tagLabel + "\",\"tagCollectionName\":\"" + tagCollectionName + "\"}";
				more = true;
			}
		}
		tagsListToJson += "]";
		return tagsListToJson;
	}


    public void setTagsList(Set tagsList){ this.tagsList = tagsList;}


	public boolean getIsMultipleItems() {
		return this.isMultipleItems;
	}

	public void setIsMultipleItems(boolean isMultipleItems) {
		this.isMultipleItems = isMultipleItems;
	}

	/**
	 * In the case of an ordinary question, this will obtain the a set of text
	 * with one element and return it; in FIB return multiple elements separated
	 * by underscores.
	 * For calculated question this will return the instruction instead.
	 * 
	 * @return text of question
	 */
	public String getText() {
		String text = "";

		if (itemData != null) {
			if (itemData.getTypeId().equals(TypeIfc.CALCULATED_QUESTION)) {
			    // CALCULATED_QUESTION
				text = this.getInstruction();
			}
			else {
				text = itemData.getText();
			}
		}

		return text;
	}
	
	/**
	 * Get formatted print text for calculated questions
	 * @return
	 */
	public String getCalculatedQuestionText() {
		String text = "";
		for (FinBean fbean : this.getFinArray()) {
			if (StringUtils.isNotBlank(text)) {
				text += "______" + fbean.getText();
			} else {
				text += fbean.getText();	
			}
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
			return isNotEmpty(k);
		else
			return false;
	}
	
	public boolean getFeedbackIsNotEmpty() {
		return isNotEmpty(getFeedback());
	}

	public boolean getGradingCommentIsNotEmpty() {
		return isNotEmpty(getGradingComment());
	}

	public String getStrippedKey() {
		return strip(getKey());
	}

    /**
	 * String representation of the exact points (unrounded points)
	 * 
	 * @return String representation of the points.
	 */
	public double getExactPoints() {
		return points;
	}

	/**
	 * String representation of the rounded points.
	 * 
	 * @return String representation of the points.
	 */
	public String getPointsForEdit() {
		return Double.toString(getPoints());
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
		setPoints(Double.parseDouble(pointsForEdit));
	}
	
    /**
     * String representation of the rounded discount.
     *
     * @return String representation of the discount.
     */
    public double getDiscount() {
    	 return Precision.round(discount, 2);
    }

    /**
     * String representation of the exact points (unrounded discount)
     *
     * @return String representation of the discount.
     */
    public double getExactDiscount() {
    	return discount;
    }

    /**
     * String representation of the rounded Discount.
     *
     * @return String representation of the Discount.
     */
    public String getDiscountForEdit() {
    	return Double.toString(getDiscount());
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
    	setDiscount(Double.parseDouble(discountForEdit));
    }
	
	public boolean getReview() {
		if (getItemGradingDataArray().isEmpty()) {
			return false;
		}
		ItemGradingData data = getItemGradingDataArray().get(0);
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
			List<ItemGradingData> items = new ArrayList<ItemGradingData>();
			items.add(data);
			setItemGradingDataArray(items);
		}
		Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
		log.debug("setReview():  getItemGradingDataArray size = "
				+ getItemGradingDataArray().size());
		while (iter.hasNext()) {
			ItemGradingData data = iter.next();
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
		List<ItemGradingData> itemgradingdataArray = getItemGradingDataArray();
		if (itemgradingdataArray.isEmpty()) {
			return true;
		}
		Iterator<ItemGradingData> iter = itemgradingdataArray.iterator();
		int itemgradingsize =itemgradingdataArray.size();
		int publishedanswer_notnull = 0;
		if (getItemData().getTypeId().equals(TypeIfc.MATCHING)) 
			// SAM-776: Every choice has to be filled in before a question is considered answered 
		{
			while (iter.hasNext()) {
				ItemGradingData data = (ItemGradingData) iter.next();
				if (data.getPublishedAnswerId() != null){
					publishedanswer_notnull ++;
				}
			}
			if (publishedanswer_notnull == itemgradingsize) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			if (getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)
					|| getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK)
					|| getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) // SAM-330
					) 
			{
				if (data.getAnswerText() != null
						&& !data.getAnswerText().equals("")) {
					return false;
				}
			}
			else if (getItemData().getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION)) {
				if (StringUtils.isNotEmpty(data.getAnswerText())
						&& data.getAnswerText().matches("\\{\"x\":-?\\d+,\"y\":-?\\d+\\}")) {
					return false;
				}
			}
			else {
				if (data.getPublishedAnswerId() != null
						|| data.getAnswerText() != null) {
					return false;
				}
			}
		}
		}
		return true;
	}

    public boolean getUnanswered() {
		return unanswered;
	}

    /**
	 * String representation of the max points available for this question.
	 * 
	 * @return String representation of the max points.
	 */
	public double getRoundedMaxPoints() {
		return maxPoints;
	}
	
	public double getRoundedMaxPointsToDisplay() {
		return Precision.round(maxPoints, 2);		
	}

    /**
	 * question number
	 * 
	 * @param number
	 */
	public void setNumber(int number) {
		this.number = number;
		this.itemData.setSequence( Integer.valueOf(number));
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

	public List<ItemGradingData> getItemGradingDataArray() {
		if (itemGradingDataArray == null) {
			return new ArrayList<ItemGradingData>();
		}
		return itemGradingDataArray;
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
      Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
      if (iter.hasNext())
      {
        ItemGradingData data = iter.next();
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
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (!iter.hasNext()
					&& (presponseId == null || presponseId.equals(""))) {
				return;
			}
			ItemGradingData data = null;
			if (iter.hasNext()) {
				data = iter.next();
			} else {
				data = new ItemGradingData();
				data.setPublishedItemId(itemData.getItemId());
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
						.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
				List<ItemGradingData> items = new ArrayList<ItemGradingData>();
				items.add(data);
				setItemGradingDataArray(items);
			}
			Iterator<AnswerIfc> iterA = ((ItemTextIfc) itemData.getItemTextSet().toArray()[0])
					.getAnswerSet().iterator();
			while (iterA.hasNext()) {
				AnswerIfc answer = iterA.next();
				if (answer.getId().toString().equals(responseId)) {
					data.setPublishedAnswerId(answer.getId());
					break;
				}
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
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
      Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
      int i = 0;
      while (iter.hasNext())
      {
        ItemGradingData data = iter.next();
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
      log.error(e.getMessage(), e);
      return new String[0];
    }
  }

	public void setResponseIds(String[] presponseIds) {
		try {
			List<ItemGradingData> newItems = new ArrayList<ItemGradingData>();
            if (getItemGradingDataArray().isEmpty()
					&& (presponseIds == null || presponseIds.length == 0)) {
				return;
			}
			for (int i = 0; i < presponseIds.length; i++) {
				ItemGradingData data = null;
				Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
				while (iter.hasNext()) {
					ItemGradingData temp = iter.next();
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
					Iterator<AnswerIfc> iter2 = itemText.getAnswerSet().iterator();
					while (iter2.hasNext()) {
						AnswerIfc answer = iter2.next();
						if (answer.getId().toString().equals(presponseIds[i])) {
							data.setPublishedAnswerId(answer.getId());
						}
					}
				}
				newItems.add(data);
			}
			setItemGradingDataArray(newItems);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getResponseText() {
		log.debug("itemcontentbean.getResponseText");
		try {
			String response = responseText;
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				ItemGradingData data = iter.next();
				response = data.getAnswerText();
			}
			return response;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return responseText;
		}
	}
	
	public String getResponseTextPlain() {
		return ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(getResponseText());
	}

	public String getResponseTextForDisplay() {
		log.debug("itemcontentbean.getResponseText");
		try {
			String response = responseText;
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				ItemGradingData data = iter.next();
				response = data.getAnswerText();
			}

			if (response!=null){
				response = response.replaceAll("(\r\n|\r)", "<br/>");
				return response;
			}else{
				return responseText;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return responseText;
		}
	}

	public void setResponseTextPlain(String presponseId) {
		setResponseText(presponseId);
	}

	public void setResponseText(String presponseId) {
		log.debug("itemcontentbean.setResponseText");
		try {
			responseText = presponseId;
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (!iter.hasNext()
					&& (presponseId == null || presponseId.equals(""))) {
				return;
			}
			ItemGradingData data = null;
			if (iter.hasNext()) {
				data = iter.next();
			} else {
				data = new ItemGradingData();
				data.setPublishedItemId(itemData.getItemId());
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
						.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
				List<ItemGradingData> items = new ArrayList<ItemGradingData>();
				items.add(data);
				setItemGradingDataArray(items);
			}
			data.setAnswerText(presponseId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

    public String getSerializedImageMap() {
		StringBuffer ret = new StringBuffer();
		List<MatchingEntry> list = getMatchingArray();
		for (MatchingEntry entry : list) {
			if (!(entry instanceof ImageMapQuestionBean ib)) {
				continue;
			}
			if (ret.length() > 0)
				ret.append("#-#");

			ret.append(ib.serialize());
		}
		return ret.toString();
	}

	public void setSerializedImageMap(String serializedString) {
		if (serializedString != null) {
			HashMap<String, String> map = new HashMap<String, String>();
			List<MatchingEntry> list = getMatchingArray();

			for (String str : serializedString.split("#-#")) {
				String[] tokens = str.split("#:#");
				if (tokens.length == 2) {
					map.put(tokens[0], (tokens[1] != null) ? tokens[1] : "");
				}
			}

			for (MatchingEntry entry : list) {
				if (!(entry instanceof ImageMapQuestionBean ib)) {
					continue;
				}
				if (map.get(ib.getItemText().getId().toString()) != null)
					ib.setResponse(map.get(ib.getItemText().getId().toString()));
			}
		}
	}

    public List getSelectionArray() {
		return selectionArray;
	}

	public void setSelectionArray(List newArray) {
		selectionArray = newArray;
	}

    public boolean getForceRanking(){
		return this.forceRanking;
	}

    public boolean getAddComment(){
		return this.addComment;
	}

    public String getStudentComment() {
		try {
			String comment = studentComment;
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				ItemGradingData data = iter.next();
				comment = data.getAnswerText();
			}
			return comment;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return studentComment;
		}
	}

	public void setStudentComment(String param){
		try {
			studentComment = param;
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (!iter.hasNext()
					&& (param == null || param.equals(""))) {
				return;
			}
			ItemGradingData data = null;
			if (iter.hasNext()) {
				data = iter.next();
			} else {
				data = new ItemGradingData();
				data.setPublishedItemId(itemData.getItemId());
				ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet()
				.toArray()[0];
				data.setPublishedItemTextId(itemText.getId());
				List<ItemGradingData> items = new ArrayList<ItemGradingData>();
				items.add(data);
				setItemGradingDataArray(items);
			}
			data.setAnswerText(param);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}	

	public List<SelectItem> getSelectItemPartsMC() {
		List<SelectItem> selectItemParts = new ArrayList<SelectItem>();
		
		for(SelectionBean selection: selectionArray) {			
			selectItemParts.add(new SelectItem(selection.getAnswerId(), ""));
		}
		
		return selectItemParts;
	}

    public void setRationale(String newRationale) {
		int count = getItemGradingDataArray().size();
		ItemGradingData data = null;
		if (count <= 0) {
			data = new ItemGradingData();
			data.setPublishedItemId(itemData.getItemId());
			ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet().toArray()[0];
			data.setPublishedItemTextId(itemText.getId());
			List<ItemGradingData> items = new ArrayList<ItemGradingData>();
			items.add(data);
			setItemGradingDataArray(items);
			data = getItemGradingDataArray().get(0);
		}
		else {
		    data = getItemGradingDataArray().get(count - 1);
		}
		if ( getItemData().getTypeId().toString().equals(TypeIfc.TRUE_FALSE.toString())){
			// for True false  
			data.setRationale(newRationale);
		} 
		else if ( getItemData().getTypeId().toString().equals(TypeIfc.MULTIPLE_CORRECT.toString())) {
			//   MCMC, need to update rationale in all  itemgrading records
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator(); 
			while (iter.hasNext()) { 
				ItemGradingData mcmcdata = iter.next(); 
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
				List<ItemGradingData> items = getItemGradingDataArray();
				items.add(newdata);
				setItemGradingDataArray(items);
			}
		}
	}

	public String getRationale() {
		int count = getItemGradingDataArray().size();
		if (count > 0) {
			ItemGradingData data = getItemGradingDataArray().get(count - 1);
			rationale = ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(data.getRationale());
		}
		return Validator.check(rationale, "");
	}
	
	public String getRationaleForDisplay() {
		int count = getItemGradingDataArray().size();
		if (count > 0) {
			ItemGradingData data = getItemGradingDataArray().get(count - 1);
			if (data.getRationale() != null) {
				rationale = ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(data.getRationale()).replaceAll("(\r\n|\r)", "<br/>");
				rationale = ComponentManager.get(FormattedText.class).escapeHtml(rationale, true);
			}
		}
		return Validator.check(rationale, "");
	}

    public List<MediaData> getMediaArray() {
		List<MediaData> mediaArray = new ArrayList<>();
		ItemGradingData itemGradingData = null;
		try {
			Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
			if (iter.hasNext()) {
				itemGradingData = iter.next();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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

  private void setDurationIsOver(ItemDataIfc item, List<MediaData> mediaList){
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
      MediaData m = (MediaData) mediaList.get(i);
      double duration = (new Double(m.getDuration())).doubleValue();
      if (duration > maxDurationAllowed)
        m.setDurationIsOver(true);
      else
        m.setDurationIsOver(false);
    }
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
			pointsDisplayString = String.valueOf(Precision.round(points, 2));
		}
		return pointsDisplayString;
	}

	public String strip(String text) {
		if (text != null) {
			//text = text.replaceAll("<.*?>", " ");
			//text = FormattedText.convertFormattedTextToPlaintext(text);
			//text = FormattedText.stripHtmlFromText(text, true); // SAM-2277
			text = ComponentManager.get(FormattedText.class).stripHtmlFromText( text, false, false ).trim(); // We do not want escaped HTML to be un-escaped
		}
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

  public Double getUpdatedScore () {
      if (itemData.getScore() == null)
	  return 0.0;
      else
	  return itemData.getScore();
  }
	 
  public void setUpdatedScore(Double score) {
	  //added conditional processing 
	  if (itemData.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
		  setUpdatedScoreForEmi(score);
		  return;
	  }
	  
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
              Set<AnswerIfc> answerSet = itemText.getAnswerSet();
              Iterator<AnswerIfc> iter2 = answerSet.iterator();
              while (iter2.hasNext()) {
                  AnswerIfc answer = iter2.next();
                  log.debug("old value " + answer.getScore() +
                                     "new value " + score);
                  answer.setScore(score);
              }
              EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", itemId=" + itemData.getItemId(), true));
          }
          itemService.saveItem(item);
          itemData.setScore(score);
	  }
  }
  
  public void setUpdatedScoreForEmi(Double score) {
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

   		int answerCombinations = 0;
		double correctAnswerScore = 0.0;

          ItemDataIfc data = item.getData();
          Set itemTextSet = data.getItemTextSet();
          Iterator iter = itemTextSet.iterator();
          while (iter.hasNext()) {
              ItemTextIfc itemText = (ItemTextIfc) iter.next();
              if (!itemText.isEmiQuestionItemText()) continue;
              answerCombinations++;
          }
   		  
          iter = itemTextSet.iterator();
          while (iter.hasNext()) {
              ItemTextIfc itemText = (ItemTextIfc) iter.next();
              if (!itemText.isEmiQuestionItemText()) continue;

              int requiredOptions = itemText.getRequiredOptionsCount();
              Double optionScore = item.getScore()/answerCombinations/requiredOptions;
              Set<AnswerIfc> answerSet = itemText.getAnswerSet();
              Iterator<AnswerIfc> iter2 = answerSet.iterator();
              while (iter2.hasNext()) {
                  AnswerIfc answer = iter2.next();
                  log.debug("old value " + answer.getScore() +
                                     "new value " + optionScore);
                  answer.setScore(optionScore);
                  answer.setDiscount(optionScore);
              }
              EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "itemId=" + itemData.getItemId(), true));
          }
          
          itemService.saveItem(item);
          itemData.setScore(score);
	  }
  }
  
  public boolean getHasNoMedia() {
	return getMediaArray().size() < 1;
  }

  public String getAnswerKeyTF() {
 	String answerKey = itemData.getAnswerKey();
	if ("true".equals(answerKey)) answerKey = rb.getString("true_msg"); 
	if ("false".equals(answerKey)) answerKey = rb.getString("false_msg");
	return answerKey;
  }

  public String getAnswerKeyCalcQuestion() {
	String answerKey = "";
	if(itemData!=null){
		String answerKeyToSplit = itemData.getAnswerKey();
		if(answerKeyToSplit==null){
			return answerKey;
		}
		String keys[] = answerKeyToSplit.split(":split:");
		GradingService gradingService = new GradingService();
		for(String key: keys){
			if(!gradingService.extractVariables(key).isEmpty()){
				if(StringUtils.isNotEmpty(answerKey)){
					answerKey += ", ";
				}
				answerKey += key;
			}
		}
	}
	return answerKey;
  }
  
  public void setAttachment(Long itemGradingId){
	  List itemGradingAttachmentList = new ArrayList();
	  DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
	  Map itemContentsMap = dbean.getItemContentsMap();
      if (itemContentsMap != null)
      {
        ItemContentsBean itemContentsBean = (ItemContentsBean) itemContentsMap.get(itemGradingId);
        if (itemContentsBean != null) {
        	ItemGradingData itemGradingData = itemContentsBean.getItemGradingDataArray().get(0);
        	AttachmentUtil attachmentUtil = new AttachmentUtil();
        	Set itemGradingAttachmentSet = new HashSet();
  		    if (itemGradingAttachmentList != null) {
  			  itemGradingAttachmentSet = new HashSet(itemGradingAttachmentList);
  		    }
        	itemGradingAttachmentList = attachmentUtil.prepareAssessmentAttachment(itemGradingData, itemGradingAttachmentSet);
        	itemContentsBean.setItemGradingAttachmentList(itemGradingAttachmentList);
        }
      }
  }

    private boolean hasItemGradingAttachment = false;
  public boolean getHasItemGradingAttachment(){
	  if (itemGradingAttachmentList!=null && itemGradingAttachmentList.size() >0)
		  this.hasItemGradingAttachment = true;
	  return this.hasItemGradingAttachment;
  }

    public String getLeadInText() {
	if (leadInText == null) {
		setThemeAndLeadInText();
	}
	return leadInText;
  }


  public String getThemeText() {
	if (themeText == null) {
		setThemeAndLeadInText();
	}
	return themeText;
  }

  public void setThemeAndLeadInText() {
	themeText = itemData.getThemeText();
	leadInText = itemData.getLeadInText();
  }
  
  public void setIsInvalidFinInput(boolean isInvalidFinInput) {
	  this.isInvalidFinInput = isInvalidFinInput;
  }

  public boolean getIsInvalidFinInput() {
	  return isInvalidFinInput;
  }  

  public void setIsInvalidSALengthInput(boolean isInvalidSALengthInput) {
	  this.isInvalidSALengthInput = isInvalidSALengthInput;
  }

  public boolean getIsInvalidSALengthInput() {
	  return isInvalidSALengthInput;
  }

    // SAM-2368
  // This class allows jsp to call a method with the current EL expression version
  // #{itemContents.htmlStripped[question.text]} is using the Map Trick.
  // http://www.theserverside.com/news/1363683/JSF-Anti-Patterns-and-Pitfalls
  // Please remove this if Samigo change to use EL 2.2
  // http://stackoverflow.com/questions/8325298/invoking-methods-with-parameters-by-el-in-jsf-1-2
  
  Map htmlStripped = new Map<String,String>() {
	@Override
	public int size() { return 0; }
	@Override
	public boolean isEmpty() { return false; }
	@Override
	public boolean containsKey(Object key) { return true; }
	@Override
	public boolean containsValue(Object value) { return false; }
	@Override
	public String get(Object key) { return strip((String)key); }
	@Override
	public String put(String key, String value) { return null; }
	@Override
	public String remove(Object key) { return null;	}
	@Override
	public void putAll(Map<? extends String, ? extends String> m) {}
	@Override
	public void clear() {}
	@Override
	public Set<String> keySet() { return null; }
	@Override
	public Collection<String> values() { return null; }
	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() { return null; }
  };
  
  public Map<String,String> getHtmlStripped() {
	return htmlStripped;  
  }

  // SAM-3131 We need an index/counter of the current answer to display helper text for screen-reader users
  public int getAnswerCounter() {
    return answerCounter++;
  }

    public Long getEffectiveItemId() {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		if (author.getIsEditPendingAssessmentFlow()) {
			return itemData.getOriginalItemId();
		} else {
			return itemData.getItemId();
		}
	}

	public boolean isCancelled() {
		return ItemCancellationUtil.isCancelledOrCancellationPending(this.itemData);
	}

	public boolean isCancellable() {
		return ItemCancellationUtil.isCancellable(this.itemData);
	}
	
	public boolean isTimedQuestion() {
		String value = itemData.getItemMetaDataByLabel(ItemMetaDataIfc.TIMED);
		return (StringUtils.isNotBlank(value) && !StringUtils.equalsIgnoreCase(Boolean.FALSE.toString(), value));
	}
	
	public boolean isTrackingQuestion() {
		DeliveryBean dbean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		return dbean.isTrackingQuestions();
	}
	
	public String getTimeLimit() {
		return itemData.getItemMetaDataByLabel(ItemMetaDataIfc.TIMED);
	}
	
	public String getTimeLimitString() {
		int seconds = Integer.parseInt(getTimeLimit());
		int hour = 0;
		int minute = 0;
		if (seconds >= 3600) {
			hour = Math.abs(seconds/3600);
			minute = Math.abs((seconds-hour*3600)/60);
		} else {
			minute = Math.abs(seconds/60);
		}
		StringBuilder sb = new StringBuilder();
		if (hour > 1) {
			sb.append(hour).append(" ").append(rb.getString("time_limit_hours"));
		} else if (hour == 1) {
			sb.append(hour).append(" ").append(rb.getString("time_limit_hour"));
		}
		if(sb.length() > 0) {
			sb.append(" ");
		}
		if (minute > 1) {
			sb.append(minute).append(" ").append(rb.getString("time_limit_minutes"));
		} else if (minute == 1) {
			sb.append(minute).append(" ").append(rb.getString("time_limit_minute"));
		}
		return sb.toString();
	}
	
	public String getRealTimeLimit() {
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		return delivery.getTimeBeforeDueRetract(getTimeLimit(), getAttemptDate());
	}
	
	public Date getAttemptDate() {
		//if null get from first itemgrading
		if(attemptDate == null) {
			attemptDate = getItemGradingDataArray().stream().findFirst().orElse(new ItemGradingData()).getAttemptDate();
		}
		return attemptDate;
	}

    /**
	 * Check if current item is Enabled
	 * -1: TimedQuestion or TrackingQuestion, NOT started
	 * 0: TimedQuestion, Time expired
	 * 1: OK -> TimedQuestion started and NOT expired, or TrackingQuestion started, or NOT (TimedQuestion or TrackingQuestion)
	 * @return 
	 */
	public int getEnabled() {
		if(!isTimedQuestion() && !isTrackingQuestion()) {
			return 1;
		}
		
		Date attemptDate = getAttemptDate();
		if(attemptDate == null) {
			return -1;
		}
		
		if(isTimedQuestion()) {
			String timeBeforeDueRetract = getRealTimeLimit();
			long adjustedTimedAssesmentDueDateLong  = attemptDate.getTime() + (Long.parseLong(timeBeforeDueRetract) * 1000);
			Date endDate = new Date(adjustedTimedAssesmentDueDateLong);
			
			Date now = new Date();
			return now.before(endDate) ? 1 : 0;
		}
		return 1;
	}
	
	
	public String getTimeElapsed() {
		try {
			Date start = getAttemptDate();
			Date now = new Date();
			long ret = now.getTime() - start.getTime();
			return String.valueOf(ret/1000);
		}catch(Exception e) {
			return "0";
		}
	}

	public String getFormattedTimeElapsed() {
	    String timeElapsedInString = "";
		int timeElapsedInSeconds;
		try {
			ItemGradingData itemGrading = this.getItemGradingDataArray().get(0);
			timeElapsedInSeconds = ((int) itemGrading.getSubmittedDate().getTime()) / 1000 - ((int)itemGrading.getAttemptDate().getTime()) / 1000;
		} catch (Exception ex) {
			log.error("Question no answered, setting time as 0");
			timeElapsedInSeconds = 0;
		}
		if (timeElapsedInSeconds > 0) {
			timeElapsedInString = TimeUtil.getFormattedTime(timeElapsedInSeconds);
		}
		return timeElapsedInString;	
	}
	
	public String startTimedQuestion() {
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		
		Iterator<ItemGradingData> iter = getItemGradingDataArray().iterator();
		ItemGradingData itemGradingData = null;
		if (iter.hasNext()) {
			itemGradingData = iter.next();
		} else {
			itemGradingData = new ItemGradingData();
			itemGradingData.setAssessmentGradingId(delivery.getAssessmentGradingId());
			itemGradingData.setPublishedItemId(itemData.getItemId());
			itemGradingData.setAgentId(AgentFacade.getAgentString());
			ItemTextIfc itemText = (ItemTextIfc) itemData.getItemTextSet().toArray()[0];
			itemGradingData.setPublishedItemTextId(itemText.getId());
			List<ItemGradingData> items = getItemGradingDataArray();
			items.add(itemGradingData);
			setItemGradingDataArray(items);
		}
		
		attemptDate = itemGradingData.getAttemptDate();
		if(attemptDate == null) {
			attemptDate = new Date();
			itemGradingData.setAttemptDate(attemptDate);
		}
		
		return delivery.samePage();
	}
}


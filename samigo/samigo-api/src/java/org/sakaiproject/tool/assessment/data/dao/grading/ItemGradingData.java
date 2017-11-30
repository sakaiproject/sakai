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

package org.sakaiproject.tool.assessment.data.dao.grading;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * A response to a specific question and its associated data
 * 
 * 
 */
@Slf4j
public class ItemGradingData implements java.io.Serializable {

	private static final long serialVersionUID = 7526471155622776147L;
	private Long itemGradingId;
	private Long assessmentGradingId;
	private Long publishedItemId;
	private Long publishedItemTextId;
	private String agentId;
	private Long publishedAnswerId;
	private String rationale;
	private String answerText;
	private Date submittedDate;
	private Double autoScore;
	private Double overrideScore;
	private String comments;
	private String gradedBy;
	private Date gradedDate;
	private Boolean review;
	// these two properties are used by audio question in Samigo 2.2
	private Integer attemptsRemaining;
	private String lastDuration;
	private Boolean isCorrect;
	private List mediaArray;
	private Set<ItemGradingAttachment> itemGradingAttachmentSet = new HashSet<ItemGradingAttachment>();

	public ItemGradingData() {
	}

	public ItemGradingData(Long itemGradingId, Long assessmentGradingId) {
		this.itemGradingId = itemGradingId;
		this.assessmentGradingId = assessmentGradingId;
	}

	public Long getItemGradingId() {
		return itemGradingId;
	}

	public void setItemGradingId(Long itemGradingId) {
		this.itemGradingId = itemGradingId;
	}

	public Long getPublishedItemId() {
		return publishedItemId;
	}

	public void setPublishedItemId(Long publishedItemId) {
		this.publishedItemId = publishedItemId;
	}

	public Long getPublishedItemTextId() {
		return publishedItemTextId;
	}

	public void setPublishedItemTextId(Long publishedItemTextId) {
		this.publishedItemTextId = publishedItemTextId;
	}

	public Long getAssessmentGradingId() {
		return assessmentGradingId;
	}

	public void setAssessmentGradingId(Long assessmentGradingId) {
		this.assessmentGradingId = assessmentGradingId;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public Long getPublishedAnswerId() {
		return publishedAnswerId;
	}

	public void setPublishedAnswerId(Long publishedAnswerId) {
		this.publishedAnswerId = publishedAnswerId;
	}

	public String getRationale() {
		return rationale;
	}

	public void setRationale(String rationale) {
		this.rationale = rationale;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}

	public Double getAutoScore() {
		return autoScore;
	}

	public void setAutoScore(Double autoScore) {
		this.autoScore = autoScore;
	}

	public Double getOverrideScore() {
		return overrideScore;
	}

	public void setOverrideScore(Double overrideScore) {
		this.overrideScore = overrideScore;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getGradedBy() {
		return gradedBy;
	}

	public void setGradedBy(String gradedBy) {
		this.gradedBy = gradedBy;
	}

	public Date getGradedDate() {
		return gradedDate;
	}

	public void setGradedDate(Date gradedDate) {
		this.gradedDate = gradedDate;
	}

	public Boolean getReview() {
		return review;
	}

	public void setReview(Boolean newReview) {
		review = newReview;
	}

	public Integer getAttemptsRemaining() {
		return attemptsRemaining;
	}

	public void setAttemptsRemaining(Integer attemptsRemaining) {
		this.attemptsRemaining = attemptsRemaining;
	}

	public String getLastDuration() {
		return lastDuration;
	}

	public void setLastDuration(String lastDuration) {
		this.lastDuration = lastDuration;
	}
	
	public Boolean getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public List getMediaArray() {
		return mediaArray;
	}

	public void setMediaArray(List mediaArray) {
		this.mediaArray = mediaArray;
	}

	public int getMediaSize() {
		return mediaArray.size();
	}

	public Set<ItemGradingAttachment> getItemGradingAttachmentSet() {
		return itemGradingAttachmentSet;
	}

	public void setItemGradingAttachmentSet(
			Set<ItemGradingAttachment> itemGradingAttachmentSet) {
		this.itemGradingAttachmentSet = itemGradingAttachmentSet;
	}

	public List<ItemGradingAttachment> getItemGradingAttachmentList() {
		List<ItemGradingAttachment> list = new ArrayList<ItemGradingAttachment>();
		if (itemGradingAttachmentSet != null) {
			Iterator<ItemGradingAttachment> iter = itemGradingAttachmentSet
					.iterator();
			while (iter.hasNext()) {
				ItemGradingAttachment a = (ItemGradingAttachment) iter.next();
				list.add(a);
			}
		}
		return list;
	}

	public void setItemGradingAttachmentList(
			List<ItemGradingAttachment> itemGradingAttachmentList) {
		Set<ItemGradingAttachment> itemGradingAttachmentSet = new HashSet<ItemGradingAttachment>(itemGradingAttachmentList);
		this.itemGradingAttachmentSet = itemGradingAttachmentSet;
	}

	@Override
	public int hashCode() {
	    HashCodeBuilder builder = new HashCodeBuilder(1,31);
	    builder.append(agentId);
	    builder.append(answerText);
	    builder.append(assessmentGradingId);
	    builder.append(attemptsRemaining);
	    builder.append(autoScore);
	    builder.append(comments);
	    builder.append(gradedBy);
	    builder.append(gradedDate);
	    builder.append(itemGradingId);
	    builder.append(isCorrect);
	    builder.append(lastDuration);
	    builder.append(mediaArray);
	    builder.append(overrideScore);
	    builder.append(publishedAnswerId);
	    builder.append(publishedItemId);
	    builder.append(publishedItemTextId);
	    builder.append(rationale);
	    builder.append(review);
	    builder.append(submittedDate);
	    if (log.isDebugEnabled()) {
	        log.debug("Hashcode for Published Answer " + publishedAnswerId + " is " + builder.toHashCode());
	    }
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ItemGradingData other = (ItemGradingData) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.appendSuper(super.equals(obj));
		builder.append(agentId,other.agentId);
		builder.append(answerText,other.answerText);
		builder.append(assessmentGradingId,other.assessmentGradingId);
		builder.append(attemptsRemaining,other.attemptsRemaining);
		builder.append(autoScore,other.autoScore);
		builder.append(comments,other.comments);
		builder.append(gradedBy,other.gradedBy);
		builder.append(gradedDate,other.gradedDate);
		builder.append(itemGradingId,other.itemGradingId);
		builder.append(isCorrect,other.isCorrect);
		builder.append(lastDuration, other.lastDuration);
		builder.append(mediaArray,other.mediaArray);
		builder.append(overrideScore,other.overrideScore);
		builder.append(publishedAnswerId,other.publishedAnswerId);
		builder.append(publishedItemId,other.publishedItemId);
		builder.append(publishedItemTextId,other.publishedItemTextId);
		builder.append(rationale,other.rationale);
		builder.append(review,other.review);
		builder.append(submittedDate,other.submittedDate);
	    return builder.isEquals();
	}
}

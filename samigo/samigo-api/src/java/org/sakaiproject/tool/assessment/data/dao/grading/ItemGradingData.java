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

/**
 * A response to a specific question and its associated data
 * 
 * 
 */
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
	private Float autoScore;
	private Float overrideScore;
	private String comments;
	private String gradedBy;
	private Date gradedDate;
	private Boolean review;
	// these two properties are used by audio question in Samigo 2.2
	private Integer attemptsRemaining;
	private String lastDuration;
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

	public Float getAutoScore() {
		return autoScore;
	}

	public void setAutoScore(Float autoScore) {
		this.autoScore = autoScore;
	}

	public Float getOverrideScore() {
		return overrideScore;
	}

	public void setOverrideScore(Float overrideScore) {
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
		result = prime * result
				+ ((answerText == null) ? 0 : answerText.hashCode());
		result = prime
				* result
				+ ((assessmentGradingId == null) ? 0 : assessmentGradingId
						.hashCode());
		result = prime
				* result
				+ ((attemptsRemaining == null) ? 0 : attemptsRemaining
						.hashCode());
		result = prime * result
				+ ((autoScore == null) ? 0 : autoScore.hashCode());
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result
				+ ((gradedBy == null) ? 0 : gradedBy.hashCode());
		result = prime * result
				+ ((gradedDate == null) ? 0 : gradedDate.hashCode());
		result = prime * result
				+ ((itemGradingId == null) ? 0 : itemGradingId.hashCode());
		result = prime * result
				+ ((lastDuration == null) ? 0 : lastDuration.hashCode());
		result = prime * result
				+ ((mediaArray == null) ? 0 : mediaArray.hashCode());
		result = prime * result
				+ ((overrideScore == null) ? 0 : overrideScore.hashCode());
		result = prime
				* result
				+ ((publishedAnswerId == null) ? 0 : publishedAnswerId
						.hashCode());
		result = prime * result
				+ ((publishedItemId == null) ? 0 : publishedItemId.hashCode());
		result = prime
				* result
				+ ((publishedItemTextId == null) ? 0 : publishedItemTextId
						.hashCode());
		result = prime * result
				+ ((rationale == null) ? 0 : rationale.hashCode());
		result = prime * result + ((review == null) ? 0 : review.hashCode());
		result = prime * result
				+ ((submittedDate == null) ? 0 : submittedDate.hashCode());
		return result;
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
		if (agentId == null) {
			if (other.agentId != null)
				return false;
		} else if (!agentId.equals(other.agentId))
			return false;
		if (answerText == null) {
			if (other.answerText != null)
				return false;
		} else if (!answerText.equals(other.answerText))
			return false;
		if (assessmentGradingId == null) {
			if (other.assessmentGradingId != null)
				return false;
		} else if (!assessmentGradingId.equals(other.assessmentGradingId))
			return false;
		if (attemptsRemaining == null) {
			if (other.attemptsRemaining != null)
				return false;
		} else if (!attemptsRemaining.equals(other.attemptsRemaining))
			return false;
		if (autoScore == null) {
			if (other.autoScore != null)
				return false;
		} else if (!autoScore.equals(other.autoScore))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (gradedBy == null) {
			if (other.gradedBy != null)
				return false;
		} else if (!gradedBy.equals(other.gradedBy))
			return false;
		if (gradedDate == null) {
			if (other.gradedDate != null)
				return false;
		} else if (!gradedDate.equals(other.gradedDate))
			return false;
		if (itemGradingId == null) {
			if (other.itemGradingId != null)
				return false;
		} else if (!itemGradingId.equals(other.itemGradingId))
			return false;
		if (lastDuration == null) {
			if (other.lastDuration != null)
				return false;
		} else if (!lastDuration.equals(other.lastDuration))
			return false;
		if (mediaArray == null) {
			if (other.mediaArray != null)
				return false;
		} else if (!mediaArray.equals(other.mediaArray))
			return false;
		if (overrideScore == null) {
			if (other.overrideScore != null)
				return false;
		} else if (!overrideScore.equals(other.overrideScore))
			return false;
		if (publishedAnswerId == null) {
			if (other.publishedAnswerId != null)
				return false;
		} else if (!publishedAnswerId.equals(other.publishedAnswerId))
			return false;
		if (publishedItemId == null) {
			if (other.publishedItemId != null)
				return false;
		} else if (!publishedItemId.equals(other.publishedItemId))
			return false;
		if (publishedItemTextId == null) {
			if (other.publishedItemTextId != null)
				return false;
		} else if (!publishedItemTextId.equals(other.publishedItemTextId))
			return false;
		if (rationale == null) {
			if (other.rationale != null)
				return false;
		} else if (!rationale.equals(other.rationale))
			return false;
		if (review == null) {
			if (other.review != null)
				return false;
		} else if (!review.equals(other.review))
			return false;
		if (submittedDate == null) {
			if (other.submittedDate != null)
				return false;
		} else if (!submittedDate.equals(other.submittedDate))
			return false;
		return true;
	}
	
	
}

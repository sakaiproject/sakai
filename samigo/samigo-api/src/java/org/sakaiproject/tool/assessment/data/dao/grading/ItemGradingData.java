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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A response to a specific question and its associated data
 */
@Getter
@Setter
@EqualsAndHashCode
public class ItemGradingData implements java.io.Serializable {

  private static final long serialVersionUID = 7526471155622776147L;

  private String agentId;
  private String answerText;
  private Long assessmentGradingId;
  private Integer attemptsRemaining;
  private Double autoScore;
  private String comments;
  private String gradedBy;
  private Date gradedDate;
  private Boolean isCorrect;
  @EqualsAndHashCode.Exclude
  private Set<ItemGradingAttachment> itemGradingAttachmentSet = new HashSet<>();
  private Long itemGradingId;
  private String lastDuration;
  private List<MediaData> mediaArray;
  private Double overrideScore;
  private Long publishedAnswerId;
  private Long publishedItemId;
  private Long publishedItemTextId;
  private String rationale;
  private Boolean review;
  private Date submittedDate;

  public ItemGradingData() {}

  public ItemGradingData(Long itemGradingId, Long assessmentGradingId) {

    this.itemGradingId = itemGradingId;
    this.assessmentGradingId = assessmentGradingId;
  }
}

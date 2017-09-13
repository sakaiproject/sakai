/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.entitybroker.DeveloperHelperService;

/**
 * A json friendly representation of a DiscussionForum. Just the stuff you need, hopefully.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class SparsestForum {

	@Getter
	private Long id;
	
	@Getter
	private String title;
	
	/**
	 * An epoch date in seconds. NOT milliseconds.
	 */
	@Getter
	private Long createdDate;
	
	@Getter
	private String creator;
	
	@Getter
	private String extendedDescription;
	
	@Getter
	private String shortDescription;
	
	@Getter
	private Boolean isModerated;
	
	/**
	 * An epoch date in seconds. NOT milliseconds.
	 */
	@Getter
	private Long modifiedDate;
	
	@Getter
	private String modifier;
	
	@Getter @Setter
	private Integer totalMessages;
	
	@Getter @Setter
	private Integer readMessages;
	
	@Getter
	private Boolean isDraft;
	
	@Getter
	private Boolean isAvailabilityRestricted;
	
	/**
         * An epoch date in seconds. NOT milliseconds.
         */
	@Getter
	private Long openDate;
	
	/**
         * An epoch date in seconds. NOT milliseconds.
         */
	@Getter
	private Long closeDate;
	
	@Getter
	private Boolean isLocked;
	
	@Getter
	private Boolean isPostFirst;
	
	@Getter
	private String assocGradebookItemName;
	
	@Getter
	private List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
	
	public SparsestForum(DiscussionForum fatForum, DeveloperHelperService dhs) {
		
		this.id = fatForum.getId();
		this.title = fatForum.getTitle();
		// Epoch time in seconds for the created date
		this.createdDate = fatForum.getCreated().getTime()/1000;
		this.creator = fatForum.getCreatedBy();
		this.extendedDescription = fatForum.getExtendedDescription();
		this.shortDescription = fatForum.getShortDescription();
		this.isModerated = fatForum.getModerated();
		this.modifiedDate = fatForum.getModified().getTime()/1000;
		this.modifier = fatForum.getModifiedBy();
		this.isDraft = fatForum.getDraft();
		this.isAvailabilityRestricted = fatForum.getAvailabilityRestricted();
		
		if (this.isAvailabilityRestricted != null && this.isAvailabilityRestricted) {
		    this.openDate = fatForum.getOpenDate() != null ? fatForum.getOpenDate().getTime()/1000 : null;	
		    this.closeDate = fatForum.getCloseDate() != null ? fatForum.getCloseDate().getTime()/1000 : null;
		}
		
		this.isLocked = fatForum.getLocked();
		this.isPostFirst = fatForum.getPostFirst();
		this.assocGradebookItemName = fatForum.getDefaultAssignName();
		
		for(Attachment attachment : (List<Attachment>) fatForum.getAttachments()) {
			String url = dhs.getServerURL() + "/access/content" + attachment.getAttachmentId();
			attachments.add(new SparseAttachment(attachment.getAttachmentName(),url));
		}
	}
}

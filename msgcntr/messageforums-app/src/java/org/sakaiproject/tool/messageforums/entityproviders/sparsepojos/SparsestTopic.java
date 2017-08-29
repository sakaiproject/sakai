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

import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Topic;

public class SparsestTopic {
	
	@Getter
	private Long id;
	
	@Getter
	private String title;
	
	@Getter
	private Long createdDate;
	
	@Getter
	private String creator;
	
	@Getter
	private Long modifiedDate;
	
	@Getter
	private String modifier;
	
	@Getter
	private Boolean isAutoMarkThreadsRead;
	
	@Getter @Setter
	private Integer totalMessages = 0;
	
	@Getter @Setter
	private Integer readMessages = 0;
	
	@Getter
        private Boolean isLocked;
        
        @Getter
        private Boolean isPostFirst;
        
        @Getter
        private String assocGradebookItemName;
        
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
	
	@Getter @Setter
	private List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
	
	public SparsestTopic(Topic fatTopic) {
		
		this.id = fatTopic.getId();
		this.title = fatTopic.getTitle();
		this.createdDate = fatTopic.getCreated().getTime()/1000;
		this.creator = fatTopic.getCreatedBy();
		this.modifiedDate = fatTopic.getModified().getTime()/1000;
		this.modifier = fatTopic.getModifiedBy();
		this.isAutoMarkThreadsRead = fatTopic.getAutoMarkThreadsRead();
		this.isAvailabilityRestricted = fatTopic.getAvailabilityRestricted();
		
		if (this.isAvailabilityRestricted != null && this.isAvailabilityRestricted) {
		    this.openDate = fatTopic.getOpenDate() != null ? fatTopic.getOpenDate().getTime()/1000 : null;        
		    this.closeDate = fatTopic.getCloseDate() != null ? fatTopic.getCloseDate().getTime()/1000 : null;
		}
		
                this.assocGradebookItemName = fatTopic.getDefaultAssignName();
                
                // The Topic object is used by both the Forums and Messages tools, and there are
                // properties specific to the Forums tool that are only available on the child
                // DiscussionTopic object
                if (fatTopic instanceof DiscussionTopic) {
                    this.isLocked = ((DiscussionTopic)fatTopic).getLocked();
                    this.isPostFirst = ((DiscussionTopic)fatTopic).getPostFirst();
                }
	}
}

/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/access/trunk/access-impl/impl/src/java/org/sakaiproject/access/tool/AccessServlet.java $
 * $Id: AccessServlet.java 17063 2006-10-11 19:48:42Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.hbm.privacy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SAKAI_PRIVACY_RECORD", uniqueConstraints = @UniqueConstraint(name = "uniquePrivacyRecord", columnNames = {"contextId", "recordType", "userId"}))
@NamedQueries({
		@NamedQuery(name = "findPrivacyByUserIdContextIdType", query = "from PrivacyRecord as privacy where privacy.userId = :userId and privacy.contextId = :contextId and privacy.recordType = :recordType"),
		@NamedQuery(name = "findDisabledPrivacyUserIdContextIdType", query = "from PrivacyRecord as privacy where privacy.userId = :userId and privacy.contextId = :contextId and privacy.recordType = :recordType and privacy.viewable = :viewable"),
		@NamedQuery(name = "finalPrivacyByContextViewableType", query = "from PrivacyRecord as privacy where privacy.contextId = :contextId and privacy.viewable = :viewable and privacy.recordType = :recordType"),
		@NamedQuery(name = "finalPrivacyByContextType", query = "from PrivacyRecord as privacy where privacy.contextId = :contextId and privacy.recordType = :recordType"),
		@NamedQuery(name = "finalPrivacyByContextViewableTypeUserList", query = "from PrivacyRecord as privacy where privacy.contextId = :contextId and privacy.viewable = :viewable and privacy.recordType = :recordType and privacy.userId in (:userIds)"),
		@NamedQuery(name = "finalPrivacyByContextTypeAndUserIds", query = "from PrivacyRecord as privacy where privacy.contextId = :contextId and privacy.recordType = :recordType and privacy.userId in (:userIds)")
})

@Data
@NoArgsConstructor
public class PrivacyRecord
{
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "privacy_record_sequence")
	@SequenceGenerator(name = "privacy_record_sequence", sequenceName = "PrivacyRecordImpl_SEQ")
	private Long surrogateKey;

	@Version
	@Column
	private Integer lockId;

	@Column(length = 100, nullable = false)
	private String contextId;

	@Column(length = 100, nullable = false)
	private String recordType;

	@Column(length = 100, nullable = false)
	private String userId;

	@Column(nullable = false)
	private boolean viewable;
	
	public PrivacyRecord(String userId, String contextId, String recordType, boolean viewable) {
		this.userId = userId;
		this.contextId = contextId;
		this.recordType = recordType;
		this.viewable = viewable;
	}
}

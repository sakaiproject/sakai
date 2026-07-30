/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.content.hbm;

import java.util.Date;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "content_resource_lock")
@BatchSize(size = 10)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = "getLocks", query = "from Lock w where asset_id = :asset and qualifier_id = :qualifier"),
		@NamedQuery(name = "getActiveAssets", query = "from Lock w where is_active is true and asset_id = :asset"),
		@NamedQuery(name = "getActiveQualifierLocks", query = "from Lock w where is_active is true and qualifier_id = :qualifier")
})

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class Lock implements org.sakaiproject.content.api.Lock {
	@Id
	@Column(name = "id", length = 36)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "asset_id", length = 36)
	private String asset;

	@Column(name = "qualifier_id", length = 36)
	private String qualifier;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "is_system")
	private boolean system;

	@Column(name = "reason", length = 36)
	private String reason;

	@Column(name = "date_added")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdded;

	@Column(name = "date_removed")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateRemoved;
}
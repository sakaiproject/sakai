/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.api.model;

import java.util.Map;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PLUS_LINK",
  indexes = { @Index(columnList = "LINK, CONTEXT_GUID, SAKAI_TOOL_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "LINK", "CONTEXT_GUID" }) }
)
@Getter
@Setter
public class Link extends BaseLTI implements PersistableEntity<String> {

	@Id
	@Column(name = "LINK_GUID", length = LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "LINK", length = LENGTH_EXTERNAL_ID, nullable = false)
	private String link;

	@Column(name = "SAKAI_TOOL_ID", length = LENGTH_SAKAI_ID, nullable = true)
	private String sakaiToolId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTEXT_GUID", nullable = false)
	private Context context;

	// We don't have a LineItem here because Plus ignores Basic Outcomes.
	// Sakai has no internal concept of a single per-tool grade to return anyways.
	// So we are all in on dynamically creating lineItems that correspond to
	// a GB_GRADABLE_OBJECT - if we were to someday model a basic outcome
	// we would make a different class, perhaps one that extends and overrides
	// LineItem.

	@Column(name = "TITLE", length = LENGTH_TITLE, nullable = true)
	private String title;

	@Column(name = "DESCRIPTION", length = LENGTH_MEDIUMTEXT, nullable = true)
	private String description;

	// vim: tabstop=4 noet
}

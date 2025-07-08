/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Hibernate model for an uploaded profile image
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "PROFILE_IMAGES_UPLOADED_T")
public class ProfileImageUploaded implements PersistableEntity<String> {

	@EqualsAndHashCode.Include
    @Id
    @Column(name = "USER_ID", length = 99)
	private String userId;

	@Column(name = "RESOURCE_MAIN", length = 4000, nullable = false)
	private String mainResource;

    @Column(name = "RESOURCE_THUMB", length = 4000, nullable = false)
	private String thumbnailResource;

    @Column(name = "RESOURCE_AVATAR", length = 4000, nullable = false)
	private String avatarResource;

    @Column(name = "IS_CURRENT", nullable = false)
	private Boolean current;

	/** 
	 * Additional constructor to create a ProfileImage record in one go
	 */
	public ProfileImageUploaded(String userId, String mainResource, String thumbnailResource, String avatarResource, boolean current) {

		this.userId = userId;
		this.mainResource = mainResource;
		this.thumbnailResource = thumbnailResource;
		this.avatarResource = avatarResource;
		this.current = current;
	}

    public String getId() {
        return userId;
    }
}

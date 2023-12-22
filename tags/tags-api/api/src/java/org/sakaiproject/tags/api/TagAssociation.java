/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.tags.api;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "FIND_ASSOCIATIONS_BY_ITEM_AND_COLLECTION",
                query = "select ta.* from tagservice_tagassociation ta, tagservice_tag t where item_id = :itemId and tagcollectionid = :collectionId and t.tagid = ta.tag_id",
                resultClass = TagAssociation.class),
})
@Entity
@NoArgsConstructor
@Table(name = "tagservice_tagassociation",
	uniqueConstraints = @UniqueConstraint(columnNames = { "tag_id", "item_id" })
)
public class TagAssociation {

	@Id
	@Column(name = "id", length = 99, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "tag_id")
	@NonNull
	private String tagId;

	@Column(name = "item_id")
	@NonNull
	private String itemId;

}

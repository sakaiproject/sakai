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

import java.util.List;

import org.sakaiproject.tags.api.TagAssociation;
import org.sakaiproject.serialization.SerializableRepository;

public interface TagAssociationRepository extends SerializableRepository<TagAssociation, String> {

	void newTagAssociation(TagAssociation tagAssociation);
	void deleteTagAssociation(String associationId);
	List<TagAssociation> findTagAssociationByItemId(String itemId);
	TagAssociation findTagAssociationByItemIdAndTagId(String itemId, String tagId);
}

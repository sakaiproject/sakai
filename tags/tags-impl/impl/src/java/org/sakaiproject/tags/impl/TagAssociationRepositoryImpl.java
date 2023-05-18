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
package org.sakaiproject.tags.impl;

import java.util.List;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.serialization.BasicSerializableRepository;
import org.sakaiproject.tags.api.TagAssociation;
import org.sakaiproject.tags.api.TagAssociationRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class TagAssociationRepositoryImpl extends BasicSerializableRepository<TagAssociation, String>  implements TagAssociationRepository {

	@Override
	@Transactional
	public void newTagAssociation(TagAssociation tagAssociation) {
		sessionFactory.getCurrentSession().persist(tagAssociation);
	}

	@Override
	@Transactional
	public void deleteTagAssociation(String assocId) {
		TagAssociation tagAssociation = findOne(assocId);
		if (tagAssociation != null) {
			delete(tagAssociation);
		}
	}

	@Override
	public List<TagAssociation> findTagAssociationByItemId(String itemId) {
		return startCriteriaQuery()
				.add(Restrictions.eq("itemId", itemId))
				.list();
	}

	@Override
	public TagAssociation findTagAssociationByItemIdAndTagId(String itemId, String tagId) {
		return (TagAssociation) startCriteriaQuery()
				.add(Restrictions.eq("itemId", itemId))
				.add(Restrictions.eq("tagId", tagId))
				.uniqueResult();
	}
}

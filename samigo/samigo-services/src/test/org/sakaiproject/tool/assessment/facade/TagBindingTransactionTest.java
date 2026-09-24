/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2026 Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-hibernate.xml")
public class TagBindingTransactionTest {

    @Autowired private ItemFacadeQueriesAPI itemFacadeQueries;
    @Autowired private PublishedItemFacadeQueriesAPI publishedItemFacadeQueries;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private SessionFactory sessionFactory;

    @Test
    public void tagBindingWritesRunAfterSourceTransactionCommits() {
        TagServiceHelper.TagView tag = new TagServiceHelper.TagView("update-tag", "Updated label", "update-collection", "Collection from tag");
        TagServiceHelper.TagCollectionView collection = new TagServiceHelper.TagCollectionView("update-collection", "Updated collection");
        Long[] itemIds = new Long[2];
        new TransactionTemplate(transactionManager).execute(status -> {
            Session session = sessionFactory.getCurrentSession();
            ItemData item = new ItemData();
            item.setTypeId(1L);
            item.setStatus(1);
            item.setCreatedBy("test");
            item.setCreatedDate(new Date());
            item.setLastModifiedBy("test");
            item.setLastModifiedDate(new Date());
            session.save(item);
            itemIds[0] = item.getItemId();

            PublishedAssessmentData assessment = new PublishedAssessmentData();
            assessment.setStatus(1);
            assessment.setCreatedBy("test");
            assessment.setCreatedDate(new Date());
            assessment.setLastModifiedBy("test");
            assessment.setLastModifiedDate(new Date());
            session.save(assessment);
            PublishedSectionData section = new PublishedSectionData();
            section.setAssessment(assessment);
            section.setTypeId(1L);
            section.setStatus(1);
            section.setCreatedBy("test");
            section.setCreatedDate(new Date());
            section.setLastModifiedBy("test");
            section.setLastModifiedDate(new Date());
            session.save(section);
            PublishedItemData publishedItem = new PublishedItemData();
            publishedItem.setSection(section);
            publishedItem.setTypeId(1L);
            publishedItem.setStatus(1);
            publishedItem.setCreatedBy("test");
            publishedItem.setCreatedDate(new Date());
            publishedItem.setLastModifiedBy("test");
            publishedItem.setLastModifiedDate(new Date());
            session.save(publishedItem);
            itemIds[1] = publishedItem.getItemId();

            for (String[] binding : new String[][] {
                    {"update-tag", "update-collection"},
                    {"delete-tag", "other-collection"},
                    {"other-tag", "delete-collection"}}) {
                session.save(new ItemTag(item, binding[0], "Original label", binding[1], "Original collection"));
                session.save(new PublishedItemTag(publishedItem, binding[0], "Original label", binding[1], "Original collection"));
            }
            return null;
        });

        new TransactionTemplate(transactionManager).execute(status -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    itemFacadeQueries.updateItemTagBindingsHavingTag(tag);
                    publishedItemFacadeQueries.updateItemTagBindingsHavingTag(tag);
                    itemFacadeQueries.updateItemTagBindingsHavingTagCollection(collection);
                    publishedItemFacadeQueries.updateItemTagBindingsHavingTagCollection(collection);
                    itemFacadeQueries.deleteItemTagBindingsHavingTagId("delete-tag");
                    publishedItemFacadeQueries.deleteItemTagBindingsHavingTagId("delete-tag");
                    itemFacadeQueries.deleteItemTagBindingsHavingTagCollectionId("delete-collection");
                    publishedItemFacadeQueries.deleteItemTagBindingsHavingTagCollectionId("delete-collection");
                }
            });
            return null;
        });

        new TransactionTemplate(transactionManager).execute(status -> {
            Session session = sessionFactory.getCurrentSession();
            List<ItemTag> draft = session.createQuery("from ItemTag it where it.item.itemId = :itemId", ItemTag.class)
                .setParameter("itemId", itemIds[0]).getResultList();
            List<PublishedItemTag> published = session.createQuery("from PublishedItemTag it where it.item.itemId = :itemId", PublishedItemTag.class)
                .setParameter("itemId", itemIds[1]).getResultList();
            assertEquals(1, draft.size());
            assertEquals(1, published.size());
            assertEquals("update-tag", draft.get(0).getTagId());
            assertEquals("update-collection", draft.get(0).getTagCollectionId());
            assertEquals("Updated label", draft.get(0).getTagLabel());
            assertEquals("Updated collection", draft.get(0).getTagCollectionName());
            assertEquals("update-tag", published.get(0).getTagId());
            assertEquals("update-collection", published.get(0).getTagCollectionId());
            assertEquals("Updated label", published.get(0).getTagLabel());
            assertEquals("Updated collection", published.get(0).getTagCollectionName());
            return null;
        });
    }
}

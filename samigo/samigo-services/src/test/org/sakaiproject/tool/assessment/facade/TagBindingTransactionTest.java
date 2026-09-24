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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.tool.assessment.integration.helper.ifc.TagServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-hibernate.xml")
public class TagBindingTransactionTest {

    @Autowired private ItemFacadeQueriesAPI itemFacadeQueries;
    @Autowired private PublishedItemFacadeQueriesAPI publishedItemFacadeQueries;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    public void tagBindingWritesRunAfterSourceTransactionCommits() {
        TagServiceHelper.TagView tag = new TagServiceHelper.TagView("tag", "Label", "collection", "Collection");
        TagServiceHelper.TagCollectionView collection = new TagServiceHelper.TagCollectionView("collection", "Collection");

        new TransactionTemplate(transactionManager).execute(status -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    itemFacadeQueries.updateItemTagBindingsHavingTag(tag);
                    publishedItemFacadeQueries.updateItemTagBindingsHavingTag(tag);
                    itemFacadeQueries.updateItemTagBindingsHavingTagCollection(collection);
                    publishedItemFacadeQueries.updateItemTagBindingsHavingTagCollection(collection);
                    itemFacadeQueries.deleteItemTagBindingsHavingTagId(tag.tagId);
                    publishedItemFacadeQueries.deleteItemTagBindingsHavingTagId(tag.tagId);
                    itemFacadeQueries.deleteItemTagBindingsHavingTagCollectionId(collection.tagCollectionId);
                    publishedItemFacadeQueries.deleteItemTagBindingsHavingTagCollectionId(collection.tagCollectionId);
                }
            });
            return null;
        });
    }
}

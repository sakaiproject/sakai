/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.dao;

import java.util.Date;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Testing for the Evaluation Data Access Layer
 */
@Slf4j
@ContextConfiguration({ "/hibernate-test.xml", "/spring-hibernate.xml" })
public class ContentReviewDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	protected ContentReviewItemDao itemDao;

	private static final String USER = "dhorwitz";
	private static final Integer providerId = "test".hashCode(); 

	@Test
	public void testSave() {
		ContentReviewItem itemA = new ContentReviewItem(USER, "site", "task", "content-A", new Date(),
				ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
		itemDao.create(itemA);
		
		log.info("Verifying itemA save: " + itemA.toString());
		
		Optional<ContentReviewItem> item = itemDao.get(itemA.getId());
		if (item.isPresent()) {
			Assert.assertEquals(itemA.getId(), item.get().getId());
			Assert.assertEquals(itemA.getSiteId(), item.get().getSiteId());
			Assert.assertEquals(itemA.getTaskId(), item.get().getTaskId());
			Assert.assertEquals(itemA.getContentId(), item.get().getContentId());
		} else {
			Assert.fail("itemA was null");
		}
	}

	@Test
	public void testDeleteT() {
		ContentReviewItem itemB = new ContentReviewItem(USER, "site", "task", "content-B", new Date(),
				ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
		itemDao.create(itemB);
		
		Optional<ContentReviewItem> item = itemDao.get(itemB.getId());
		
		if (item.isPresent()) {
			log.info("Retrieved itemB: " + item.get());
			itemDao.delete(item.get());
		}
		Assert.assertEquals(Optional.empty(), itemDao.get(item.get().getId()));
	}

	@Test
	public void testDeleteLong() {
		ContentReviewItem itemC = new ContentReviewItem(USER, "site", "task", "content-C", new Date(),
				ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
		itemDao.create(itemC);
		
		Optional<ContentReviewItem> item = itemDao.get(itemC.getId());
		
		if (item.isPresent()) {
			log.info("Retrieved itemC: " + item.get());
			itemDao.delete(item.get().getId());
		}
		Assert.assertEquals(Optional.empty(), itemDao.get(item.get().getId()));
	}

	// TODO finish this
	@Test
	public void testUpdate() {
		ContentReviewItem itemD = new ContentReviewItem(USER, "site", "task", "content-D", new Date(),
				ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
		itemDao.create(itemD);
		
		log.info("Create itemD: " + itemD.toString());
		
		Optional<ContentReviewItem> item = itemDao.get(itemD.getId());
		
		if (item.isPresent()) {
			item.get().setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
			itemDao.save(item.get());
			
			Optional<ContentReviewItem> updatedItem = itemDao.get(itemD.getId());
			if (updatedItem.isPresent()) {
				log.info("Updated itemD: " + itemD.toString());
				Assert.assertEquals(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE, updatedItem.get().getStatus());
				return;		// test passed
			}
		}
		Assert.fail("Update of itemD failed");
	}

//	@Test
//	public void testOptimisticLock() {
//		itemA = new ContentReviewItem(USER, "site", "task", "content-A", new Date(),
//				ContentReviewConstants.NOT_SUBMITTED_CODE);
//		itemDao.save(itemA);
//
//		// first test we have saved the item
//		Assert.assertNotNull(itemA.getId());
//
//		itemB = new ContentReviewItem(USER, "site", "task", "content-B", new Date(),
//				ContentReviewConstants.NOT_SUBMITTED_CODE);
//		itemDao.create(itemB);
//		Assert.assertNotNull(itemB.getId());
//
//		Assert.assertNotSame(itemA.getId(), itemB.getId());
//		
//		Optional<ContentReviewItem> itemC = itemDao.get(itemB.getId());
//
//		// update ItemB
//		itemB.setStatus(ContentReviewConstants.SUBMISSION_ERROR_USER_DETAILS_CODE);
//		itemDao.save(itemB);
//
//		if (itemC.isPresent()) {
//			itemC.get().setStatus(ContentReviewConstants.SUBMISSION_ERROR_RETRY_CODE);
//			itemDao.save(itemC.get());
//		}
//	}
}

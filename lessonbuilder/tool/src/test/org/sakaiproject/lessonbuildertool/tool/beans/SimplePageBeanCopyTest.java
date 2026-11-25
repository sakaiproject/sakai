/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.tool.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.org.ponder.messageutil.MessageLocator;

public class SimplePageBeanCopyTest {

	private static final String SITE_ID = "site-1";
	private static final String TOOL_PAGE_ID = "tool-page";

	private SimplePageBean bean;
	private SimplePageToolDao dao;
	private GradebookIfc gradebookIfc;
	private AtomicLong nextPageId;
	private AtomicLong nextItemId;
	private List<Object> savedObjects;
	private boolean failNextSave;

	@Before
	public void setUp() {
		dao = mock(SimplePageToolDao.class);
		gradebookIfc = mock(GradebookIfc.class);
		MessageLocator messageLocator = mock(MessageLocator.class);
		ToolManager toolManager = mock(ToolManager.class);
		SessionManager sessionManager = mock(SessionManager.class);
		ToolSession toolSession = mock(ToolSession.class);
		ToolConfiguration placement = mock(ToolConfiguration.class);

		when(messageLocator.getMessage(anyString())).thenReturn("error");
		when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
		when(toolManager.getCurrentPlacement()).thenReturn(placement);
		when(placement.getPageId()).thenReturn(TOOL_PAGE_ID);
		when(placement.getContext()).thenReturn(SITE_ID);

		nextPageId = new AtomicLong(100);
		nextItemId = new AtomicLong(1000);
		savedObjects = new ArrayList<>();
		failNextSave = false;

		when(dao.makePage(anyString(), anyString(), anyString(), any(), any())).thenAnswer(invocation ->
				new SimplePageImpl(invocation.getArgument(0), invocation.getArgument(1),
						invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4)));
		when(dao.copyItem(any(SimplePageItem.class))).thenAnswer(invocation -> {
			SimplePageItem old = invocation.getArgument(0);
			SimplePageItem item = new SimplePageItemImpl();
			item.setPageId(old.getPageId());
			item.setSequence(old.getSequence());
			item.setType(old.getType());
			item.setSakaiId(old.getSakaiId());
			item.setName(old.getName());
			item.setHtml(old.getHtml());
			return item;
		});
		when(dao.copyItem2(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));
		when(dao.saveItem(any(), anyList(), anyString(), anyBoolean())).thenAnswer(invocation -> {
			if (failNextSave) {
				failNextSave = false;
				List<String> elist = invocation.getArgument(1);
				elist.add("save failed");
				return false;
			}
			Object o = invocation.getArgument(0);
			if (o instanceof SimplePage page && page.getPageId() == 0) {
				page.setPageId(nextPageId.getAndIncrement());
			} else if (o instanceof SimplePageItem item && item.getId() == 0) {
				item.setId(nextItemId.getAndIncrement());
			}
			savedObjects.add(o);
			return true;
		});
		when(dao.update(any(), anyList(), anyString(), anyBoolean())).thenReturn(true);
		when(dao.findItemsOnPage(any(Long.class))).thenReturn(Collections.emptyList());

		bean = new SimplePageBean();
		bean.setSimplePageToolDao(dao);
		bean.setGradebookIfc(gradebookIfc);
		bean.setMessageLocator(messageLocator);
		bean.setToolManager(toolManager);
		bean.setSessionManager(sessionManager);
		bean.setCurrentSiteId(SITE_ID);

		SimplePage current = page(1L, null, null, "Current");
		bean.setCurrentPage(current);
		bean.setCurrentPageId(1L);
		when(dao.getPage(1L)).thenReturn(current);
	}

	@Test
	public void resolveTopParentUsesDestinationHierarchy() {
		SimplePage nestedParent = page(50L, 10L, 10L, "Nested parent");
		when(dao.getPage(50L)).thenReturn(nestedParent);

		assertEquals(Long.valueOf(10L), bean.resolveTopParent(50L));
		assertEquals(Long.valueOf(1L), bean.resolveTopParent(1L));
		assertEquals(Long.valueOf(1L), bean.resolveTopParent(null));
	}

	@Test
	public void duplicateSubpageTreeRejectsPagesUserCannotEdit() {
		when(dao.canEditPage(20L)).thenReturn(false);

		Long result = bean.duplicateSubpageTree(20L, 1L, new HashMap<>());

		assertNull(result);
		verify(dao, never()).getPage(20L);
		assertTrue(savedObjects.isEmpty());
	}

	@Test
	public void duplicateSubpageTreeUsesDestinationTopParentAndRegistersGradebook() {
		SimplePage source = page(20L, null, null, "Graded source");
		source.setGradebookPoints(10.0);
		SimplePage destinationParent = page(30L, null, null, "Destination");
		when(dao.getPage(20L)).thenReturn(source);
		when(dao.getPage(30L)).thenReturn(destinationParent);
		when(dao.canEditPage(20L)).thenReturn(true);
		when(dao.findItemsOnPage(20L)).thenReturn(Collections.emptyList());
		when(gradebookIfc.isGradebookGroupEnabled(SITE_ID)).thenReturn(false);
		when(gradebookIfc.addExternalAssessment(anyList(), eq(SITE_ID), eq("lesson-builder:100"),
				any(), eq("Graded source"), eq(10.0), any(), eq(LessonBuilderConstants.TOOL_ID)))
				.thenReturn(true);

		Map<Long, Long> pageMap = new HashMap<>();
		Long newId = bean.duplicateSubpageTree(20L, 30L, pageMap);

		assertEquals(Long.valueOf(100L), newId);
		assertEquals(Long.valueOf(100L), pageMap.get(20L));
		SimplePage created = (SimplePage) savedObjects.get(0);
		assertEquals(Long.valueOf(30L), created.getParent());
		assertEquals(Long.valueOf(30L), created.getTopParent());
		assertEquals(Double.valueOf(10.0), created.getGradebookPoints());
		verify(gradebookIfc).addExternalAssessment(anyList(), eq(SITE_ID), eq("lesson-builder:100"),
				any(), eq("Graded source"), eq(10.0), any(), eq(LessonBuilderConstants.TOOL_ID));
	}

	@Test
	public void duplicateSubpageTreeClearsPointsWhenGradebookRegistrationFails() {
		SimplePage source = page(21L, null, null, "Graded source");
		source.setGradebookPoints(7.0);
		when(dao.getPage(21L)).thenReturn(source);
		when(dao.canEditPage(21L)).thenReturn(true);
		when(dao.findItemsOnPage(21L)).thenReturn(Collections.emptyList());
		when(gradebookIfc.isGradebookGroupEnabled(SITE_ID)).thenReturn(false);
		when(gradebookIfc.addExternalAssessment(anyList(), eq(SITE_ID), anyString(),
				any(), anyString(), eq(7.0), any(), eq(LessonBuilderConstants.TOOL_ID)))
				.thenReturn(false);

		Long newId = bean.duplicateSubpageTree(21L, 1L, new HashMap<>());

		assertNotNull(newId);
		SimplePage created = (SimplePage) savedObjects.get(0);
		assertNull(created.getGradebookPoints());
	}

	@Test
	public void duplicateSubpageTreePreservesUngradedSource() {
		SimplePage source = page(22L, null, null, "Ungraded");
		source.setGradebookPoints(null);
		when(dao.getPage(22L)).thenReturn(source);
		when(dao.canEditPage(22L)).thenReturn(true);
		when(dao.findItemsOnPage(22L)).thenReturn(Collections.emptyList());

		bean.duplicateSubpageTree(22L, 1L, new HashMap<>());

		SimplePage created = (SimplePage) savedObjects.get(0);
		assertNull(created.getGradebookPoints());
		verify(gradebookIfc, never()).addExternalAssessment(anyList(), anyString(), anyString(),
				any(), anyString(), anyDouble(), any(), anyString());
	}

	@Test
	public void duplicateSubpageTreeDoesNotRecordUnsavedPageInMap() {
		SimplePage source = page(23L, null, null, "Failing");
		when(dao.getPage(23L)).thenReturn(source);
		when(dao.canEditPage(23L)).thenReturn(true);
		failNextSave = true;

		Map<Long, Long> pageMap = new HashMap<>();
		Long result = bean.duplicateSubpageTree(23L, 1L, pageMap);

		assertNull(result);
		assertTrue(pageMap.isEmpty());
		assertTrue(savedObjects.isEmpty());
	}

	@Test
	public void prepareCopiedItemFailsWhenNestedDuplicateFails() {
		SimplePageItem oldItem = new SimplePageItemImpl(1, 1, 1, SimplePageItem.PAGE, "99", "Child");
		SimplePageItem newItem = new SimplePageItemImpl(0, 30, 1, SimplePageItem.PAGE, "99", "Child");
		when(dao.canEditPage(99L)).thenReturn(false);

		boolean ok = bean.prepareCopiedItem(newItem, oldItem, SITE_ID,
				SimplePageBean.SUBPAGE_COPY_DUPLICATE, new HashMap<>());

		assertFalse(ok);
		assertEquals("99", newItem.getSakaiId());
	}

	@Test
	public void copyPageItemsRollsBackWhenNestedDuplicateFails() {
		SimplePageItem pageLink = new SimplePageItemImpl(5, 1, 1, SimplePageItem.PAGE, "40", "Child");
		when(dao.findItemsOnPage(1L)).thenReturn(List.of(pageLink));
		when(dao.findItemsOnPage(40L)).thenReturn(Collections.emptyList());
		when(dao.canEditPage(40L)).thenReturn(false);
		when(dao.getPage(40L)).thenReturn(page(40L, null, null, "Child"));

		boolean ok = bean.copyPageItems(1L, 30L, SimplePageBean.SUBPAGE_COPY_DUPLICATE, SITE_ID);

		assertFalse(ok);
		assertTrue(savedObjects.stream().noneMatch(o -> o instanceof SimplePageItem));
	}

	@Test
	public void prepareCopiedItemSetsDuplicatedSakaiIdWhenPageIdAlreadyAssigned() {
		SimplePage sourceChild = page(40L, 1L, 1L, "Child");
		when(dao.getPage(40L)).thenReturn(sourceChild);
		when(dao.canEditPage(40L)).thenReturn(true);
		when(dao.findItemsOnPage(40L)).thenReturn(Collections.emptyList());

		SimplePageItem oldItem = new SimplePageItemImpl(5, 1, 1, SimplePageItem.PAGE, "40", "Child");
		SimplePageItem newItem = new SimplePageItemImpl(0, 0, 1, SimplePageItem.PAGE, "40", "Child");
		newItem.setPageId(55L);

		boolean ok = bean.prepareCopiedItem(newItem, oldItem, SITE_ID,
				SimplePageBean.SUBPAGE_COPY_DUPLICATE, new HashMap<>());

		assertTrue(ok);
		assertEquals("100", newItem.getSakaiId());
		SimplePage created = (SimplePage) savedObjects.get(0);
		assertEquals(Long.valueOf(55L), created.getParent());
		assertEquals(Long.valueOf(55L), created.getTopParent());
	}

	private static SimplePage page(long id, Long parent, Long topParent, String title) {
		SimplePageImpl page = new SimplePageImpl(TOOL_PAGE_ID, SITE_ID, title, parent, topParent);
		page.setPageId(id);
		return page;
	}
}

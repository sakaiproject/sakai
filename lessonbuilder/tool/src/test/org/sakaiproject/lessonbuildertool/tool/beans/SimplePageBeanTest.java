/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.sakaiproject.lessonbuildertool.tool.beans;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.org.ponder.messageutil.MessageLocator;

public class SimplePageBeanTest {

    private static final String CSRF = "csrf-token";
    private static final String SITE_ID = "site-1";

    private SimplePageBean simplePageBean;
    private SimplePageToolDao dao;
    private SessionManager sessionManager;
    private Session session;
    private SecurityService securityService;

    @Before
    public void before() {
        MessageLocator messageLocator = mock(MessageLocator.class);
        dao = mock(SimplePageToolDao.class);
        MemoryService memoryService = mock(MemoryService.class);
        sessionManager = mock(SessionManager.class);
        session = mock(Session.class);
        securityService = mock(SecurityService.class);
        ToolSession toolSession = mock(ToolSession.class);

        try (MockedStatic<ComponentManager> cm = mockStatic(ComponentManager.class);
             MockedStatic<ServerConfigurationService> scs = mockStatic(ServerConfigurationService.class)) {
            cm.when(() -> ComponentManager.get("org.sakaiproject.memory.api.MemoryService")).thenReturn(memoryService);
            scs.when(() -> ServerConfigurationService.getString("lessonbuilder.html.types", "html,xhtml,htm,xht")).thenReturn("html,xhtml,htm,xht");
            simplePageBean = new SimplePageBean();
        }

        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        when(session.getAttribute("sakai.csrf.token")).thenReturn(CSRF);
        when(securityService.unlock(eq(SimplePage.PERMISSION_LESSONBUILDER_UPDATE), anyString())).thenReturn(true);
        when(messageLocator.getMessage(anyString())).thenReturn("message");
        when(dao.saveItemBatch(any(), any(), any(), anyString())).thenReturn(true);

        simplePageBean.setMessageLocator(messageLocator);
        simplePageBean.setSimplePageToolDao(dao);
        simplePageBean.setSessionManager(sessionManager);
        simplePageBean.setSecurityService(securityService);
        simplePageBean.setCsrfToken(CSRF);
        simplePageBean.setCurrentSiteId(SITE_ID);
        simplePageBean.setCurrentPageId(99L);

        // common data for tests
        SimplePageItem i1 = new SimplePageItemImpl(1, 0, 0, 2, "1", "Lessons");
        SimplePageItem i2 = new SimplePageItemImpl(2, 1, 1, 2, "2", "SubPage 1");
        SimplePageItem i3 = new SimplePageItemImpl(3, 1, 2, 2, "3", "SubPage 2");
        SimplePageItem i4 = new SimplePageItemImpl(4, 2, 1, 2, "4", "SubPage 1 - 1");
        SimplePageItem i5 = new SimplePageItemImpl(5, 2, 2, 2, "5", "SubPage 1 - 2");
        SimplePageItem i6 = new SimplePageItemImpl(6, 3, 1, 2, "6", "SubPage 2 - 1");
        List<SimplePageItem> l1 = Collections.singletonList(i1);
        List<SimplePageItem> l2 = Collections.singletonList(i2);
        List<SimplePageItem> l3 = Collections.singletonList(i3);
        List<SimplePageItem> l4 = Collections.singletonList(i4);
        List<SimplePageItem> l5 = Collections.singletonList(i5);
        List<SimplePageItem> l6 = Collections.singletonList(i6);
        when(dao.findItemsBySakaiId("1")).thenReturn(l1);
        when(dao.findItemsBySakaiId("2")).thenReturn(l2);
        when(dao.findItemsBySakaiId("3")).thenReturn(l3);
        when(dao.findItemsBySakaiId("4")).thenReturn(l4);
        when(dao.findItemsBySakaiId("5")).thenReturn(l5);
        when(dao.findItemsBySakaiId("6")).thenReturn(l6);
        when(simplePageBean.getMessageLocator().getMessage("simplepage.printall.continuation")).thenReturn("continuation");
    }

    @Test
    public void subPagePathTest() {
        SimplePageItem i6 = new SimplePageItemImpl(6, 3, 1, 2, "6", "SubPage 2 - 1");
        String path = simplePageBean.getSubPagePath(i6, false);
        Assert.assertEquals("Lessons > SubPage 2", path);

        path = simplePageBean.getSubPagePath(i6, true);
        Assert.assertEquals("Lessons (continuation) SubPage 2", path);
    }

    @Test
    public void infiniteSubPagePathTest() {
        SimplePageItem i7 = new SimplePageItemImpl(7, 0, 0, 2, "0", "Infinite");
        SimplePageItem i5 = new SimplePageItemImpl(5, 2, 2, 2, "5", "SubPage 1 - 2");
        List<SimplePageItem> l7 = Collections.singletonList(i7);
        when(simplePageBean.getSimplePageToolDao().findItemsBySakaiId("0")).thenReturn(l7);

        String path = simplePageBean.getSubPagePath(i5, false);
        Assert.assertEquals("Infinite > Lessons > SubPage 1", path);
        verify(simplePageBean.getSimplePageToolDao(), times(2)).findItemsBySakaiId("0");
    }

    @Test
    public void addBlti_addsEverySelectedLinkInProviderOrder() {
        LessonEntity entityProducer = mock(LessonEntity.class);
        LessonEntity first = mock(LessonEntity.class, withSettings().extraInterfaces(BltiInterface.class));
        LessonEntity second = mock(LessonEntity.class, withSettings().extraInterfaces(BltiInterface.class));
        when(entityProducer.getEntity("/blti/101")).thenReturn(first);
        when(entityProducer.getEntity("/blti/102")).thenReturn(second);
        when(first.getTitle()).thenReturn("First assignment");
        when(second.getTitle()).thenReturn("Second assignment");
        when(first.getDescription()).thenReturn("First description");
        when(second.getDescription()).thenReturn("Second description");
        when(((BltiInterface) first).frameSize()).thenReturn(0);
        when(((BltiInterface) second).frameSize()).thenReturn(0);

        SimplePageItem firstItem = new SimplePageItemImpl(101, 99, 1, SimplePageItem.BLTI, "/blti/101", "First assignment");
        SimplePageItem secondItem = new SimplePageItemImpl(102, 99, 2, SimplePageItem.BLTI, "/blti/102", "Second assignment");
        when(dao.findItemsOnPage(99L)).thenReturn(new ArrayList<>());
        when(dao.makeItem(99, 1, SimplePageItem.BLTI, "/blti/101", "First assignment")).thenReturn(firstItem);
        when(dao.makeItem(99, 2, SimplePageItem.BLTI, "/blti/102", "Second assignment")).thenReturn(secondItem);

        simplePageBean.setTaskService(mock(org.sakaiproject.tasks.api.TaskService.class));
        simplePageBean.setBltiEntity(entityProducer);
        simplePageBean.setItemId(-1L);
        simplePageBean.setSelectedBlti(new String[] { "/blti/101", "/blti/102" });

        Assert.assertEquals("success", simplePageBean.addBlti());

        InOrder order = inOrder(dao);
        order.verify(dao).makeItem(99, 1, SimplePageItem.BLTI, "/blti/101", "First assignment");
        order.verify(dao).makeItem(99, 2, SimplePageItem.BLTI, "/blti/102", "Second assignment");
        verify(dao).saveItemBatch(any(), any(), any(), anyString());
        Assert.assertEquals("First description", firstItem.getDescription());
        Assert.assertEquals("Second description", secondItem.getDescription());
        Assert.assertEquals(0, simplePageBean.selectedBlti.length);
    }

    @Test
    public void addBlti_rejectsMultipleLinksWhenEditingAnExistingItem() {
        simplePageBean.setBltiEntity(mock(LessonEntity.class));
        simplePageBean.setItemId(10L);
        simplePageBean.setSelectedBlti(new String[] { "/blti/101", "/blti/102" });

        Assert.assertEquals("failure", simplePageBean.addBlti());
        verify(dao, never()).makeItem(anyLong(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    public void addBlti_keepsProviderOrderWhenInsertingAfterAnItem() {
        LessonEntity entityProducer = mock(LessonEntity.class);
        LessonEntity first = mock(LessonEntity.class, withSettings().extraInterfaces(BltiInterface.class));
        LessonEntity second = mock(LessonEntity.class, withSettings().extraInterfaces(BltiInterface.class));
        when(entityProducer.getEntity("/blti/101")).thenReturn(first);
        when(entityProducer.getEntity("/blti/102")).thenReturn(second);
        when(first.getTitle()).thenReturn("First assignment");
        when(second.getTitle()).thenReturn("Second assignment");
        when(((BltiInterface) first).frameSize()).thenReturn(0);
        when(((BltiInterface) second).frameSize()).thenReturn(0);

        SimplePageItem anchor = new SimplePageItemImpl(50, 99, 5, SimplePageItem.TEXT, "", "Anchor");
        SimplePageItem firstItem = new SimplePageItemImpl(101, 99, 6, SimplePageItem.BLTI, "/blti/101", "First assignment");
        SimplePageItem secondItem = new SimplePageItemImpl(102, 99, 7, SimplePageItem.BLTI, "/blti/102", "Second assignment");
        when(dao.findItemsOnPage(99L)).thenReturn(new ArrayList<>(List.of(anchor)));
        when(dao.makeItem(99, 6, SimplePageItem.BLTI, "/blti/101", "First assignment")).thenReturn(firstItem);
        when(dao.makeItem(99, 7, SimplePageItem.BLTI, "/blti/102", "Second assignment")).thenReturn(secondItem);

        simplePageBean.setBltiEntity(entityProducer);
        simplePageBean.setItemId(-1L);
        simplePageBean.setAddBefore("-50");
        simplePageBean.setSelectedBlti(new String[] { "/blti/101", "/blti/102" });

        Assert.assertEquals("success", simplePageBean.addBlti());

        InOrder order = inOrder(dao);
        order.verify(dao).makeItem(99, 6, SimplePageItem.BLTI, "/blti/101", "First assignment");
        order.verify(dao).makeItem(99, 7, SimplePageItem.BLTI, "/blti/102", "Second assignment");
        verify(dao).saveItemBatch(any(), any(), any(), anyString());
    }
}

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

package org.sakaiproject.condition.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.condition.api.model.ConditionType;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConditionTestConfiguration.class)
public class ConditionServiceTests extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired private ConditionService service;
    @Autowired private SessionManager sessions;
    @Autowired private SecurityService security;
    @Autowired private SiteService sites;
    @Autowired private SessionFactory sessionFactory;

    @Before
    public void setup() {
        reset(sessions, security, sites);
        when(sessions.getCurrentSessionUserId()).thenReturn("editor-a");
        when(sites.siteReference("site-a")).thenReturn("/site/site-a");
        when(sites.siteReference("site-b")).thenReturn("/site/site-b");
        when(sites.allowAccessSite("site-a")).thenReturn(true);
        when(security.unlock(ConditionService.PERMISSION_UPDATE_CONDITION, "/site/site-a")).thenReturn(true);
    }

    private Condition condition(String siteId, String item) {
        Condition condition = new Condition();
        condition.setSiteId(siteId);
        condition.setToolId(ConditionService.CONDITIONS_TOOL_ID);
        condition.setItemId(item);
        condition.setType(ConditionType.ROOT);
        return condition;
    }

    @Test
    public void editorCanCreateUpdateAndDeleteOwnSitesCondition() {
        Condition saved = service.saveConditionForSite("site-a", condition("site-a", "own"));
        assertNotNull(saved.getId());
        saved.setArgument("updated");
        service.saveConditionForSite("site-a", saved);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertEquals("updated", service.getConditionForSite("site-a", saved.getId()).getArgument());
        assertTrue(service.deleteConditionForSite("site-a", saved.getId()));
        assertNull(service.getCondition(saved.getId()));
    }

    @Test
    public void editorCannotUpdateOrDeleteAnotherSitesCondition() {
        Condition foreign = service.saveCondition(condition("site-b", "foreign"));
        String id = foreign.getId();
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        foreign = service.getCondition(id).toBuilder().siteId("site-a").argument("forged").build();
        Condition forged = foreign;
        assertThrows(SecurityException.class, () -> service.saveConditionForSite("site-a", forged));
        assertThrows(SecurityException.class, () -> service.deleteConditionForSite("site-a", id));
        assertThrows(SecurityException.class, () -> service.getConditionForSite("site-a", id));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertEquals("site-b", service.getCondition(id).getSiteId());
        assertNull(service.getCondition(id).getArgument());
    }

    @Test
    public void foreignConditionCannotBeAttachedAsChildOrCreatedUnderAnotherSite() {
        Condition foreign = condition("site-b", "foreign");
        assertThrows(SecurityException.class, () -> service.saveConditionForSite("site-a", foreign));
        Condition parent = condition("site-a", "parent");
        parent.setSubConditions(Set.of(foreign));
        assertThrows(SecurityException.class, () -> service.saveConditionForSite("site-a", parent));
        assertTrue(service.getConditionsForSite("site-a").isEmpty());
        assertTrue(service.getConditionsForSite("site-b").isEmpty());
    }

    @Test
    public void legacyCrossSiteParentCannotCascadeDeleteForeignData() {
        Condition own = condition("site-a", "legacy-own");
        Condition foreignParent = condition("site-b", "legacy-parent");
        foreignParent.setSubConditions(Set.of(own));
        Condition savedParent = service.saveCondition(foreignParent);
        sessionFactory.getCurrentSession().flush();
        String ownId = savedParent.getSubConditions().iterator().next().getId();
        String parentId = savedParent.getId();
        sessionFactory.getCurrentSession().clear();
        assertThrows(SecurityException.class, () -> service.deleteConditionForSite("site-a", ownId));
        assertNotNull(service.getCondition(parentId));
        assertNotNull(service.getCondition(ownId));
    }

    @Test
    public void legacyCrossSiteChildCannotCascadeDeleteForeignData() {
        Condition own = condition("site-a", "legacy-own-child");
        own.setSubConditions(Set.of(condition("site-b", "foreign-child")));
        Condition saved = service.saveCondition(own);
        sessionFactory.getCurrentSession().flush();
        String foreignId = saved.getSubConditions().iterator().next().getId();
        String ownId = saved.getId();
        sessionFactory.getCurrentSession().clear();
        assertThrows(SecurityException.class, () -> service.deleteConditionForSite("site-a", ownId));
        assertNotNull(service.getCondition(ownId));
        assertNotNull(service.getCondition(foreignId));
    }

    @Test
    public void conditionEditorDoesNotNeedAnAdditionalSiteVisitGrant() {
        when(sites.allowAccessSite("site-a")).thenReturn(false);
        Condition saved = service.saveConditionForSite("site-a", condition("site-a", "scoped-editor"));
        assertEquals(saved.getId(), service.getConditionForSite("site-a", saved.getId()).getId());
    }

    @Test
    public void anonymousAndNonEditorCannotWriteConditions() {
        when(sessions.getCurrentSessionUserId()).thenReturn(null);
        assertThrows(SecurityException.class, () -> service.saveConditionForSite("site-a", condition("site-a", "anon")));
        when(sessions.getCurrentSessionUserId()).thenReturn("reader");
        when(security.unlock(ConditionService.PERMISSION_UPDATE_CONDITION, "/site/site-a")).thenReturn(false);
        assertThrows(SecurityException.class, () -> service.saveConditionForSite("site-a", condition("site-a", "reader")));
    }
}

/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
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

package org.sakaiproject.rubrics.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.rubrics.impl.RubricsContentProducer;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RubricsTestConfiguration.class})
public class RubricsContentProducerTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private RubricsContentProducer contentProducer;
    @Autowired private RubricRepository rubricRepository;
    @Autowired private RubricsService rubricsService;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;

    private final String siteId = "playpen";
    private final String siteRef = "/site/" + siteId;

    @Before
    public void setup() throws Exception {
        reset(securityService);
        when(siteService.siteReference(siteId)).thenReturn(siteRef);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        User instructor = mock(User.class);
        when(instructor.getId()).thenReturn("instructor");
        when(instructor.getDisplayName()).thenReturn("Instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(instructor);
        when(userDirectoryService.getUser("instructor")).thenReturn(instructor);
        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteRef)).thenReturn(true);
    }

    @Test
    public void referencesIdentifyTheSiteAndRubric() {
        String reference = RubricsService.rubricReference(siteId, 42L);

        assertEquals("/rubrics/playpen/42", reference);
        assertTrue(contentProducer.matches(reference));
        assertEquals("42", contentProducer.getId(reference));
        assertEquals(siteId, contentProducer.getSiteId(reference));
        assertEquals(siteId, contentProducer.getContainer(reference));
        assertEquals("rubrics", contentProducer.getTool());
        assertEquals("rubric", contentProducer.getType(reference));

        assertFalse(contentProducer.matches("/rubrics/playpen"));
        assertFalse(contentProducer.matches("/rubrics/playpen/not-a-number"));
        assertFalse(contentProducer.matches("/rubrics/playpen/42/extra"));
        assertFalse(contentProducer.matches("/assignment/a/playpen/42"));
        assertFalse(contentProducer.matches((String) null));
    }

    @Test
    public void rubricEventsMapToIndexActions() {
        assertEquals(SearchBuilderItem.ACTION_ADD, contentProducer.getAction(event(RubricsConstants.EVENT_RUBRIC_CREATED)));
        assertEquals(SearchBuilderItem.ACTION_ADD, contentProducer.getAction(event(RubricsConstants.EVENT_RUBRIC_UPDATED)));
        assertEquals(SearchBuilderItem.ACTION_DELETE, contentProducer.getAction(event(RubricsConstants.EVENT_RUBRIC_DELETED)));
        assertEquals(SearchBuilderItem.ACTION_UNKNOWN, contentProducer.getAction(event("rubrics.evaluation.saved")));
        assertTrue(contentProducer.matches(event(RubricsConstants.EVENT_RUBRIC_UPDATED)));
        assertFalse(contentProducer.matches(event("asn.new.assignment")));
    }

    @Test
    public void contentHoldsTheTitleCriteriaAndRatings() {
        RubricTransferBean rubric = rubricsService.createDefaultRubric(siteId);
        CriterionTransferBean criterion = rubric.getCriteria().get(0);
        criterion.setTitle("Use of evidence");
        criterion.setDescription("<p>Cites <b>primary</b> sources</p>");
        rubricsService.updateCriterion(criterion, siteId);
        String reference = RubricsService.rubricReference(siteId, rubric.getId());

        // the indexer strips markup from whatever the producer returns
        String content = SearchUtils.stripHtml(contentProducer.getContent(reference));

        assertEquals(rubric.getTitle(), contentProducer.getTitle(reference));
        assertTrue(content, content.startsWith(rubric.getTitle()));
        assertTrue(content, content.contains("Use of evidence Cites primary sources"));
        assertTrue(content, content.contains(rubric.getCriteria().get(1).getTitle()));
        rubric.getCriteria().get(1).getRatings().forEach(rating -> assertTrue(content, content.contains(rating.getTitle())));
        assertFalse(content, content.contains("<"));
    }

    @Test
    public void onlyListedRubricsOfTheReferencedSiteHaveContent() {
        RubricTransferBean listed = rubricsService.createDefaultRubric(siteId);
        Rubric adhoc = rubricRepository.findById(rubricsService.createDefaultRubric(siteId).getId()).get();
        adhoc.setAdhoc(true);
        rubricRepository.save(adhoc);

        // the indexer drops documents without content
        assertFalse(contentProducer.getContent(RubricsService.rubricReference(siteId, listed.getId())).isEmpty());
        for (String unindexed : List.of(
                RubricsService.rubricReference(siteId, adhoc.getId()),
                RubricsService.rubricReference("othersite", listed.getId()),
                RubricsService.rubricReference(siteId, 999999L))) {
            assertEquals(unindexed, "", contentProducer.getContent(unindexed));
            assertEquals(unindexed, "", contentProducer.getTitle(unindexed));
        }

        List<String> siteContent = new ArrayList<>();
        contentProducer.getSiteContentIterator(siteId).forEachRemaining(siteContent::add);
        assertEquals(List.of(RubricsService.rubricReference(siteId, listed.getId())), siteContent);
    }

    @Test
    public void onlyRubricsEditorsOfTheOwningSiteCanReadResults() {
        String reference = RubricsService.rubricReference(siteId, 1L);
        assertTrue(contentProducer.canRead(reference));

        when(securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteRef)).thenReturn(false);
        assertFalse(contentProducer.canRead(reference));
        assertFalse(contentProducer.canRead("/rubrics/bogus"));
    }

    @Test
    public void urlOpensTheSiteRubricsTool() throws Exception {
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(tool.getId()).thenReturn("rubrics-placement");
        Site site = mock(Site.class);
        when(site.getToolForCommonId(RubricsConstants.RBCS_TOOL)).thenReturn(tool);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(serverConfigurationService.getPortalUrl()).thenReturn("http://localhost:8080/portal");

        assertEquals("http://localhost:8080/portal/site/playpen/tool/rubrics-placement",
                contentProducer.getUrl(RubricsService.rubricReference(siteId, 1L)));
    }

    private static Event event(String name) {
        Event event = mock(Event.class);
        when(event.getEvent()).thenReturn(name);
        return event;
    }
}

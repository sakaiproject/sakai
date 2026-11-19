/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.model.ConversationsComment;
import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.model.Metadata;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConversationsTestConfiguration.class)
public class EntityContentProducerTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    @Qualifier("org.sakaiproject.conversations.impl.ConversationsEntityContentProducerImpl")
    private EntityContentProducer producer;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private SiteService siteService;

    private Site site;
    private List<String> references;

    @Before
    public void setup() {
        site = mock(Site.class);
        when(siteService.getOptionalSite("site1")).thenReturn(Optional.of(site));
        when(site.getToolForCommonId(ConversationsService.TOOL_ID)).thenReturn(mock(ToolConfiguration.class));

        Metadata metadata = new Metadata();
        metadata.setCreated(Instant.now());
        ConversationsTopic topic = new ConversationsTopic();
        topic.setMetadata(metadata);
        topic.setSiteId("site1");
        topic.setTitle("Topic");
        sessionFactory.getCurrentSession().persist(topic);
        ConversationsPost post = new ConversationsPost();
        post.setMetadata(metadata);
        post.setSiteId("site1");
        post.setTopic(topic);
        post.setMessage("Post");
        sessionFactory.getCurrentSession().persist(post);
        ConversationsComment comment = new ConversationsComment();
        comment.setMetadata(metadata);
        comment.setSiteId("site1");
        comment.setTopic(topic);
        comment.setPost(post);
        comment.setMessage("Comment");
        sessionFactory.getCurrentSession().persist(comment);
        sessionFactory.getCurrentSession().flush();
        references = List.of("/conversations/site1/t/" + topic.getId(),
            "/conversations/site1/p/" + post.getId(), "/conversations/site1/c/" + comment.getId());
    }

    @Test
    public void indexesContentOnlyWhileToolIsPresent() {
        List<String> actual = new ArrayList<>();
        producer.getSiteContentIterator("site1").forEachRemaining(actual::add);
        assertEquals(references, actual);

        when(site.getToolForCommonId(ConversationsService.TOOL_ID)).thenReturn(null);
        assertFalse(producer.getSiteContentIterator("site1").hasNext());

        when(site.getToolForCommonId(ConversationsService.TOOL_ID)).thenReturn(mock(ToolConfiguration.class));
        actual.clear();
        producer.getSiteContentIterator("site1").forEachRemaining(actual::add);
        assertEquals(references, actual);
    }

    @Test
    public void skipsContentWhenSiteIsMissing() {
        when(siteService.getOptionalSite("site1")).thenReturn(Optional.empty());
        assertFalse(producer.getSiteContentIterator("site1").hasNext());
    }
}

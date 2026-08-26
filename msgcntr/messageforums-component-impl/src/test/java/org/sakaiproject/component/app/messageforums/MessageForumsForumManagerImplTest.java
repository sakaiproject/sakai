/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MsgcntrTestConfiguration.class})
public class MessageForumsForumManagerImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    @Qualifier("org.sakaiproject.api.app.messageforums.MessageForumsForumManager")
    private MessageForumsForumManager forumManager;

    @Test
    public void getFaqForumForAreaReturnsNullWhenNoFaqForumExists() {
        Area area = new AreaImpl();
        area.setTypeUuid("discussionForum");
        area.setContextId("site-without-faq-forum");

        Assert.assertNull(forumManager.getFaqForumForArea(area));
    }
}

/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.mailarchive.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.site.api.SiteService;

@RunWith(MockitoJUnitRunner.class)
public class BaseMailArchiveServiceHardDeleteTest
{
        private static final String SITE_ID = "mail-hard-delete";

        private TestMailArchiveService service;

        @Mock private AliasService aliasService;
        @Mock private SiteService siteService;
        @Mock private MailArchiveChannel mailArchiveChannel;
        @Mock private MessageChannelEdit channelEdit;
        @Mock private Message message;

        @Before
        public void setUp()
        {
                service = new TestMailArchiveService();
                service.setAliasService(aliasService);
                service.setSiteService(siteService);

                String channelRef = service.channelReference(SITE_ID, SiteService.MAIN_CONTAINER);
                service.setChannel(channelRef, mailArchiveChannel);
                service.setChannelEdit(channelRef, channelEdit);

                when(message.getId()).thenReturn("message-id");
                when(mailArchiveChannel.getMessages(null, true, null)).thenReturn(Collections.singletonList(message));
        }

        @Test
        public void hardDeleteRemovesMailArchiveChannelAndAlias() throws Exception
        {
                service.hardDelete(SITE_ID);

                verify(mailArchiveChannel).removeMessage("message-id");
                assertTrue(service.getRemovedEdits().contains(channelEdit));
                verify(aliasService).removeTargetAliases(service.channelReference(SITE_ID, SiteService.MAIN_CONTAINER));
        }

        private static class TestMailArchiveService extends BaseMailArchiveService
        {
                private final Map<String, MailArchiveChannel> channels = new HashMap<>();
                private final Map<String, MessageChannelEdit> edits = new HashMap<>();
                private final List<MessageChannelEdit> removed = new ArrayList<>();

                void setChannel(String reference, MailArchiveChannel channel)
                {
                        channels.put(reference, channel);
                }

                void setChannelEdit(String reference, MessageChannelEdit edit)
                {
                        edits.put(reference, edit);
                }

                List<MessageChannelEdit> getRemovedEdits()
                {
                        return removed;
                }

                @Override
                protected Storage newStorage()
                {
                        return null;
                }

                @Override
                public void init()
                {
                        // No-op for tests
                }

                @Override
                public MailArchiveChannel getMailArchiveChannel(String ref) throws IdUnusedException, PermissionException
                {
                        MailArchiveChannel channel = channels.get(ref);
                        if (channel == null)
                        {
                                throw new IdUnusedException(ref);
                        }
                        return channel;
                }

                @Override
                public MessageChannelEdit editChannel(String ref)
                                throws IdUnusedException, PermissionException, InUseException
                {
                        MessageChannelEdit edit = edits.get(ref);
                        if (edit == null)
                        {
                                throw new IdUnusedException(ref);
                        }
                        return edit;
                }

                @Override
                protected void removeChannel(MessageChannelEdit edit)
                {
                        removed.add(edit);
                }
        }
}

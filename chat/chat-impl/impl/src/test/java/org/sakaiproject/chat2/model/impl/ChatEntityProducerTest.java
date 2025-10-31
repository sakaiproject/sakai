package org.sakaiproject.chat2.model.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;

public class ChatEntityProducerTest {

    private static final String SITE_ID = "site-id";

    private ChatEntityProducer chatEntityProducer;
    private ChatManager chatManager;

    @Before
    public void setUp() {
        chatManager = mock(ChatManager.class);
        chatEntityProducer = new ChatEntityProducer();
        chatEntityProducer.setChatManager(chatManager);
    }

    @Test
    public void hardDeleteRemovesAllChannelsForSite() {
        ChatChannel channelOne = new ChatChannel();
        channelOne.setId("channel-1");
        ChatChannel channelTwo = new ChatChannel();
        channelTwo.setId("channel-2");
        List<ChatChannel> channels = Arrays.asList(channelOne, channelTwo);

        when(chatManager.getContextChannels(SITE_ID, true)).thenReturn(channels);

        chatEntityProducer.hardDelete(SITE_ID);

        verify(chatManager).getContextChannels(SITE_ID, true);
        verify(chatManager).deleteChannel(channelOne);
        verify(chatManager).deleteChannel(channelTwo);
        verifyNoMoreInteractions(chatManager);
    }

    @Test
    public void hardDeleteWithEmptyChannelList() {
        when(chatManager.getContextChannels(SITE_ID, true)).thenReturn(Collections.emptyList());

        chatEntityProducer.hardDelete(SITE_ID);

        verify(chatManager).getContextChannels(SITE_ID, true);
        verifyNoMoreInteractions(chatManager);
    }
}

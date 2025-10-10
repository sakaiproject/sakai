/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.poll.logic.test;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.service.impl.PollListManagerImpl;
import org.sakaiproject.poll.repository.OptionRepository;
import org.sakaiproject.poll.repository.PollRepository;
import org.sakaiproject.poll.repository.VoteRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PollListManagerImplTest {

    /**
     * This is to test a specific bug where the wrong sites were being searched when asking for just one site.
     */
    @Test
    public void testGetPollForUserCorrectSites() {
        ExternalLogic externalLogic = Mockito.mock(ExternalLogic.class);
        PollRepository pollRepository = Mockito.mock(PollRepository.class);
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        VoteRepository voteRepository = Mockito.mock(VoteRepository.class);

        PollListManagerImpl impl = new PollListManagerImpl();
        impl.setExternalLogic(externalLogic);
        impl.setPollRepository(pollRepository);
        impl.setOptionRepository(optionRepository);
        impl.setVoteRepository(voteRepository);

        // User can see 3 sites.
        List<String> userSites = new ArrayList<>(Arrays.asList(new String[]{"site1", "site2", "site3"}));
        Mockito.when(externalLogic.getSitesForUser("userId", PollListManager.PERMISSION_VOTE)).thenReturn(userSites);
        // Find the polls in just one site.
        impl.findAllPollsForUserAndSitesAndPermission("userId", new String[]{"site3"}, PollListManager.PERMISSION_VOTE);
        ArgumentCaptor<List<String>> siteCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(pollRepository).findOpenPollsBySiteIds(siteCaptor.capture(), Mockito.any());
        Assert.assertEquals(List.of("site3"), siteCaptor.getValue());
    }
}

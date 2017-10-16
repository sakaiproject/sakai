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
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.poll.dao.PollDao;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.service.impl.PollListManagerImpl;

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
        PollDao dao = Mockito.mock(PollDao.class);

        PollListManagerImpl impl = new PollListManagerImpl();
        impl.setExternalLogic(externalLogic);
        impl.setDao(dao);

        // User can see 3 sites.
        List<String> userSites = new ArrayList<>(Arrays.asList(new String[]{"site1", "site2", "site3"}));
        Mockito.when(externalLogic.getSitesForUser("userId", PollListManager.PERMISSION_VOTE)).thenReturn(userSites);
        // Find the polls in just one site.
        impl.findAllPollsForUserAndSitesAndPermission("userId", new String[]{"site3"}, PollListManager.PERMISSION_VOTE);
        ArgumentCaptor<Search> argument = ArgumentCaptor.forClass(Search.class);
        Mockito.verify(dao).findBySearch(Mockito.eq(Poll.class), argument.capture());
        // Check that the DAO search is just done against the site we asked for.
        Assert.assertArrayEquals(argument.getValue().getRestrictions()[0].getArrayValue(), new String[]{"site3"});
    }
}

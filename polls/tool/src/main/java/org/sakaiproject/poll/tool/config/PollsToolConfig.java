/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.config;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.tool.entityproviders.PollEntityProvider;
import org.sakaiproject.poll.tool.entityproviders.PollOptionEntityProvider;
import org.sakaiproject.poll.tool.entityproviders.PollVoteEntityProvider;
import org.sakaiproject.poll.tool.entityproviders.PollsEntityProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollsToolConfig {

    @Bean
    public PollsEntityProvider pollsEntityProvider(PollListManager pollListManager,
                                                   PollVoteManager pollVoteManager,
                                                   UsageSessionService usageSessionService,
                                                   UserDirectoryService userDirectoryService,
                                                   EntityProviderManager entityProviderManager,
                                                   DeveloperHelperService developerHelperService) {
        PollsEntityProvider provider = new PollsEntityProvider();
        provider.setPollListManager(pollListManager);
        provider.setPollVoteManager(pollVoteManager);
        provider.setUsageSessionService(usageSessionService);
        provider.setUserDirectoryService(userDirectoryService);
        provider.setEntityProviderManager(entityProviderManager);
        provider.setDeveloperHelperService(developerHelperService);
        return provider;
    }

    @Bean
    public PollEntityProvider pollEntityProvider(PollListManager pollListManager,
                                                 PollVoteManager pollVoteManager,
                                                 EntityProviderManager entityProviderManager,
                                                 DeveloperHelperService developerHelperService) {
        PollEntityProvider provider = new PollEntityProvider();
        provider.setPollListManager(pollListManager);
        provider.setPollVoteManager(pollVoteManager);
        provider.setEntityProviderManager(entityProviderManager);
        provider.setDeveloperHelperService(developerHelperService);
        return provider;
    }

    @Bean
    public PollOptionEntityProvider pollOptionEntityProvider(PollListManager pollListManager,
                                                             EntityProviderManager entityProviderManager,
                                                             DeveloperHelperService developerHelperService) {
        PollOptionEntityProvider provider = new PollOptionEntityProvider();
        provider.setPollListManager(pollListManager);
        provider.setEntityProviderManager(entityProviderManager);
        provider.setDeveloperHelperService(developerHelperService);
        return provider;
    }

    @Bean
    public PollVoteEntityProvider pollVoteEntityProvider(PollListManager pollListManager,
                                                         PollVoteManager pollVoteManager,
                                                         UsageSessionService usageSessionService,
                                                         UserDirectoryService userDirectoryService,
                                                         EntityProviderManager entityProviderManager,
                                                         DeveloperHelperService developerHelperService) {
        PollVoteEntityProvider provider = new PollVoteEntityProvider();
        provider.setPollListManager(pollListManager);
        provider.setPollVoteManager(pollVoteManager);
        provider.setUsageSessionService(usageSessionService);
        provider.setUserDirectoryService(userDirectoryService);
        provider.setEntityProviderManager(entityProviderManager);
        provider.setDeveloperHelperService(developerHelperService);
        return provider;
    }
}

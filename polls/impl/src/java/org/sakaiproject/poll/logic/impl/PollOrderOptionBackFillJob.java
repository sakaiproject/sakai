/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.poll.logic.impl;

import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

/**
 * This Quartz job is responsible for back-filling the 'order' attribute of all existing Poll options.
 * It will iterate over all sites in the system, ordered by creation date. It will then request any
 * existing Polls for the site, starting with the newest and working backwards, and back-fill the
 * 'order' attribute of each poll's options based on the option's original ID value. If the options
 * are not null, the job will abort to avoid re-setting already existing orders (in case the job is
 * accidentally run more than once).
 *
 * @author Brian Jones (bjones86@uwo.ca)
 */
@Slf4j
@DisallowConcurrentExecution
public class PollOrderOptionBackFillJob implements Job
{
    // APIs
    @Getter @Setter private SiteService     siteService;
    @Getter @Setter private SecurityService securityService;
    @Getter @Setter private PollListManager pollService;

    private static final SecurityAdvisor YES_MAN = (String userId, String function, String reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;

    /**
     * This is the method that is fired when the job is 'triggered'.
     *
     * @param jobExecutionContext - the context of the job execution
     * @throws JobExecutionException
     */
    @Override
    public void execute( JobExecutionContext jobExecutionContext ) throws JobExecutionException
    {
        log.info( "Attempting to back-fill all existing Poll option orders..." );
        int modifiedCount = 0;

        List<Poll> pollsForSite = pollService.findAllPolls();
        if( pollsForSite != null && !pollsForSite.isEmpty() )
        {
            // Iterate over the polls for the site...
            for( Poll poll : pollsForSite )
            {
                try
                {
                    // Iterate over Options in the poll...
                    securityService.pushAdvisor( YES_MAN );
                    List<Option> pollOptions = pollService.getOptionsForPoll( poll );
                    if( pollOptions != null && !pollOptions.isEmpty() )
                    {
                        // Check if any options have a null order
                        boolean hasNullOptionOrder = false;
                        for( Option option : pollOptions )
                        {
                            if( option.getOptionOrder() == null )
                            {
                                hasNullOptionOrder = true;
                                break;
                            }
                        }

                        // If any of the option's order is null, we need to back-fill them
                        if( hasNullOptionOrder )
                        {
                            log.info( "Poll ID {} has options with null order, processing...", poll.getPollId() );

                            // Order the list by ID
                            pollOptions.sort( Comparator.comparingLong( Option::getOptionId ) );

                            // Iterate over the list
                            for( int i = 0; i < pollOptions.size(); i++ )
                            {
                                // Add order based on ID
                                Option option = pollOptions.get( i );
                                option.setOptionOrder(i);
                                pollService.saveOption( option );
                                modifiedCount++;
                                log.info( "Option {} ---> new order == {}", option.getOptionId(), i );
                            }
                        }
                    }
                }
                catch( Exception ex )
                {
                    log.error( "Unexcepted exception", ex );
                }
                finally
                {
                    securityService.popAdvisor( YES_MAN );
                }
            }
        }

        log.info( "Processing finished, modified {} poll options", modifiedCount );
    }
}

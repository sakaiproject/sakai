/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.scorm.service.sakai.impl.mocks;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 *
 * @author bjones86
 */
public class MockSecurityService implements SecurityService
{

    @Override
    public String getUserEffectiveRole()
    {
        return "";
    }

    @Override
    public boolean hasAdvisors()
    {
        return false;
    }

    @Override
    public boolean isSuperUser()
    {
        return false;
    }

    @Override
    public boolean isSuperUser( String userId )
    {
        return false;
    }

    @Override
    public boolean isUserRoleSwapped()
    {
        return false;
    }

    @Override
    public SecurityAdvisor popAdvisor()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public SecurityAdvisor popAdvisor( SecurityAdvisor advisor )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void pushAdvisor( SecurityAdvisor advisor )
    {
    }

    @Override
    public boolean setUserEffectiveRole( String azGroupId, String role )
    {
        return false;
    }

    @Override
    public boolean unlock( String lock, String reference )
    {
        return false;
    }

    @Override
    public boolean unlock( User user, String lock, String reference )
    {
        return false;
    }

    @Override
    public boolean unlock( String userId, String lock, String reference )
    {
        return false;
    }

    @Override
    public boolean unlock( String userId, String lock, String reference, Collection<String> authzGroupIds )
    {
        return false;
    }

    @Override
    public List<User> unlockUsers( String lock, String reference )
    {
        return Collections.emptyList();
    }
}

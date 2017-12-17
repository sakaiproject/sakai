/**
 * $Id$
 * $URL$
 * HttpServletAccessProviderMock.java - entity-broker - Apr 6, 2008 12:18:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.mocks;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;


/**
 * Pretends to be an access servlet provider for things that use them,
 * will not throw any exceptions or do anything
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
@SuppressWarnings("deprecation")
public class HttpServletAccessProviderMock implements HttpServletAccessProvider {

    private String prefix = null;
    public HttpServletAccessProviderMock(String prefix) {
        this.prefix = prefix;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.access.HttpServletAccessProvider#handleAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.sakaiproject.entitybroker.EntityReference)
     */
    public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
        // Okey dokey, do nothing but say all is well
        try {
            res.getWriter().print(prefix + ": HttpServletAccessProviderMock");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        ((EntityHttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);
    }

}

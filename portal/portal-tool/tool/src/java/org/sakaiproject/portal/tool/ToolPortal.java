/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.portal.tool;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.util.*;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;

import static org.sakaiproject.component.cover.ComponentManager.get;
/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class ToolPortal extends ToolPortalServlet {
    public ToolPortal() {
        super(
                get(ActiveToolManager.class),
                get(ServerConfigurationService.class),
                get(SessionManager.class),
                get(SiteService.class)
        );
    }
}

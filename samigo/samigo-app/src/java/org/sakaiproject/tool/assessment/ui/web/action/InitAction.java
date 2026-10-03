/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.web.action;

import javax.servlet.http.HttpServlet;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer;

public class InitAction extends HttpServlet {

  private static final long serialVersionUID = 8101462284850616249L;

  @Override
  public void init() {
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    if (typeFacadeQueries != null) {
      typeFacadeQueries.setTypeFacadeMap();
      typeFacadeQueries.setFacadeItemTypes();
    }

    // Initialize and register the entity producer even when components are loaded lazily.
    ComponentManager.get(AssessmentEntityProducer.class);
  }
}

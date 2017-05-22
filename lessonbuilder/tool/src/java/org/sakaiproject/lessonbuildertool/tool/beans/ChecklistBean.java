/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David P. Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016, University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.beans;

import org.sakaiproject.lessonbuildertool.ChecklistItemStatus;
import org.sakaiproject.lessonbuildertool.ChecklistItemStatusImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.cover.SessionManager;

public class ChecklistBean {

    private SimplePageBean simplePageBean;
    private SimplePageToolDao simplePageToolDao;

    public String checklistId;
    public String checklistItemId;
    public String checklistItemDone;
    public String csrfToken;

    private String[] results;

    public boolean checkCsrf() {
	Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
	if (sessionToken != null && sessionToken.toString().equals(csrfToken)) {
	    return true;
	}
	else
	    return false;
    }

    public String[] getResults() {
	if(!checkCsrf()) {
	    return new String[]{"error"};
	}

        handleAjaxCall();
        return results;
    }

    public void handleAjaxCall() {
        String userId = simplePageBean.getCurrentUserId();

        ChecklistItemStatus previousStatus = simplePageToolDao.findChecklistItemStatus(Long.valueOf(checklistId), Long.valueOf(checklistItemId), userId);

        if(previousStatus != null) {
            previousStatus.setDone(Boolean.valueOf(checklistItemDone));
            if(simplePageToolDao.saveChecklistItemStatus(previousStatus)) {
                results = new String[]{"success"};
            } else {
                results = new String[]{"error"};
            }
        } else {
            ChecklistItemStatus newStatus = new ChecklistItemStatusImpl(Long.valueOf(checklistId), Long.valueOf(checklistItemId), userId, Boolean.valueOf(checklistItemDone));
            if(simplePageToolDao.saveChecklistItemStatus(newStatus)) {
                results = new String[]{"success"};
            } else {
                results = new String[]{"error"};
            }
        }
    }

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }

    public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
        this.simplePageToolDao = simplePageToolDao;
    }
}
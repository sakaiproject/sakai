/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

@Slf4j
public class DelegatedAccessAddToolToMyWorkspacesJob implements ScheduledInvocationCommand{
	@Getter @Setter
	private DelegatedAccessDao dao;
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter
	private SakaiProxy sakaiProxy;
	
	
	public void execute(String nodeId){
		log.info("DelegatedAccessAddToolToMyWorkspacesJob started");
		long startTime = System.currentTimeMillis();
		
		List<String> userIds = dao.getDelegatedAccessUsers();
		if(userIds != null){
			//convert userIds to workspace site ids by adding a ~ to the front
			List<String> userWorkspaceIds = new ArrayList<String>();
			for(String userId : userIds){
				if(!DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)
						&& !DelegatedAccessConstants.SITE_HIERARCHY_USER.equals(userId)){
					userWorkspaceIds.add("~" + userId);
				}
			}
			//find which site's already have the DA tool
			List<String> sitesWithDelegatedAccess = dao.getSitesWithDelegatedAccessTool(userWorkspaceIds.toArray(new String[userWorkspaceIds.size()]));
			//filter out the sites that already ahve the DA tool
			for(String siteId : sitesWithDelegatedAccess){
				userWorkspaceIds.remove(siteId);
			}
			//now go through the leftover sites and add the DA tool:
			//user has access but doesn't have the DA tool, we need to add it
			String currentUserId = sakaiProxy.getCurrentUserId();
			try{
				for(String siteId : userWorkspaceIds){
					//trick the session into thinking you are the user who's workspace this is for.  This way,
					//SiteService will create the workspace if its missing
					sakaiProxy.setSessionUserId(siteId.substring(1));
					
					Site workspace = sakaiProxy.getSiteById(siteId);
					if(workspace != null){
						SitePage page = workspace.addPage();
						page.addTool("sakai.delegatedaccess");
						//check if the workspace already has the become user tool, if not add it:
						ToolConfiguration tool = workspace.getToolForCommonId("sakai.su");
						if(tool == null && projectLogic.hasAllowBecomeUserPerm(siteId.substring(1))){
							SitePage suPage = workspace.addPage();
							suPage.addTool("sakai.su");
						}
						sakaiProxy.saveSite(workspace);
					}
				}
			}catch (Exception e) {
				log.error(e.getMessage(), e);
			}finally{
				sakaiProxy.setSessionUserId(currentUserId);
			}
		
		}
		projectLogic.updateAddDAMyworkspaceJobStatus("" + new Date().getTime());
		log.info("DelegatedAccessAddToolToMyWorkspacesJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
	}
}

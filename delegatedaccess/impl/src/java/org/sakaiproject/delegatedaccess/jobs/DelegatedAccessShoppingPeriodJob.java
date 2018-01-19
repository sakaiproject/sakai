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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessMutableTreeNode;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

/**
 * This is the job that will populate the shopping period access tree.  It should be ran every morning (sometime after midnight).  
 * This is used to open and close the shopping period for sites based on their open and close dates.
 * 
 * @author Bryan Holladay
 *
 */
@Slf4j
public class DelegatedAccessShoppingPeriodJob implements StatefulJob, ScheduledInvocationCommand {
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter	
	private SakaiProxy sakaiProxy;
	
	public void init() { }

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		execute("");
	}
	public void execute(String nodeId){
		if(nodeId != null){
			nodeId = nodeId.trim();
		}
		
		try{
			Map<String, String> errors = new HashMap<String, String>();
			log.info("DelegatedAccessShoppingPeriodJob started.  NodeId: " + nodeId);
			long startTime = System.currentTimeMillis();
			SecurityAdvisor advisor = sakaiProxy.addSiteUpdateSecurityAdvisor();
			DefaultMutableTreeNode treeNode = null;
			if(nodeId == null || "".equals(nodeId)){
				TreeModel treeModel = projectLogic.getEntireTreePlusUserPerms(DelegatedAccessConstants.SHOPPING_PERIOD_USER);
				if (treeModel != null && treeModel.getRoot() != null) {
					treeNode = (DefaultMutableTreeNode) treeModel.getRoot();
				}
			}else{
				NodeModel nodeModel = projectLogic.getNodeModel(nodeId, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
				treeNode = new DelegatedAccessMutableTreeNode();
				treeNode.setUserObject(nodeModel);
			}
			
			if(treeNode != null){
				projectLogic.updateShoppingPeriodSettings(treeNode);
			
				sakaiProxy.popSecurityAdvisor(advisor);
				log.info("DelegatedAccessShoppingPeriodJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
				if(errors.size() > 0){
					String warning = "The following sites had errors: \n\n";
					for(Entry entry : errors.entrySet()){
						warning += entry.getKey() + ": " + entry.getValue() + "\n";
					}
					log.warn(warning);
					sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", warning);
				}
			}
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			StringWriter sw = new StringWriter();
			sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", sw.toString());
		}
	}
}

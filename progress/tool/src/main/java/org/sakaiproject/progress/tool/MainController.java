/****************************************************************************** 
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.progress.tool;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.progress.api.ProgressService;
import org.sakaiproject.progress.api.IGradebookService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 *
 * The concept here is to only hand marshalling data to and from the templates.
 * All the business logic should exist in the service.
 * 
 */
@Slf4j
@Controller
public class MainController {

	@Resource(name = "org.sakaiproject.progress.impl.GradebookServiceImpl")
	private IGradebookService gradebookService;

//	@Resource(name = "org.sakaiproject.progress.api.ProgressService")
//	private ProgressService progressService;

	@Resource(name = "org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;
	
	/* Make ProgressModel class
	@Resource(name = "org.sakaiproject.progress.tool.ProgressModel")
	private ProgressModel progress;
	*/

	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String pageIndex(Model model) {
		String context = toolManager.getCurrentPlacement().getContext();
		
		gradebookService.setGradebook(context);
		List<User> users = gradebookService.getStudents(context);

		model.addAttribute("users", users);
		model.addAttribute("test", gradebookService.getId());
		return "index";
	}

}

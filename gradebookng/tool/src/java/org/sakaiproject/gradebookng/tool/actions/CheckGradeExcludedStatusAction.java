/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.actions;

import java.io.Serializable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;

public class CheckGradeExcludedStatusAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public CheckGradeExcludedStatusAction() {}

	private class GradeUpdateResponse implements ActionResponse {

		private boolean isExcluded;

		public GradeUpdateResponse(final boolean isExcluded) {
			this.isExcluded = isExcluded;
		}

		public String getStatus() {
			return "OK";
		}

		public String toJson() {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode result = mapper.createObjectNode();
			result.put("isExcluded", isExcluded);
			return result.toString();
		}
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		boolean isExcluded;
		final String assignmentId = params.has("assignmentId") ? params.get("assignmentId").asText() : null;
		final String studentUuid = params.has("studentId") ? params.get("studentId").asText() : null;
		if(assignmentId == null || studentUuid == null){
			isExcluded = false;
		} else {
			GradeDefinition studentGradeDef = businessService.getGradeForStudent(params.get("assignmentId").asLong(), studentUuid);
			if(studentGradeDef != null) {
				isExcluded = studentGradeDef.isGradeExcluded();
			} else {
				isExcluded = false;
			}
		}
		return new GradeUpdateResponse(isExcluded);
	}
}

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
/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

 Unless required by applicable law or agr eed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/
package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.author.RestoreAssessmentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class RestoreAssessmentsListener implements ActionListener {
    public void processAction(ActionEvent event) throws AbortProcessingException {
        try {
            log.debug("RestoreAssessmentsListener: processAction()");
            RestoreAssessmentsBean bean = (RestoreAssessmentsBean) ContextUtil.lookupBean("restoreAssessmentsBean");
            bean.init();
        }catch(Exception e){
            log.error("Failed to get archived published assessments.", e);
            throw new RuntimeException("failed to get archived published assessments");
        }
    }
}

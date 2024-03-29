/*
 * Copyright (c) 2024, The Apereo Foundation
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
 *
 */
package org.sakaiproject.tool.assessment.ui.listener.delivery;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
public class SebSetupActionListener implements ActionListener {


    public void processAction(ActionEvent actionEvent) {
        DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
        Boolean sebSetup = ContextUtil.lookupBooleanParam("sebSetup");

        if (sebSetup != null) {
            deliveryBean.setSebSetup(sebSetup);
        }
    }
}

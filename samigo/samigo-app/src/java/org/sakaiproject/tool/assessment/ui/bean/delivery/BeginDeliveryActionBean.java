/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.bean.delivery;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@ManagedBean(name="beginDeliveryActionBean")
@SessionScoped
public class BeginDeliveryActionBean {
    
    public String startAssessment() {
        DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
        
        if (delivery.getNavigation() != null && "1".equals(delivery.getNavigation().trim())) {
            LinearAccessDeliveryActionListener linearDeliveryListener = new LinearAccessDeliveryActionListener();
            linearDeliveryListener.processAction(null);
        }
        else {
            DeliveryActionListener deliveryListener = new DeliveryActionListener();
            deliveryListener.processAction(null);
        }
        
        return delivery.validate();
    }
} 
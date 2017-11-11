/**
 * Copyright (c) 2005-2008 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.rsf.bean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class BeginAssessmentDeliveryBean {

  public HttpServletRequest httpServletRequest;
  public HttpServletResponse httpServletResponse;
  
  public String startAssessment() {
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", httpServletRequest, httpServletResponse);
    
    if (delivery.getNavigation().trim() != null && "1".equals(delivery.getNavigation().trim())) {
      LinearAccessDeliveryActionListener linearDeliveryListener = new LinearAccessDeliveryActionListener();
      linearDeliveryListener.processAction(null);
    }
    else {
      DeliveryActionListener deliveryListener = new DeliveryActionListener();
      deliveryListener.processAction(null);
    }
    // can return a lot of things... but takeAssessment is the positive result
    return delivery.validate();
  }
  
}

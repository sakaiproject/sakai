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

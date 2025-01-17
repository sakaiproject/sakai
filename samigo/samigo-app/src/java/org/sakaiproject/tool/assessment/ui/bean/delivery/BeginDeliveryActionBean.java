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
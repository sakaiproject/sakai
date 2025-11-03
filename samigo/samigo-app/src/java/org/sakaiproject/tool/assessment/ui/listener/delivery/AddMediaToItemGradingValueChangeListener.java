package org.sakaiproject.tool.assessment.ui.listener.delivery;

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class AddMediaToItemGradingValueChangeListener implements ValueChangeListener {
    @Override
    public void processValueChange(ValueChangeEvent event) {
        org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean delivery =
                (org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean) ContextUtil.lookupBean("delivery");
        delivery.addMediaToItemGrading(event);
    }
}


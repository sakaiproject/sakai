
package org.sakaiproject.tool.assessment.ui.listener.shared;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.shared.MediaService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.MediaBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemoveMediaListener implements ActionListener
{
  private static Log log = LogFactory.getLog(RemoveMediaListener.class);
  private static ContextUtil cu;

  public RemoveMediaListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    MediaBean mediaBean = (MediaBean) cu.lookupBean(
        "mediaBean");
    MediaService mediaService = new MediaService();

    // #0. check if we need to pause time 
    if (delivery.isTimeRunning() && delivery.timeExpired()){
      delivery.setOutcome("timeExpired");
    }
    else{
      delivery.syncTimeElapsedWithServer();
      delivery.setOutcome("takeAssessment");
    }

    // #1. get all the info need from bean
    String mediaId = mediaBean.getMediaId();
    mediaService.remove(mediaId);

    // #2. update time based on server
    if (delivery.isTimeRunning() && delivery.timeExpired()){
      delivery.setOutcome("timeExpired");
    }
    else{
      delivery.syncTimeElapsedWithServer();
      delivery.setTimeElapseAfterFileUpload(delivery.getTimeElapse());
      delivery.setOutcome("takeAssessment");
    }
  }

}

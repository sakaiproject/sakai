/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.shared;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.shared.MediaService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.MediaBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemoveMediaListener implements ActionListener
{
  //private static Log log = LogFactory.getLog(RemoveMediaListener.class);

  public RemoveMediaListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    MediaBean mediaBean = (MediaBean) ContextUtil.lookupBean(
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
    Long itemGradingId = mediaBean.getItemGradingId();
    mediaService.remove(mediaId, itemGradingId);

    // #2. update time based on server
    if (delivery.isTimeRunning() && delivery.timeExpired()){
      delivery.setOutcome("timeExpired");
    }
    else{
      delivery.syncTimeElapsedWithServer();
      delivery.setTimeElapseAfterFileUpload(delivery.getTimeElapse());
      delivery.setOutcome("takeAssessment");
    }

    // #1. do whatever need doing before returning to take assessment
    DeliveryActionListener dlistener = new DeliveryActionListener();
    // false => do not reset the entire current delivery.pageContents.
    // we will do it ourselves and only update the question that this media
    // is attached to
    dlistener.processAction(null, false);
  }

}

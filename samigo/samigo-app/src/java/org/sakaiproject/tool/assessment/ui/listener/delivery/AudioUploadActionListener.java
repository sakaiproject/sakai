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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  When student makes a recording for an audio question type
 * the audio recorder applet makes a copy of the local recording and posts it
 * to a special servlet,
 * @see org.sakaiproject.tool.assessment.ui.servlet.delivery.UploadAudioMediaServlet,
 *  that copies it to a designated file on the server.
 * When that student then posts the answer by pressing the Update button, the
 * actual grading record is made.
 * </p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

@Slf4j
 public class AudioUploadActionListener implements ActionListener
{
  /**
   * ACTION. add audio recording to item grading
   * @param ae the action event triggering the processAction method
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.info("AudioUploadActionListener.processAction() ");

    try {
      // get managed bean
      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");

      delivery.saveWork();


    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
  }
}

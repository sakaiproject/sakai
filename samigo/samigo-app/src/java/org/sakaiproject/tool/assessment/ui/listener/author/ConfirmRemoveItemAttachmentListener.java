/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/shared/ConfirmRemoveMediaListener.java $
 * $Id: ConfirmRemoveMediaListener.java 9268 2006-05-10 21:27:24Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.AttachmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: ConfirmRemoveAttachmentListener.java 9268 2006-05-10 21:27:24Z daisyf@stanford.edu $
 */

public class ConfirmRemoveAttachmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ConfirmRemoveAttachmentListener.class);
  private static ContextUtil cu;

  public ConfirmRemoveAttachmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    String attachmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("attachmentId");
    String attachmentLocation = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("attachmentLocation");
    String attachmentFilename = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("attachmentFilename");
    String attachmentType = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("attachmentType");

    AttachmentBean attachmentBean = (AttachmentBean) cu.lookupBean(
        "attachmentBean");
    attachmentBean.setAttachmentId(new Long(attachmentId));
    attachmentBean.setLocation(attachmentLocation);
    attachmentBean.setFilename(attachmentFilename);
    attachmentBean.setAttachmentType(new Long(attachmentType));
  }

}

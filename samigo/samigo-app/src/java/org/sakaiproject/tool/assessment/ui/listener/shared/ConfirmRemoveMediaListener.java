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

package org.sakaiproject.tool.assessment.ui.listener.shared;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.shared.MediaBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmRemoveMediaListener implements ActionListener
{

  public ConfirmRemoveMediaListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    String mediaId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaId");
    String mediaUrl = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaUrl");
    String mediaFilename = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaFilename");
    String itemGradingId = (String) FacesContext.getCurrentInstance().
    	getExternalContext().getRequestParameterMap().get("itemGradingId");

    MediaBean mediaBean = (MediaBean) ContextUtil.lookupBean(
        "mediaBean");
    mediaBean.setMediaId(mediaId);
    mediaBean.setMediaUrl(mediaUrl);
    mediaBean.setFilename(mediaFilename);
    mediaBean.setItemGradingId(Long.valueOf(itemGradingId));

  }
}

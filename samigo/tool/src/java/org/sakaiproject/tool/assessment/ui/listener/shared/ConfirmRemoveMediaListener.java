
package org.sakaiproject.tool.assessment.ui.listener.shared;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class ConfirmRemoveMediaListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ConfirmRemoveMediaListener.class);
  private static ContextUtil cu;

  public ConfirmRemoveMediaListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    String mediaId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaId");
    String mediaUrl = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaUrl");
    String mediaFilename = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("mediaFilename");

    MediaBean mediaBean = (MediaBean) cu.lookupBean(
        "mediaBean");
    mediaBean.setMediaId(mediaId);
    mediaBean.setMediaUrl(mediaUrl);
    mediaBean.setFilename(mediaFilename);
  }

}

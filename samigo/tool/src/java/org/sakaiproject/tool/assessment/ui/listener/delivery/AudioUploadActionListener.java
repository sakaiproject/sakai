/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2006 The Sakai Foundation.
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      https://source.sakaiproject.org/svn/sakai/trunk/sakai_license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import javax.faces.component.UIOutput;
import javax.faces.event.FacesEvent;

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
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AudioUploadActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(AudioUploadActionListener.class);
  private static ContextUtil cu;

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
      DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");

      // look for the correct file upload path information
      String audioMediaUploadPath = getAudioMediaUploadPath(ae);
      log.info("audioMediaUploadPath: " + audioMediaUploadPath);

      // now use utility method to fetch the file
      delivery.addMediaToItemGrading(audioMediaUploadPath);
      log.info("delivery.addMediaToItemGrading(audioMediaUploadPath)");


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Get audio media upload path from the event's component tree.
   * @param ae  the event
   * @return
   */
  private String getAudioMediaUploadPath(FacesEvent ae)
  {
    String audioMediaUploadPath = null;

    // now find what component fired the event
    UIComponent component = ae.getComponent();
    // get the subview containing the audio question
    UIComponent parent = component.getParent();

    // get the its peer components from the parent
    List peers = parent.getChildren();

    // look for the correct file upload path information
    // held in the value of the component 'audioMediaUploadPath'
    for (int i = 0; i < peers.size(); i++)
    {
      UIComponent peer = (UIComponent) peers.get(i);

      if ("audioMediaUploadPath".equals(peer.getId()) && peer instanceof UIOutput)
      {
        audioMediaUploadPath = "" + ((UIOutput) peer).getValue();
        log.info("FOUND: Component " + i +
                 " peer.getId(): " + peer.getId()+
                 " peer.getValue(): " + audioMediaUploadPath );
        break;
      }
    }

    return audioMediaUploadPath;
  }

}

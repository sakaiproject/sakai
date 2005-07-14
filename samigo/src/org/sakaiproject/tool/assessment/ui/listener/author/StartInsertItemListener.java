/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class StartInsertItemListener implements ValueChangeListener
{
    private static Log log = LogFactory.getLog(StartInsertItemListener.class);


  /**
   * Standard process action method.
   * @param ae ValueChangeEvent
   * @throws AbortProcessingException
   */
  public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
  {
    log.info("StartInsertItemListener valueChangeLISTENER.");
    log.debug("lydiatest BEGIN StartInsertItemListener processValueChange ------  ");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    String olditemtype = (String) ae.getOldValue();
    log.debug("lydiatest ae.getOldValue : " + olditemtype );
    String selectedvalue= (String) ae.getNewValue();
    log.debug("lydiatest ae.getNewValue : " + selectedvalue);
    String newitemtype = null;
    String insertItemPosition = null;
    String insertToSection = null;

    // only set itemtype when the value has indeed changed.
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      String[] strArray = selectedvalue.split(",");
    log.debug("lydiatest ae.getNewValue [0] : " + strArray[0]);
    log.debug("lydiatest ae.getNewValue [1] : " + strArray[1]);
    log.debug("lydiatest ae.getNewValue [2] : " + strArray[2]);
      newitemtype = strArray[0].trim();
      insertToSection = strArray[1].trim();
      insertItemPosition= strArray[2].trim();
      itemauthorbean.setItemType(newitemtype);
      itemauthorbean.setInsertToSection(insertToSection);
      itemauthorbean.setInsertPosition(insertItemPosition);
      itemauthorbean.setInsertType(newitemtype);
      itemauthorbean.setItemNo(String.valueOf(Integer.parseInt(insertItemPosition) +1));

    log.debug("lydiatest StartInsertItem item type " + itemauthorbean.getItemType());
    log.debug("lydiatest STartInsertItem param insertItemPosition " + insertItemPosition);
    log.debug("lydiatest STartInsertItem param insert to Section " + insertToSection);


    StartCreateItemListener listener = new StartCreateItemListener();

    if (!listener.startCreateItem(itemauthorbean))
    {
      throw new RuntimeException("failed to startCreatItem.");
    }


    }

  }


}

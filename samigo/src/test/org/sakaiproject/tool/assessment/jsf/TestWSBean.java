/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package test.org.sakaiproject.tool.assessment.jsf;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


/**
 * <p>Description: Test Bean with some properties</p>
 */

public class TestWSBean
  implements Serializable
{
  private String itemid;
  private String itembankxml;

  public TestWSBean()
  {
    itemid = "itemid";
  }

  public String getItemid()
  {
    ItemContentsBean itemContentsBean = (ItemContentsBean) ContextUtil.lookupBean("itemContents");
    String itemId = (String)  FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("itemid");
     System.out.println("Getting Item Id=  "+ itemId);
    if (itemId != null && !itemId.equals(""))
    {
     ItemService itemService = new ItemService();
     ItemFacade item = itemService.getItem(itemId);
     itemContentsBean.setItemData(item.getData());
    }
    return itemid;
  }

  public void setItemid(String p)
  {
    itemid= p;
  }

  public void setItembankxml(String p)
  {
    itembankxml= p;
  }


}

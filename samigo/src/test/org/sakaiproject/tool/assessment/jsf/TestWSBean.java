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

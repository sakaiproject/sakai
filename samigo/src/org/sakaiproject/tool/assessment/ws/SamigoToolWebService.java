/**
 * SamigoToolWebService.java
 *
 */

package org.sakaiproject.tool.assessment.ws;

import java.util.HashMap;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.util.XmlUtil;
import org.w3c.dom.Document;


public class SamigoToolWebService {

  /**
   * Creates a new SamigoToolWebService object.
   */
  public SamigoToolWebService()
  {
  }

  
  /**
   * Get an array of items from the backend, with all questions.
  */
  public Item[] getItemObjArrayByKeyword(String keyword)
  {
    ItemService itemservice = new ItemService();
    HashMap map= itemservice.getItemsByKeyword(keyword);
    Item[] itemArray = new Item[map.size()];
   
    // converting to Object Array for transmitting through Axis SOAP
    int i = 0;
    Iterator iter = map.keySet().iterator();
    while (iter.hasNext()) {
      String itemid = (String)iter.next();
      if (map.get(itemid)!=null){
        ItemFacade a = (ItemFacade) map.get(itemid);
        String itemtext = a.getText();
        String idstring = a.getItemIdString();
        Item item = new Item(); 
        item.setItemid(idstring);
        item.setItemtext(itemtext);
        item.setUrl(showItem(idstring));
        itemArray[i]= item;
        i++;
      }
    }

     return itemArray;

  }




  public java.lang.String showItem(java.lang.String itemid) {
    String ret = "jsf/test/previewQuestion.faces?itemid="+itemid;
    return ret;
    }


    public String download(String[] idStringArray, String qtiVersion)
    {

      //  move this to TestWSBean.getItembankxml 

      QTIService qtiservice= new QTIService();
        
      Document doc= qtiservice.getExportedItemBank(idStringArray,new Integer(qtiVersion).intValue());
      String xmlString = XmlUtil.getDOMString(doc);
      return xmlString;
    }



}

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

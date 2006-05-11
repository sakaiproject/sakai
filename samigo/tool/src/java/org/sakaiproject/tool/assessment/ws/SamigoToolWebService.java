/**********************************************************************************
 * $URL$
 * $Id$
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



/**
 * SamigoToolWebService.java
 *
 * This was part of the web services demo files  
 * This file used to be called by an Apache Axis generated 
 * SamigoToolServiceSoapBindingImpl.java
 * all the wsdl2java generated files are deleted due to Axis 1.1 imcompatible 
 * with jdk 1.5
 * Will regenerate them if we need to use this again. 
 * Keep this file in case we want to reuse any of the methods here. 
 */

package org.sakaiproject.tool.assessment.ws;

import java.util.HashMap;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
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

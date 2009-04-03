/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/ItemFacadeQueriesAPI.java $
 * $Id: ItemFacadeQueriesAPI.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface ItemFacadeQueriesAPI
{

  public IdImpl getItemId(String id);

  public IdImpl getItemId(Long id);

  public IdImpl getItemId(long id);

  public Long add();

  public List getQPItems(Long questionPoolId);

  public List list();

  public void show(Long itemId);

  public ItemFacade getItem(Long itemId, String agent);

  public void showType(Long typeId);

  public void listType();

  // DELETEME
  public void remove(Long itemId);

  public void deleteItem(Long itemId, String agent);

  public void deleteItemContent(Long itemId, String agent);

  public void deleteItemMetaData(Long itemId, String label);

  public void addItemMetaData(Long itemId, String label, String value);

  public Long facadeAdd() throws DataFacadeException;

  public void ifcShow(Long itemId);

  public ItemFacade saveItem(ItemFacade item) throws DataFacadeException;

  /**
   private void exportXml(ItemDataIfc item) {
   XStream xstream = new XStream();
   xstream = new XStream(new DomDriver());
   xstream.alias("item", ItemData.class);
   xstream.alias("itemText", ItemText.class);
   xstream.alias("itemFeedback", ItemFeedback.class);
   xstream.alias("itemMetaData", ItemMetaData.class);
   xstream.alias("answer", Answer.class);
   xstream.alias("answerFeedback", AnswerFeedback.class);
   String xml = xstream.toXML(item);
   byte[] b = xml.getBytes();
   try {
   FileOutputStream out = new FileOutputStream("out");
   out.write(b);
   }
   catch (FileNotFoundException ex) {
   }
   catch (IOException ex1) {
   }
   }
   */

  public ItemFacade getItem(Long itemId);

  public HashMap getItemsByKeyword(String keyword);

  public Long getItemTextId(Long publishedItemId);
  
  public void deleteSet(Set s);
}

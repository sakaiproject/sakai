/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/ItemFacadeQueries.java $
 * $Id: ItemFacadeQueries.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ItemFacadeQueries extends HibernateDaoSupport implements ItemFacadeQueriesAPI {
  private static Log log = LogFactory.getLog(ItemFacadeQueries.class);

  public ItemFacadeQueries() {
  }

  public IdImpl getItemId(String id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(Long id){
    return new IdImpl(id);
  }
  public IdImpl getItemId(long id){
    return new IdImpl(id);
  }

  /*
  public static void main(String[] args) throws DataFacadeException {
    ItemFacadeQueriesAPI instance = new ItemFacadeQueries();
    // add an item
    if (args[0].equals("add")) {
      Long itemId = instance.add();
      //log.debug("**Item #" + itemId);
      instance.show(itemId);
    }
    if (args[0].equals("f_add")) {
      Long itemId = new Long(-1);
      itemId = instance.facadeAdd();
      //log.debug("**Item #" + itemId);
      instance.ifcShow(itemId);
    }
    if (args[0].equals("show")) {
      instance.show(new Long(args[1]));
    }
    if (args[0].equals("showtype")) {
      instance.showType(new Long(args[1]));
    }
    if (args[0].equals("remove")) {
      instance.remove(new Long(args[1]));
    }
    if (args[0].equals("listtype")) {
      instance.listType();
    }
    if (args[0].equals("list")) {
      instance.list();
      List items = instance.list();
      for (int i = 0; i < items.size(); i++) {
        ItemData item = (ItemData) items.get(i);
        log.debug("Item #" + item.getItemId() + " has rationale= " +
                           item.getHasRationale());
      }
    }
    if (args[0].equals("getQPItems")) {
      List items = instance.getQPItems(new Long(args[1])); // poolId
      for (int i = 0; i < items.size(); i++) {
        ItemData item = (ItemData) items.get(i);
        log.debug("Item #" + item.getItemId() + " has rationale= " +
                           item.getHasRationale());
      }
    }
    System.exit(0);
  }
  */
  
  public Long add() {
    ItemData item = new ItemData();
    item.setInstruction("Matching game");
    item.setTypeId(TypeFacade.MATCHING);
    item.setScore( Float.valueOf(10));
    item.setDiscount(Float.valueOf(0));
    item.setHasRationale(Boolean.FALSE);
    item.setStatus(  Integer.valueOf(1));
    item.setCreatedBy("1");
    item.setCreatedDate(new Date());
    item.setLastModifiedBy("1");
    item.setLastModifiedDate(new Date());

    // prepare itemText
    item.setItemTextSet(prepareText(item));

    // prepare MetaData
    item.setItemMetaDataSet(prepareMetaData(item));
    item.addItemMetaData("ITEM_OBJECTIVE", "the objective is to ...");

    // prepare feedback
    item.setCorrectItemFeedback("well done!");
    item.setInCorrectItemFeedback("better luck next time!");

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving item: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    return item.getItemId();
  }

  public List getQPItems(final Long questionPoolId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, QuestionPoolItem qpi where qpi.itemId=ab.itemIdString and qpi.questionPoolId = ?");
	    		q.setLong(0, questionPoolId.longValue());
	    		return q.list();
	    	};
	    };
	    return getHibernateTemplate().executeFind(hcb);

//    return getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItem qpi where qpi.itemId=ab.itemIdString and qpi.questionPoolId = ?",
//    		new Object[] { questionPoolId }, new org.hibernate.type.Type[] { Hibernate.LONG });
  }

  public List list() {
    return getHibernateTemplate().find("from ItemData");
  }

  public void show(Long itemId) {
    getHibernateTemplate().load(ItemData.class, itemId);
  }

  public ItemFacade getItem(Long itemId, String agent) {
	ItemFacade item = new ItemFacade((ItemData)getHibernateTemplate().load(ItemData.class, itemId));
	return item;
  }

  public void showType(Long typeId) {
    getHibernateTemplate().load(TypeD.class, typeId);
  }

  public void listType() {
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    TypeFacade f = typeFacadeQueries.getTypeFacadeById(  Long.valueOf(1));
    log.debug("***facade: "+f.getAuthority());
  }

  public void remove(Long itemId) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);

    // get list of attachment in section
    AssessmentService service = new AssessmentService();
    List itemAttachmentList = service.getItemResourceIdList(item);
    service.deleteResources(itemAttachmentList);

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem deleting item : "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
      if (item != null) {
        printItem(item);
      }
  }

  public void deleteItem(Long itemId, String agent) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);
    // get list of attachment in item
    AssessmentService service = new AssessmentService();
    List itemAttachmentList = service.getItemResourceIdList(item);
    service.deleteResources(itemAttachmentList);

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
	SectionDataIfc section = item.getSection();
        // section might be null if you are deleting an item created inside a pool, that's not linked to any assessment. 
        if (section !=null) {
          Set set = section.getItemSet();
          set.remove(item);
        }
        getHibernateTemplate().delete(item);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem deleting item: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }



  // is this used by ItemAddListener to save item? -daisyf
  public void deleteItemContent(Long itemId, String agent) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemTextSet();
          item.setItemTextSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleteItemTextSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }

    retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemMetaDataSet();
          item.setItemMetaDataSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleteItemMetaDataSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }

    retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
          Set set = item.getItemFeedbackSet();
          item.setItemFeedbackSet(new HashSet());
          getHibernateTemplate().deleteAll(set);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem deleting ItemFeedbackSet: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

  public void deleteItemMetaData(final Long itemId, final String label) {
// delete metadata by label
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);
    final String query = "from ItemMetaData imd where imd.item.itemId=? and imd.label= ?";
    
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, itemId.longValue());
    		q.setString(1, label);
    		return q.list();
    	};
    };
    List itemmetadatalist = getHibernateTemplate().executeFind(hcb);

    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        if (item!=null){ // need to dissociate with item before deleting in Hibernate 3
	  Iterator iter = itemmetadatalist.iterator();
	  while (iter.hasNext()){
	    ItemMetaDataIfc meta= (ItemMetaDataIfc) iter.next();
            meta.setItem(null);
	  }
          
          Set set = item.getItemMetaDataSet();
          set.removeAll(itemmetadatalist);
          item.setItemMetaDataSet(set);
          getHibernateTemplate().deleteAll(itemmetadatalist);
          retryCount = 0;
	}
        else retryCount=0;
      }
      catch (Exception e) {
        log.warn("problem delete itemmetadatalist: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }


  public void addItemMetaData(Long itemId, String label, String value) {
    ItemData item = (ItemData)getHibernateTemplate().load(ItemData.class, itemId);
      if (item != null) {
        printItem(item);

    ItemMetaData itemmetadata = new ItemMetaData(item, label, value);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(itemmetadata);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem saving itemmetadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    //item.addItemMetaData(label, value);
    //getHibernateTemplate().saveOrUpdate(item);
      }
  }

  private HashSet prepareText(ItemData item) {
    HashSet textSet = new HashSet();
    ItemText text1 = new ItemText();
    text1.setItem(item);
    text1.setSequence(  Long.valueOf(1));
    text1.setText("cat has");
    HashSet answerSet1 = new HashSet();
    HashSet answerFeedbackSet1 = new HashSet();
    Answer answer1 = new Answer(text1, "2 legs", Long.valueOf(1), "i",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0));
    answerFeedbackSet1.add(new AnswerFeedback(answer1, "incorrect", "sorry"));
    answer1.setAnswerFeedbackSet(answerFeedbackSet1);
    answerSet1.add(answer1);
    answerSet1.add(new Answer(text1, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet1.add(new Answer(text1, "4 legs", Long.valueOf(3), "iii",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    text1.setAnswerSet(answerSet1);

    textSet.add(text1);

    ItemText text2 = new ItemText();
    text2.setItem(item);
    text2.setSequence( Long.valueOf(2));
    text2.setText("chicken has");
    HashSet answerSet2 = new HashSet();
    answerSet2.add(new Answer(text2, "2 legs", Long.valueOf(1), "i",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    answerSet2.add(new Answer(text2, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet2.add(new Answer(text2, "4 legs", Long.valueOf(3), "iii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    text2.setAnswerSet(answerSet2);
    textSet.add(text2);

    ItemText text3 = new ItemText();
    text3.setItem(item);
    text3.setSequence(Long.valueOf(3));
    text3.setText("baby has");
    HashSet answerSet3 = new HashSet();
    answerSet3.add(new Answer(text3, "2 legs", Long.valueOf(1), "i",
    		Boolean.FALSE, null,  Float.valueOf(0), Float.valueOf(0)));
    answerSet3.add(new Answer(text3, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet3.add(new Answer(text3, "4 legs", Long.valueOf(3), "iii",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    text3.setAnswerSet(answerSet3);
    textSet.add(text3);
    return textSet;
  }

  private HashSet prepareMetaData(ItemData item) {
    HashSet set = new HashSet();
    set.add(new ItemMetaData(item, "qmd_itemtype", "Matching"));
    set.add(new ItemMetaData(item, "TEXT_FORMAT", "HTML"));
    set.add(new ItemMetaData(item, "MUTUALLY_EXCLUSIVE", "True"));
    return set;
  }

  private void printItem(ItemData item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
  }
  
  public Long facadeAdd() throws DataFacadeException {
    ItemFacade item = new ItemFacade();
      item.setInstruction("Matching game");
      item.setTypeId(Long.valueOf(9));
      item.setScore( Float.valueOf(10));
      item.setDiscount(Float.valueOf(0));
      item.setHasRationale(Boolean.FALSE);
      item.setStatus( Integer.valueOf(1));
      item.setCreatedBy("1");
      item.setCreatedDate(new Date());
      item.setLastModifiedBy("1");
      item.setLastModifiedDate(new Date());

      // prepare itemText
      item.setItemTextSet(prepareText((ItemData)item.getData()));
      item.addItemText("I have",new HashSet());

      // prepare MetaData
      item.setItemMetaDataSet(prepareMetaData((ItemData)item.getData()));
      item.addItemMetaData("ITEM_OBJECTIVE", "the objective is to ...");

      // prepare feedback
      item.setCorrectItemFeedback("well done!");
      item.setInCorrectItemFeedback("better luck next time!");

      getHibernateTemplate().save(item.getData());
    return item.getData().getItemId();
  }
  

  public void ifcShow(Long itemId) {
      ItemDataIfc itemData = (ItemDataIfc) getHibernateTemplate().load(ItemData.class, itemId);
      if (itemData != null) {
        printIfcItem(itemData);
        printFacadeItem(itemData);
        //exportXml(itemData);
      }
  }


 public ItemFacade saveItem(ItemFacade item) throws DataFacadeException {
    try{
      ItemDataIfc itemdata = (ItemDataIfc) item.getData();
      itemdata.setLastModifiedDate(new Date());
      itemdata.setLastModifiedBy(AgentFacade.getAgentString());
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().saveOrUpdate(itemdata);
        item.setItemId(itemdata.getItemId());
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem save or update itemdata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    if ((item.getData()!= null) && (item.getData().getSection()!= null)) {
    AssessmentIfc assessment = item.getData().getSection().getAssessment();
    assessment.setLastModifiedBy(AgentFacade.getAgentString());
    assessment.setLastModifiedDate(new Date());
    retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
    	try {
    		getHibernateTemplate().update(assessment);
    		retryCount = 0;
    	}
    	catch (Exception e) {
    		log.warn("problem updating asssessment: "+e.getMessage());
    		retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
    	}
    }
    }
    return item;
    }
    catch(Exception e){
	e.printStackTrace();
	return null;
    }
 }



  private void printIfcItem(ItemDataIfc item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("**createdDate = " +
                       item.getCreatedDate());
  }

  private void printFacadeItem(ItemDataIfc item) {
    ItemFacade f = new ItemFacade(item);
    log.debug("****Id = " + f.getItemId());
    log.debug("****score = " + f.getScore());
    log.debug("****grade = " + f.getGrade());
    log.debug("****CorrectFeedback is lazy = " +
                       f.getCorrectItemFeedback());
    log.debug("****Objective not lazy = " +
                       f.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("****createdDate = " +
                       f.getCreatedDate());
    log.debug("****ItemType = " +
                       f.getItemType().getKeyword());
  }

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

  public ItemFacade getItem(Long itemId) {
    ItemData item = (ItemData) getHibernateTemplate().load(ItemData.class, itemId);
    return new ItemFacade(item);
  }


  public HashMap getItemsByKeyword(final String keyword) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, ItemText itext where itext.item=ab and itext.text like ? ");
	    		q.setString(0, keyword);
	    		return q.list();
	    	};
	    };
	    List list1 = getHibernateTemplate().executeFind(hcb);

//     List list1 = getHibernateTemplate().find("select ab from ItemData ab, ItemText itext where itext.item=ab and itext.text like ? ",new Object[] { keyword}, new org.hibernate.type.Type[] { Hibernate.STRING });

	    final HibernateCallback hcb2 = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select distinct ab from ItemData ab, Answer answer where answer.item=ab and answer.text like ? ");
	    		q.setString(0, keyword);
	    		return q.list();
	    	};
	    };
	    List list2 = getHibernateTemplate().executeFind(hcb2);

//     List list2 = getHibernateTemplate().find("select distinct ab from ItemData ab, Answer answer where answer.item=ab and answer.text like ? ",new Object[] { keyword}, new org.hibernate.type.Type[] { Hibernate.STRING });

	    final HibernateCallback hcb3 = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab, ItemMetaData md where md.item=ab and md.entry like ?  and md.label= ? ");
	    		q.setString(0, keyword);
	    		q.setString(1, "KEYWORD");
	    		return q.list();
	    	};
	    };
	    List list3 = getHibernateTemplate().executeFind(hcb3);

//     List list3 = getHibernateTemplate().find("select ab from ItemData ab, ItemMetaData metadata where metadata.item=ab and metadata.entry like ?  and metadata.label= 'KEYWORD' ", new Object[] { keyword}, new org.hibernate.type.Type[] { Hibernate.STRING });

	    final HibernateCallback hcb4 = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select ab from ItemData ab where ab.instruction like ?  ");
	    		q.setString(0, keyword);
	    		return q.list();
	    	};
	    };
	    List list4 = getHibernateTemplate().executeFind(hcb4);

//     List list4 = getHibernateTemplate().find("select ab from ItemData ab where ab.instruction like ?  ", new Object[] { keyword}, new org.hibernate.type.Type[] { Hibernate.STRING });
    HashMap itemfacadeMap = new HashMap();

    for (int i = 0; i < list1.size(); i++) {
      ItemData a = (ItemData) list1.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list2.size(); i++) {
      ItemData a = (ItemData) list2.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list3.size(); i++) {
      ItemData a = (ItemData) list3.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }
    for (int i = 0; i < list4.size(); i++) {
      ItemData a = (ItemData) list4.get(i);
      ItemFacade f = new ItemFacade(a);
      itemfacadeMap.put(f.getItemIdString(),f);
    }

    log.debug("Search for keyword, found: " + itemfacadeMap.size());
    return itemfacadeMap;

  }

  /*
   * This API is for linear access to create a dummy record to indicate the student
   * has taken action on the item (question). Therefore, we just need one itemTextId
   * for recording - use the first one (index 0).
   */
  public Long getItemTextId(final Long publishedItemId) {
	    final HibernateCallback hcb = new HibernateCallback(){
	    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    		Query q = session.createQuery("select i.id from PublishedItemText i where i.item.itemId = ?");
	    		q.setLong(0, publishedItemId.longValue());
	    		return q.list();
	    	};
	    };
	    List list = getHibernateTemplate().executeFind(hcb);
	    log.debug("list.size() = " + list.size());
	    Long itemTextId = (Long) list.get(0);
	    log.debug("itemTextId" + itemTextId);
	    return itemTextId;
  }

  public void deleteSet(Set s) {
		int retryCount = PersistenceService.getInstance().getRetryCount()
				.intValue();
		while (retryCount > 0) {
			try {
				if (s != null) { // need to dissociate with item before deleting in Hibernate 3
					getHibernateTemplate().deleteAll(s);
					retryCount = 0;
				} else {
					retryCount = 0;
				}
			} catch (Exception e) {
				log.warn("problem deleteSet: " + e.getMessage());
				retryCount = PersistenceService.getInstance().retryDeadlock(e,
						retryCount);
			}
		}
	}
	  
}

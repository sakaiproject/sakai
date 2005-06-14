package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osid.OsidException;
import org.sakaiproject.tool.assessment.business.AAMTree;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolAccessData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class QuestionPoolFacadeQueries
    extends HibernateDaoSupport implements QuestionPoolFacadeQueriesAPI {
  private static Log log = LogFactory.getLog(QuestionPoolFacadeQueries.class);

  public QuestionPoolFacadeQueries() {
  }

  public IdImpl getQuestionPoolId(String id) {
    return new IdImpl(id);
  }

  public IdImpl getQuestionPoolId(Long id) {
    return new IdImpl(id);
  }

  public IdImpl getQuestionPoolId(long id) {
    return new IdImpl(id);
  }

  /**
   * Get a list of all the pools in the site. Note that questions in each pool will not
   * be populated. We must keep this list updated.
   */
  public List getAllPools() {
    return getHibernateTemplate().find("from QuestionPoolData");
  }

  /**
       * Get all the pools that the agent has access to. The easiest way seems to be
   * #1. get all the existing pool
   * #2. get all the QuestionPoolAccessData record of the agent
   * #3. go through the existing pools and check it against the QuestionPoolAccessData (qpa) record to see if
   * the agent is granted access to it. qpa record (if exists) always trumps the default access right set
   * up for a pool
   * e.g. if the defaultAccessType for a pool is ACCESS_DENIED but the qpa record say ADMIN, then access=ADMIN
   * e.g. if the defaultAccessType for a pool is ADMIN but the qpa record say ACCESS_DENIED, then access=ACCESS_DENIED
   * e.g. if no qpa record exists, then access rule will follow the defaultAccessType set by the pool
   */
  public QuestionPoolIteratorFacade getAllPools(String agentId) {
    ArrayList qpList = new ArrayList();

    // #1.
    List poolList = getAllPools(); // we initiate it when application restart already
    //System.out.println("*** total pool in the DB =" + poolList.size());
    // #2. get all the QuestionPoolAccessData record belonging to the agent
    List questionPoolAccessList = getHibernateTemplate().find(
        "from QuestionPoolAccessData as qpa where qpa.agentId=?",
        new Object[] {agentId}
        , new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    //System.out.println("*** all existing questionPoolAccess" +
    //                   questionPoolAccessList.size());
    HashMap h = new HashMap(); // prepare a hashMap with (poolId, qpa)
    Iterator i = questionPoolAccessList.iterator();
    while (i.hasNext()) {
      QuestionPoolAccessData qpa = (QuestionPoolAccessData) i.next();
      h.put(qpa.getQuestionPoolId(), qpa);
    }

    // #3. We need to go through the existing QuestionPool and the QuestionPoolAccessData record
    // to determine the access type
    try {
      Iterator j = poolList.iterator();
      while (j.hasNext()) {
        QuestionPoolData qpp = (QuestionPoolData) j.next();
        // I really wish we don't need to populate  the questionpool size & subpool size for JSF
        // watch this for performance. hope Hibernate is smart enough not to load the entire question
        // - daisy, 10/04/04
        populateQuestionPoolItemDatas(qpp);

        QuestionPoolAccessData qpa = (QuestionPoolAccessData) h.get(qpp.
            getQuestionPoolId());
        if (qpa == null) {
          // if (qpa == null), take what is set for pool.
          if (! (QuestionPoolFacade.ACCESS_DENIED).equals(qpp.getAccessTypeId())) {
            qpList.add(getQuestionPool(qpp));
          }
        }
        else {
          if (! (QuestionPoolFacade.ACCESS_DENIED).equals(qpa.getAccessTypeId())) {
            qpp.setAccessTypeId(qpa.getAccessTypeId());
            qpList.add(getQuestionPool(qpp));
          }
        }
      }
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
    //System.out.println("*****final pool size is =" + qpList.size());
    return new QuestionPoolIteratorFacade(qpList);
  }

  public ArrayList getBasicInfoOfAllPools(String agentId) {
    List list = getHibernateTemplate().find(
        "select new QuestionPoolData(a.questionPoolId, a.title)from QuestionPoolData a where a.ownerId= ? ",
        new Object[] {agentId}
        , new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    //System.out.println("lydiatest getBasicInfoOfAllPolols list of qpdata=" +
    //                   list.size());
    ArrayList poolList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      QuestionPoolData a = (QuestionPoolData) list.get(i);
      QuestionPoolFacade f = new QuestionPoolFacade(a.getQuestionPoolId(),
          a.getTitle());
      poolList.add(f);
    }
    //System.out.println(
    //    "lydiatest getBasicInfoOfAllPolols poolList of qpfacade size =" +
    //    poolList.size());
    return poolList;
  }

  private QuestionPoolFacade getQuestionPool(QuestionPoolData qpp) {
    try {
      return new QuestionPoolFacade(qpp);
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return null;
    }
  }

  public List getAllItems(Long questionPoolId) {
    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?",
                                            new Object[] {questionPoolId}
                                            ,
                                            new net.sf.hibernate.type.Type[] {Hibernate.
                                            LONG});
    return list;

  }

  public List getAllItemFacadesOrderByItemText(Long questionPoolId,
                                               String orderBy) {
    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi, ItemText t where ab.itemId=qpi.itemId and ab.itemId=t.item and qpi.questionPoolId = ? order by t." +
                                            orderBy,
                                            new Object[] {questionPoolId}
                                            ,
                                            new net.sf.hibernate.type.Type[] {Hibernate.
                                            LONG});

    ArrayList itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;
  }

  public List getAllItemFacadesOrderByItemType(Long questionPoolId,
                                               String orderBy) {
    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi, TypeD t where ab.itemId=qpi.itemId and ab.typeId=t.typeId and qpi.questionPoolId = ? order by t." +
                                            orderBy,
                                            new Object[] {questionPoolId}
                                            ,
                                            new net.sf.hibernate.type.Type[] {Hibernate.
                                            LONG});

    ArrayList itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;
  }

  public List getAllItemFacades(Long questionPoolId) {
    List list = getHibernateTemplate().find("select ab from ItemData ab, QuestionPoolItemData qpi where ab.itemId=qpi.itemId and qpi.questionPoolId = ?",
                                            new Object[] {questionPoolId}
                                            ,
                                            new net.sf.hibernate.type.Type[] {Hibernate.
                                            LONG});
    ArrayList itemList = new ArrayList();
    for (int i = 0; i < list.size(); i++) {
      ItemData itemdata = (ItemData) list.get(i);
      ItemFacade f = new ItemFacade(itemdata);
      itemList.add(f);
    }
    return itemList;

  }

  private void populateQuestionPoolItemDatas(QuestionPoolData qpp) {
    try {
      Set questionPoolItems = qpp.getQuestionPoolItems();
      //System.out.println("**** 1. question size in pool according to pool = " +
      //                   questionPoolItems.size());
      if (questionPoolItems != null) {
        // let's get all the items for the specified pool in one shot
        HashMap h = new HashMap();
        List itemList = getAllItems(qpp.getQuestionPoolId());
        //System.out.println("**** 2. total questions in pool direct from DB = " +
        //                   itemList.size());

        Iterator j = itemList.iterator();
        while (j.hasNext()) {
          ItemData itemData = (ItemData) j.next();
          h.put(itemData.getItemIdString(), itemData);
        }
        ArrayList itemArrayList = new ArrayList();
        Iterator i = questionPoolItems.iterator();
        while (i.hasNext()) {
          QuestionPoolItemData questionPoolItem = (QuestionPoolItemData) i.next();
          ItemData itemData_0 = (ItemData) h.get(questionPoolItem.getItemId());
          Set itemTextSet = itemData_0.getItemTextSet();
          Iterator k = itemTextSet.iterator();
          while (k.hasNext()) {
            ItemText itemText = (ItemText) k.next();
            //System.out.println("**** 4.question text = " + itemText.getText());
          }
          itemArrayList.add(itemData_0);
        }
        qpp.setQuestions(itemArrayList);
        qpp.setSubPoolSize(new Integer(getSubPoolSize(qpp.getQuestionPoolId())));
        //System.out.println("****** questionpool id = " + qpp.getQuestionPoolId());
        //System.out.println("****** questionpool question size = " +
        //                   qpp.getQuestionSize());
        //System.out.println("****** questionpool subpool size = " +
        //                   qpp.getSubPoolSize());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
       * This method returns an ItemFacade that we can use to construct our ItemImpl
   */
  public ItemFacade getItem(String id) {
    ItemData item = (ItemData) getHibernateTemplate().load(ItemData.class, id);
    return new ItemFacade(item);
  }

  /**
   * Get a pool based on poolId. I am not sure why agent is not used though is being parsed.
   *
   * @param poolid DOCUMENTATION PENDING
   * @param agent DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolFacade getPool(Long poolId, String agent) {
    try {
      QuestionPoolData qpp = (QuestionPoolData) getHibernateTemplate().load(
          QuestionPoolData.class, poolId);
      // setAccessType
      setPoolAccessType(qpp, agent);
      // QuestionPoolItemData's identifier is a compsite identifier made up of
      // poolId and itemId <-- is regarded as "legacy DB" in Hibernate language.
      // We need to construct the properties for such as object ourselves.
      populateQuestionPoolItemDatas(qpp);
      return getQuestionPool(qpp);
    }
    catch (Exception e) {
      log.error(e);
      return null;
    }
  }

  public void setPoolAccessType(QuestionPoolData qpp, String agentId) {
    try {
      QuestionPoolAccessData qpa = getQuestionPoolAccessData(qpp.
          getQuestionPoolId(), agentId);
      if (qpa == null) {
        // if (qpa == null), take what is set for pool.
      }
      else {
        qpp.setAccessTypeId(qpa.getAccessTypeId());
      }
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
  }

  public QuestionPoolAccessData getQuestionPoolAccessData(Long poolId,
      String agentId) {
    List list = getHibernateTemplate().find("from QuestionPoolAccessData as qpa where qpa.questionPoolId =? and qpa.agentId=?",
                                            new Object[] {poolId, agentId}
                                            ,
                                            new net.sf.hibernate.type.Type[] {Hibernate.
                                            LONG, Hibernate.STRING});
    return (QuestionPoolAccessData) list.get(0);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ids DOCUMENTATION PENDING
   * @param sectionId DOCUMENTATION PENDING
   */
  public void addItemsToSection(Collection ids, long sectionId) {

  }

  /**
   * add a question to a pool
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void addItemToPool(QuestionPoolItemData qpi) {
    getHibernateTemplate().save(qpi);
  }

  /**
   * Delete pool and questions attached to it plus any subpool under it
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void deletePool(Long poolId, String agent, AAMTree tree) {
    try {
      // I decided not to load the questionpool and delete things that associate with it
      // because question is associated with it as ItemImpl not AssetBeanie. To delete
      // AssetBeanie, I would still need to do it manually. I cannot find a way to take advantage of the
      // Hibernate cascade feature, can you ? - daisyf

      // #1. delete all questions which mean AssetBeanie (not ItemImpl) 'cos AssetBeanie
      // is the one that is associated with the DB
      List itemList = getAllItems(poolId);
      getHibernateTemplate().deleteAll(itemList); // delete all AssetBeanie

      // #2. delete question and questionpool map.
      // Sorry! delete(java.lang.String queryString, java.lang.Object[] values, net.sf.hibernate.type.Type[] types)
      // is not available in this version of Spring that we are using. So, we are a using a long winded method.
      getHibernateTemplate().deleteAll(getHibernateTemplate().find(
          "select qpi from QuestionPoolItemData as qpi where qpi.questionPoolId= ?",
          new Object[] {poolId}
          , new net.sf.hibernate.type.Type[] {Hibernate.LONG}));

      // #3. Pool is owned by one but can be shared by multiple agents. So need to
      // delete all QuestionPoolAccessData record first. This seems to be missing in Navigo, nope? - daisyf
      List qpaList = getHibernateTemplate().find(
          "select qpa from QuestionPoolAccessData as qpa where qpa.questionPoolId= ?",
          new Object[] {poolId}
          , new net.sf.hibernate.type.Type[] {Hibernate.LONG});
      getHibernateTemplate().deleteAll(qpaList);

      // #4. Ready! delete pool now
      List qppList = getHibernateTemplate().find(
          "select qp from QuestionPoolData as qp where qp.id= ?",
          new Object[] {poolId}
          , new net.sf.hibernate.type.Type[] {Hibernate.LONG}); // there should only be one
      getHibernateTemplate().deleteAll(qppList);

      // #5. delete all subpools if any, this is recursive
      Iterator citer = (tree.getChildList(poolId)).iterator();
      while (citer.hasNext()) {
        deletePool( (Long) citer.next(), agent, tree);
      }
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
  }

  /**
   * Move pool under another pool. The dest pool must not be the
   * descendant of the source nor can they be the same pool .
   */
  public void movePool(String agentId, Long sourcePoolId, Long destPoolId) {
    try {

      QuestionPoolFacade sourcePool = getPool(sourcePoolId, agentId);
      if (destPoolId.equals(QuestionPoolFacade.ROOT_POOL) &&
          !sourcePoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        sourcePool.setParentPoolId(QuestionPoolFacade.ROOT_POOL);
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
      }
      else {
        QuestionPoolFacade destPool = getPool(destPoolId, agentId);
        sourcePool.setParentPoolId(destPool.getQuestionPoolId());
        getHibernateTemplate().update( (QuestionPoolData) sourcePool.getData());
      }
    }
    catch (Exception e) {
      log.warn(e.getMessage());
    }
  }

  /**
   * Is destination a descendant of the source?
   */
  public boolean isDescendantOf(QuestionPoolFacade destPool,
                                QuestionPoolFacade sourcePool) {

    Long tempPoolId = destPool.getQuestionPoolId();
    try {
      while((tempPoolId != null) &&
        (!tempPoolId.equals(QuestionPoolFacade.ROOT_POOL)))
      {
        QuestionPoolFacade tempPool = getPoolById(tempPoolId);
        if (tempPool.getParentPoolId().equals(sourcePool.getQuestionPoolId())) {
          return true;
        }
      tempPoolId = tempPool.getParentPoolId();
      }
      return false;
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return false;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void removeItemFromPool(String itemId, Long poolId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(poolId, itemId);
    getHibernateTemplate().delete(qpi);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */
  public void moveItemToPool(String itemId, Long sourceId, Long destId) {
    QuestionPoolItemData qpi = new QuestionPoolItemData(sourceId, itemId);
    getHibernateTemplate().delete(qpi);
    QuestionPoolItemData qpi2 = new QuestionPoolItemData(destId, itemId);
    getHibernateTemplate().save(qpi2);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pool DOCUMENTATION PENDING
   */
  public QuestionPoolFacade savePool(QuestionPoolFacade pool) {
    boolean insert = false;
    try {
      QuestionPoolData qpp = (QuestionPoolData) pool.getData();
      qpp.setLastModified(new Date());
      qpp.setLastModifiedById(AgentFacade.getAgentString());
      if (qpp.getQuestionPoolId() == null ||
          qpp.getQuestionPoolId().equals(new Long("0"))) { // indicate a new pool
        insert = true;
      }
      getHibernateTemplate().saveOrUpdate(qpp);

      if (insert) {
        // add a QuestionPoolAccessData record for the owner who should have ADMIN access to the pool
        QuestionPoolAccessData qpa = new QuestionPoolAccessData(qpp.
            getQuestionPoolId(), qpp.getOwnerId(), QuestionPoolData.ADMIN);
        getHibernateTemplate().save(qpa);
      }
      return pool;
    }
    catch (Exception e) {
      log.warn(e.getMessage());
      return null;
    }
  }

  /**
   * Get all the children pools of a pool. Return a list of QuestionPoolData
   * should return QuestionPool instead - need fixing, daisyf
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getSubPools(Long poolId) {
    return getHibernateTemplate().find(
        "from QuestionPoolData as qpp where qpp.parentPoolId=?",
        new Object[] {poolId}
        , new net.sf.hibernate.type.Type[] {Hibernate.LONG});
    //return new ArrayList();
  }

  public int getSubPoolSize(Long poolId) {
    return getSubPools(poolId).size();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public boolean hasSubPools(Long poolId) {
    List subPools =
        getHibernateTemplate().find(
        "from QuestionPoolData as qpp where qpp.parentPoolId=?",
        new Object[] {poolId}
        , new net.sf.hibernate.type.Type[] {Hibernate.LONG});
    if (subPools.size() > 0) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getPoolIdsByAgent(String agentId) {
    ArrayList idList = new ArrayList();
    List qpaList = getHibernateTemplate().find(
        "select qpa from QuestionPoolAccessData as qpa where qpa.agentId= ?",
        new Object[] {agentId}
        , new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    try {
      Iterator iter = qpaList.iterator();
      while (iter.hasNext()) {
        QuestionPoolAccessData qpa = (QuestionPoolAccessData) iter.next();
        idList.add(qpa.getQuestionPoolId()); // return a list of poolId (java.lang.Long)
      }
      return idList;
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Return a list of questionPoolId (java.lang.Long)
   *
   * @param itemId DOCUMENTATION PENDING
   * @param poolId DOCUMENTATION PENDING
   */

  public List getPoolIdsByItem(String itemId) {
    ArrayList idList = new ArrayList();
    List qpiList = getHibernateTemplate().find(
        "select qpi from QuestionPoolItemData as qpi where qpi.itemId= ?",
        new Object[] {itemId}
        , new net.sf.hibernate.type.Type[] {Hibernate.STRING});
    //System.out.println("lydiatest getPoolidsByItem qpilist.size(): " +
    //                   qpiList.size());
    try {
      Iterator iter = qpiList.iterator();
      while (iter.hasNext()) {
        QuestionPoolItemData qpa = (QuestionPoolItemData) iter.next();
        //System.out.println("lydiatest getPoolidsByItem : " +
        //                   qpa.getQuestionPoolId());
        idList.add(qpa.getQuestionPoolId()); // return a list of poolId (java.lang.Long)
      }
      //System.out.println("lydiatest getPoolidsByItem idList.size() : " +
      //                 idList.size());
      return idList;
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Copy a pool to a new location.
   */
  public void copyPool(AAMTree tree, String agentId, Long sourceId,
                       Long destId) {
    try {
      boolean haveCommonRoot = false;
      boolean duplicate = false;

      // Get the Pools
      QuestionPoolFacade oldPool = getPool(sourceId, agentId);

      // Are we creating a duplicate under the same parent?
      if (destId.equals(oldPool.getParentPoolId())) {
        duplicate = true;
      }

      // Determine if the Pools are in the same tree
      // If so, make sure the source level is not higher(up the tree)
      // than the dest. to avoid the endless loop.
      if (!duplicate) {
        haveCommonRoot = tree.haveCommonRoot
            (sourceId, destId);
      }

      if (haveCommonRoot &&
          (tree.poolLevel(sourceId) <=
           tree.poolLevel(destId))) {
        return; // Since otherwise it would cause an infinite loop.
        // We should revisit this.
      }

      // If Pools in same trees,
      if (haveCommonRoot) {
        // Copy to a Pool inside the root
        // Copy *this* Pool first
        QuestionPoolFacade newPool =
            new QuestionPoolFacade
            (oldPool.getData());
        newPool.setParentPoolId(destId);
        newPool.setQuestionPoolId(new Long(0));
        newPool = savePool(newPool);

        // Get the old questionpool Questions
        Collection questions = oldPool.getQuestions();
        //System.out.println("****** old pool Id = " + oldPool.getQuestionPoolId());
        //System.out.println("****** old pool question size = " + questions.size());
        Iterator iter = questions.iterator();

        // For each question ,
        while (iter.hasNext()) {
          ItemFacade item = (ItemFacade) iter.next();
          QuestionPoolItemData qpi = new QuestionPoolItemData
              (new Long(newPool.getId().getIdString()), item.getItemIdString());

          // add that question to questionpool
          addItemToPool(qpi);
        }

        // Get the SubPools of oldPool
        Iterator citer = (tree.getChildList(sourceId))
            .iterator();
        while (citer.hasNext()) {
          Long childPoolId =
              new Long( ( (IdImpl) citer.next()).getIdString());
          copyPool(
              tree, agentId, childPoolId,
              new Long(newPool.getId().getIdString()));
        }
      }
      else { // If Pools in different trees,

        // Copy to a Pool outside the same root
        // Copy *this* Pool first
        QuestionPoolFacade newPool =
            new QuestionPoolFacade(oldPool.getData());
        newPool.setParentPoolId(destId);
        newPool.setQuestionPoolId(new Long(0));

        //newPool = savePool(newPool);

        if (duplicate) {
          newPool.updateDisplayName(oldPool.getDisplayName() + " Copy");
        }
        else {
          newPool.updateDisplayName(oldPool.getDisplayName());
        }

        newPool = savePool(newPool);

        // Get the map of the old questionpool and its questions
        // we don't create new questions, what we are doing is create association
        // between the questions in the old pool and the new pool - daisyf
        Collection questionPoolItems = oldPool.getQuestionPoolItems();
        Collection newQuestionPoolItems = new ArrayList();
        Iterator iter = questionPoolItems.iterator();

        HashSet h = new HashSet();
        // For each question ,
        while (iter.hasNext()) {
          QuestionPoolItemData item = (QuestionPoolItemData) iter.next();
          //System.out.println("**** plus one question here, id = " +
          //                   item.getItemId());
          getHibernateTemplate().save(new QuestionPoolItemData(newPool.
              getQuestionPoolId(), item.getItemId()));
        }
        //System.out.println("**** no. of question in the new pool" +
        //                   newPool.getQuestionPoolItems().size());

        // Get the SubPools of oldPool
        Iterator citer = (tree.getChildList(sourceId))
            .iterator();
        while (citer.hasNext()) {
          Long childPoolId = (Long) citer.next();
          copyPool(tree, agentId, childPoolId, newPool.getQuestionPoolId());
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    catch (OsidException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws DataFacadeException {
    QuestionPoolFacadeQueriesAPI instance = new QuestionPoolFacadeQueries();
    // add an item
    if (args[0].equals("add")) {
      Long questionPoolId = instance.add();
      //System.out.println("**QuestionPool #" + questionPoolId);
    }
    if (args[0].equals("getQPItems")) {
      List items = instance.getAllItems(new Long(args[1])); // poolId
      for (int i = 0; i < items.size(); i++) {
        ItemData item = (ItemData) items.get(i);
        //System.out.println("Item #" + item.getItemId() + " has rationale= " +
        //                   item.getHasRationale());
      }
    }
    System.exit(0);
  }

  public Long add() {
    QuestionPoolData questionPool = new QuestionPoolData();
    questionPool.setTitle("Daisy Happy Pool");
    questionPool.setOwnerId("1");
    questionPool.setDateCreated(new Date());
    questionPool.setLastModifiedById("1");
    questionPool.setLastModified(new Date());
    getHibernateTemplate().save(questionPool);
    return questionPool.getQuestionPoolId();
  }

  public QuestionPoolFacade getPoolById(Long questionPoolId) {
    QuestionPoolFacade questionPoolFacade = null;
    //System.out.println("** poolId = " + questionPoolId);
    try {
      if (!questionPoolId.equals(QuestionPoolFacade.ROOT_POOL)) {
        //System.out.println("HT = " + getHibernateTemplate());
        //System.out.println("class = " + QuestionPoolData.class);
        QuestionPoolData questionPool = (QuestionPoolData) getHibernateTemplate().
            load(QuestionPoolData.class, questionPoolId);
        if (questionPool != null) {
          questionPoolFacade = new QuestionPoolFacade(questionPool);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      //System.out.println("Pool " + questionPoolId + " not found.");
    }
    return questionPoolFacade;
  }

  public HashMap getQuestionPoolItemMap(){
    HashMap h = new HashMap();
    String query = "from QuestionPoolItemData";
    List l = getHibernateTemplate().find(query);
    for (int i = 0; i < l.size(); i++) {
      QuestionPoolItemData q = (QuestionPoolItemData) l.get(i);
      h.put(q.getItemId(), q);
    }
    return h;
  }

}

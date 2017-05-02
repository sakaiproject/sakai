/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.questionpool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.struts.upload.FormFile;
import org.osid.shared.SharedException;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTreeImpl;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ExportResponsesBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;


/**
 * This holds question pool information.
 *
 * $Id$
 */
public class QuestionPoolBean implements Serializable
{
	
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = 418920360211039758L;
  public final static String ORIGIN_TOP = "poolList";
  public final static String EDIT_POOL = "editPool";
  public final static String EDIT_ASSESSMENT = "editAssessment";
  
  private String name;
  private Collection pools;
  private QuestionPoolDataBean currentPool;
  private QuestionPoolDataBean parentPool;

  private ArrayList currentItemIds;
  private ArrayList currentItems;

  private boolean allPoolsSelected;
  private boolean allItemsSelected;
  private boolean rootPoolSelected;
  private List poolListSelectItems;
  private List poolsToDelete;
  
  private QuestionPoolFacade poolToUnshare;
  
  private List itemsToDelete;
  private String[] selectedPools;
  private String[] selectedQuestions;
  private String[] destPools = {  }; // for multibox
  private String[] destItems = {  }; // items to delete
  private String sourcePart = null; // copy all questions from part
  private String destPool="0"; // for Move Pool Destination
  private FormFile filename; // for import /export
  private int htmlIdLevel; // pass this to javascript:collapseAll()
  private String questionType; // the question type to add
  private int parentPoolSize= 0; // the question type to add
  private ArrayList allItems;

  // for search Question
  private String[] searchByTypes = {  }; // for multibox
  private String searchQtext;
  private String searchQkeywords;
  private String searchQobj;
  private String searchQrubrics;

  // import questions from pool to assessment
  private String assessmentID;
  private String selectedAssessment;
  private String selectedSection;
  private boolean importToAuthoring;
  private String actionType;
  private String sortProperty = "title";
  private boolean sortAscending = true;
  private String sortCopyPoolProperty = "title";
  private boolean sortCopyPoolAscending = true;
  private String sortMovePoolProperty = "title";
  private boolean sortMovePoolAscending = true;
  private String sortSubPoolProperty = "title";
  private boolean sortSubPoolAscending = true;
  private String sortQuestionProperty = "text";
  private boolean sortQuestionAscending = true;
  
  private ArrayList addedPools;
  private ArrayList addedQuestions;
  
  private ItemFacade itemToPreview;
  private List<ItemContentsBean> itemsBean;

  private static Logger log = LoggerFactory.getLogger(QuestionPoolBean.class);


  // for JSF
  private Tree tree;
  private Collection qpools;
  private Collection copyQpools;
  private Collection moveQpools;
  private Collection sortedSubqpools;
  private QuestionPoolDataModel qpDataModel;
  private QuestionPoolDataModel qpDataModelCopy;
  private QuestionPoolDataModel subQpDataModel;
  
  // SAM-2049
  private String sortTransferPoolProperty = "title";
  private boolean sortTransferPoolAscending = true;
  private QuestionPoolDataModel qpDataModelTransfer;
  private QuestionPoolDataModel qpDataModelTransferSelected;
  private List<Long> transferPools;
  private String ownerId;
  private String confirmMessage;
  private boolean checkAll = false;

 private String addOrEdit;
  private String outcome;
  private String outcomeEdit;
  private long	 outcomePool;
  private String unsharePoolSource; 
  private String deletePoolSource;  // either from poolList.jsp , or from editPool.jsp
  private String addPoolSource;  // either from poolList.jsp , or from editPool.jsp

  //SAM-3049
  private boolean notCurrentPool;
  private String displayNameNotCPool;
  
  private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages");
  /**
   * Creates a new QuestionPoolBean object.
   */
  public QuestionPoolBean()
  {
    resetFields();
  }

  public int getRowIndex() {
      return qpDataModel.getRowIndex();
  }

  public QuestionPoolDataModel getQpools()
  {
	  if (qpDataModel == null) {
		  buildTree();
		  setQpDataModelByLevel();
	  }
	  log.debug("getQpools");
	  return qpDataModel;
  }

  public QuestionPoolDataModel getCopyQpools()
  {
//	  if (qpDataModelCopy == null) {
		  buildTree();
		  setQpDataModelByLevelCopy(getSortCopyPoolProperty(), getSortCopyPoolAscending());
//	  }
	  log.debug("getCopyQpools()");
	  return qpDataModelCopy;
  }

  public QuestionPoolDataModel getMoveQpools()
  {
//	  if (qpDataModelCopy == null) {
		  buildTreeCopy();
		  setQpDataModelByLevelCopy(getSortMovePoolProperty(), getSortMovePoolAscending());
//	  }
	  log.debug("getMoveQpools()");
	  return qpDataModelCopy;
  }

  public QuestionPoolDataModel getSortedSubqpools()
  {
	  log.debug("getSortedSubqpools()");
      return subQpDataModel;
  }

  public void sortSubqpoolsByProperty(ArrayList sortedList, String sortProperty, boolean sortAscending)
  {
        BeanSort sort = new BeanSort(sortedList, sortProperty);

        if ("lastModified".equals(sortProperty))
        {
         sort.toDateSort();
        }
        else
        {
         sort.toStringSort();
        }

        sort.sort();

        if (!sortAscending)
	{
	    Collections.reverse(sortedList);
        }

  }
  
  class TitleComparator implements Comparator {
	  public int compare(Object o1, Object o2) {
		  QuestionPoolFacade i1 = (QuestionPoolFacade)o1;
		  QuestionPoolFacade i2 = (QuestionPoolFacade)o2;
		  if (i1 == null && i2 != null) {
			  return 1;
		  }
		  if (i2 == null && i1 != null) {
			  return -1;
		  }
		  if (i2 == null && i1 == null) {
			  return 0;
		  }
		  if (i1.getTitle() == null && i2.getTitle() != null) {
			  return 1;
		  }
		  if (i2.getTitle() == null && i1.getTitle() != null) {
			  return -1;
		  }
		  if (i2.getTitle() == null && i1.getTitle() == null) {
			  return 0;
		  }
		  RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
		  try {
			RuleBasedCollator collator= new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
			return collator.compare(i1.getTitle(), i2.getTitle());
		  } catch (ParseException e) {}
		  return Collator.getInstance().compare(i1.getTitle(), i2.getTitle());
	  }
  }

  class QuestionSizeComparator implements Comparator<QuestionPoolFacade> {
	  @Override
	  public int compare(QuestionPoolFacade i1, QuestionPoolFacade i2) {
		  if (i1 == null && i2 != null) {
			  return 1;
		  }
		  if (i2 == null && i1 != null) {
			  return -1;
		  }
		  if (i2 == null && i1 == null) {
			  return 0;
		  }
		  return i1.getQuestionPoolItems().size() - i2.getQuestionPoolItems().size();
	  }
  }

  public void sortQpoolsByProperty(ArrayList sortedList, String sortProperty, boolean sortAscending)
  {
	  BeanSort sort = new BeanSort(sortedList, sortProperty);

	  // the generic sort code is pretty slow. Every time it needs to make  a
	  // comparison it fetches all properties of each of the QuestionPoolFacade's
	  // it is comparing. That includes more than just the properties that we're
	  // interested in. One of them requires a database transaction.  So
	  // optimize the sort on title, which is the one that is almost always used.
	  if ("title".equals(sortProperty)) {
		  Collections.sort(sortedList, new TitleComparator());
	  } else if ("questionSize".equals(sortProperty)) {
		  Collections.sort(sortedList, new QuestionSizeComparator());
	  } else {
		  if ("lastModified".equals(sortProperty))
		  {
			  sort.toDateSort();
		  }
		  else
		  {
			  sort.toStringSort();
		  }

		  sortedList = (ArrayList)sort.sort();

	  }
	  if (!sortAscending)
	  {
		  Collections.reverse(sortedList);
	  }
  }

  public void setAllItems(ArrayList list) {
      allItems = list;

  }


  public ArrayList getAllItems()
  {
      return allItems;
  }

  public void setSortedSubqpools(Collection spools)
  {
	sortedSubqpools = spools;

  }


  // This builds the tree.
  public void buildTree()
  {
    try
    {
      QuestionPoolService delegate = new QuestionPoolService();
      // getAllPools() returns pool in ascending order of poolId 
      // then a tree which represent the pool structure is built - daisyf
      //System.out.println("****** QPBean: build tree");
      tree=
        new QuestionPoolTreeImpl(
          (QuestionPoolIteratorFacade) delegate.getAllPoolsWithAccess(AgentFacade.getAgentString()));
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public void buildTreeCopy()
  {
	  try
	  {
		  QuestionPoolService delegate = new QuestionPoolService();
		  // getAllPools() returns pool in ascending order of poolId 
		  // then a tree which represent the pool structure is built - daisyf
		  //System.out.println("****** QPBean: build tree");
		  tree=
			  new QuestionPoolTreeImpl(
					  (QuestionPoolIteratorFacade) delegate.getAllPools(AgentFacade.getAgentString()));
	  }
	  catch(Exception e)
	  {
		  throw new RuntimeException(e);
	  }
  }

  private void printChildrenPool(Tree tree, QuestionPoolDataIfc pool, String stars){
	  List children = tree.getChildList(pool.getQuestionPoolId());
    Map childrenMap = tree.getChildren(pool.getQuestionPoolId());
    stars += "**";
    for (int i=0; i<children.size();i++){
      QuestionPoolDataIfc child = (QuestionPoolDataIfc) childrenMap.get(children.get(i).toString());
      //System.out.println(stars+child.getTitle()+":"+child.getLastModified());
      printChildrenPool(tree, child, stars);
    }  
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public List getPoolListSelectItems()
{
  if (poolListSelectItems == null) {
  	poolListSelectItems = new ArrayList();

      Collection objects = tree.getSortedObjects();
      Iterator iter = objects.iterator();
      while(iter.hasNext())
      {
        try
        {
          QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
  	poolListSelectItems.add(new SelectItem((pool.getQuestionPoolId().toString()), pool.getDisplayName() ) );
        }
        catch(Exception e)
        {
          throw new RuntimeException(e);
        }
      }

  }
  return poolListSelectItems;

}
  
  
  public boolean isNotCurrentPool() {
	  return notCurrentPool;
  }

  public void setNotCurrentPool(boolean notCurrentPool) {
	  this.notCurrentPool = notCurrentPool;
  }
  
  public String getDisplayNameNotCPool() {
	  return displayNameNotCPool;
  }

  public void setDisplayNameNotCPool(String displayNameNotCPool) {
	  this.displayNameNotCPool = displayNameNotCPool;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public void setItemsToDelete(List qpools)
  {
        itemsToDelete = qpools;
  }


  public List getItemsToDelete()
  {
	return itemsToDelete;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public void setPoolToUnshare(QuestionPoolFacade qpool)
  {
	poolToUnshare = qpool;
  }
  
  
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolFacade getPoolToUnshare()
  {
	return this.poolToUnshare;
  }
  
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public void setPoolsToDelete(List qpools)
  {
	poolsToDelete = qpools;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public List getPoolsToDelete()
  {

    List poolsToDeleteList = new ArrayList();
    if (poolsToDelete!=null)
    {
      Iterator iter = poolsToDelete.iterator();
      while(iter.hasNext())
      {
	try
        {
          QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
        poolsToDeleteList.add(pool);
        }
        catch(Exception e)
        {
          throw new RuntimeException(e);
        }

      }
    }

	return poolsToDeleteList;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Tree getTree()
  {
    return tree;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newPools DOCUMENTATION PENDING
   */
  public void setTree(Tree newtree)
  {
    tree = newtree;
  }



  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getPools()
  {
    return pools;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Object[] getPoolArray()
  {
    return pools.toArray();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newPools DOCUMENTATION PENDING
   */
  public void setPools(Collection newPools)
  {
    pools = newPools;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param newtype DOCUMENTATION PENDING
   */
  public void setQuestionType(String newtype)
  {
    questionType= newtype;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getQuestionType()
  {
	return questionType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newtype DOCUMENTATION PENDING
   */
  public void setSearchQtext(String pstr)
  {
    searchQtext = pstr;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getSearchQtext()
  {
        return searchQtext;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param newtype DOCUMENTATION PENDING
   */
  public void setSearchQkeywords (String pstr)
  {
    searchQkeywords = pstr;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getSearchQkeywords()
  {
        return searchQkeywords;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param newtype DOCUMENTATION PENDING
   */
  public void setSearchQobj(String pstr)
  {
    searchQobj = pstr;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getSearchQobj()
  {
        return searchQobj;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newtype DOCUMENTATION PENDING
   */
  public void setSearchQrubrics (String pstr)
  {
    searchQrubrics = pstr;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getSearchQrubrics()
  {
        return searchQrubrics;
  }

  	public String getSourcePart() {
		return sourcePart;
  	}

	public void setSourcePart(String s) {
		sourcePart = s;
	}

	public ArrayList getCurrentItemIds() {
		return currentItemIds;
	}

	public void setCurrentItemIds(ArrayList pstr) {
		currentItemIds = pstr;
	}

	public ArrayList getCurrentItems() {
		return currentItems;
	}

	public void setCurrentItems(ArrayList param) {
		currentItems = param;
	}


  public ItemFacade getItemToPreview()
  {
    return itemToPreview;
  }
  
  public void setItemToPreview(ItemFacade param)
  {
    itemToPreview= param ;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolDataBean getCurrentPool()
  {
    return currentPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newPool DOCUMENTATION PENDING
   */
  public void setCurrentPool(QuestionPoolDataBean newPool)
  {
    currentPool = newPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public QuestionPoolDataBean getParentPool()
  {
    return parentPool;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param newPool DOCUMENTATION PENDING
   */
  public void setParentPool(QuestionPoolDataBean newPool)
  {
    parentPool = newPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newPool DOCUMENTATION PENDING
   */
  public void setParentPoolSize(int n)
  {
    parentPoolSize = n ;
  }

  public int getParentPoolSize()
  {
    return parentPoolSize ;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getName()
  {
    return name;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setName(String newName)
  {
    name = newName;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setRootPoolSelected(boolean pallpools)
  {
    rootPoolSelected = pallpools;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getRootPoolSelected()
  {
    return rootPoolSelected;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setAllPoolsSelected(boolean pallpools)
  {
    allPoolsSelected = pallpools;
  }

  public void setAllItemsSelected(boolean pallpools)
  {
    allItemsSelected = pallpools;
  }

  public boolean getAllItemsSelected()
  {
    return allItemsSelected;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getAllPoolsSelected()
  {
    return allPoolsSelected;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setDestPool(String pPool)
  {
    destPool = pPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDestPool()
  {
    return destPool;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setDestPools(String[] pPool)
  {
    destPools = pPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String[] getDestPools()
  {
    return destPools;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setDestItems(String[] pPool)
  {
    destItems= pPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String[] getDestItems()
  {
    return destItems;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setSearchByTypes(String[] pstr)
  {
    searchByTypes= pstr;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String[] getSearchByTypes()
  {
    return searchByTypes;
  }

  public String getAssessmentID()
  {
    return assessmentID;
  }

  public void setAssessmentID(String pstr)
  {
    assessmentID = pstr;
  }

  public String getSelectedAssessment()
  {
    return selectedAssessment;
  }

  public void setSelectedAssessment(String pstr)
  {
    selectedAssessment= pstr;
  }

 public String getSelectedSection()
  {
    return selectedSection;
  }

  public void setSelectedSection(String pstr)
  {
    selectedSection= pstr;
  }

  public boolean getImportToAuthoring()
  {
    return importToAuthoring;
  }

  public void setImportToAuthoring(boolean pstr)
  {
    importToAuthoring = pstr;
  }

  public boolean getSortAscending()
  {
    return sortAscending;
  }

  public void setSortAscending(boolean pstr)
  {
    sortAscending = pstr;
  }

  public boolean getSortCopyPoolAscending()
  {
    return sortCopyPoolAscending;
  }

  public void setSortCopyPoolAscending(boolean pstr)
  {
    sortCopyPoolAscending = pstr;
  }

  public boolean getSortMovePoolAscending()
  {
    return sortMovePoolAscending;
  }

  public void setSortMovePoolAscending(boolean pstr)
  {
    sortMovePoolAscending = pstr;
  }

  public boolean getSortSubPoolAscending()
  {
    return sortSubPoolAscending;
  }

  public void setSortSubPoolAscending(boolean pstr)
  {
    sortSubPoolAscending = pstr;
  }
  
  public String getActionType()
  {
    return actionType;
  }

  public void setActionType(String pstr)
  {
    actionType= pstr;
  }

  public String getSortProperty()
  {
    return sortProperty;
  }

  public void setSortProperty(String newProperty)
  {
    sortProperty= newProperty;
  }

  public String getSortCopyPoolProperty()
  {
    return sortCopyPoolProperty;
  }

  public void setSortCopyPoolProperty(String newProperty)
  {
    sortCopyPoolProperty= newProperty;
  }

  public String getSortMovePoolProperty()
  {
    return sortMovePoolProperty;
  }

  public void setSortMovePoolProperty(String newProperty)
  {
    sortMovePoolProperty= newProperty;
  }

  public String getSortSubPoolProperty()
  {
    return sortSubPoolProperty;
  }

  public void setSortSubPoolProperty(String newProperty)
  {
    sortSubPoolProperty= newProperty;
  }
  
public String getAddOrEdit()
  {
    return addOrEdit;
  }

  /**
   * @param param
   */
  public void setAddOrEdit(String param)
  {
    this.addOrEdit= param;
  }

  public String getOutcome()
  {
    return outcome;
  }

  /**
   * set the outcome for doit()
   * @param param
   */
  public void setOutcome(String param)
  {
    this.outcome= param;
  }
 
  public String getOutcomeEdit()
  {
    return outcomeEdit;
  }

  /**
   * set the outcome for doit()
   * @param param
   */
  public void setOutcomeEdit(String param)
  {
    this.outcomeEdit= param;
  }

  public long getOutcomePool()
  {
    return outcomePool;
  }

  /**
   * set the outcome for doit()
   * @param param
   */
  public void setOutcomePool(long param)
  {
    this.outcomePool= param;
  }

  public String getDeletePoolSource()
  {
    return deletePoolSource;
  }

  /**
   * set the outcome for doit()
   * @param param
   */
  public void setDeletePoolSource(String param)
  {
    this.deletePoolSource= param;
  }

  public String getAddPoolSource()
  {
    return addPoolSource;
  }

  public void setAddPoolSource(String param)
  {
    this.addPoolSource= param;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setSelectedQuestions(String[] pPool)
  {
    selectedQuestions = pPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setSelectedPools(String[] pPool)
  {
    selectedPools = pPool;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String[] getSelectedQuestions()
  {
    return selectedQuestions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String[] getSelectedPools()
  {
    return selectedPools;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param file DOCUMENTATION PENDING
   */
  public void setFilename(FormFile file)
  {
    filename = file;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param file DOCUMENTATION PENDING
   */
  public FormFile getFilename()
  {
    return filename;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param int DOCUMENTATION PENDING
   */
  public int getHtmlIdLevel()
  {
    return htmlIdLevel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param int DOCUMENTATION PENDING
   */
  public void setHtmlIdLevel(int plevel)
  {
    htmlIdLevel = plevel;
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void resetFields()
  {
    pools = new ArrayList();
  }



// Item level actions
  public String startCopyQuestion()
  {
	  	setOutComeParams();
	    this.sourcePart = null;
        getCheckedQuestion();
        return "copyPool";
  }

  public String startMoveQuestion()
  {
	  	setOutComeParams();
        getCheckedQuestion();
        return "movePool";
  }

	public String startCopyQuestions() {
		setOutComeParams("editPool");
		this.sourcePart = null;
		getCheckedQuestions();
		return "copyPool";
	}

	public String startMoveQuestions() {
		setOutComeParams("editPool");
		getCheckedQuestions();
		return "movePool";
	}
  
     public String moveQuestion() {
		String sourceId = "";
		String destId = "";
		sourceId = this.getCurrentPool().getId().toString();
		ArrayList sourceItemIds = this.getCurrentItemIds();
		String originId = Long.toString(ORIGIN_TOP.equals(getOutcome())?0:getOutcomePool());

		destId = ContextUtil.lookupParam("movePool:selectedRadioBtn");

		if ((sourceId != null) && (destId != null) && (sourceItemIds != null)) {
			try {
				QuestionPoolService delegate = new QuestionPoolService();

				Iterator iter = sourceItemIds.iterator();
				while (iter.hasNext()) {
					String sourceItemId = (String) iter.next();
					// originally this returned "movePool" if we found it
					// in dest. This seems wrong. No error message, just
					// return to an irrelevant screen. I think it's better
					// just to skip that item. One could argue for a warning
					// message.
					if (!hasItemInDestPool(sourceItemId, destId)) {
						delegate.moveItemToPool(new Long(sourceItemId),
								new Long(sourceId), new Long(destId));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		//Questionpool has been revised
		EventTrackingService.post(EventTrackingService.newEvent("sam.questionpool.questionmoved", "/sam/" +AgentFacade.getCurrentSiteId() + "/sourceId=" + sourceId + " destId=" + destId, true));


		setOutComeTree(originId);
		
	return getOutcome();
	}

    // This is the link in edit assessment to copy all questions to a pool
	// in order to use copyPool, we need to set up a valid pool context.
	// that's what most of this is. The only actual work is setting sourcePart
	public String startCopyFromAssessment() {
		// find the first pool, and set it up
		QuestionPoolService delegate = new QuestionPoolService();
		ArrayList pools = delegate.getBasicInfoOfAllPools(AgentFacade
				.getAgentString());
		Iterator iter = pools.iterator();

		// verify that the sectionId is in the current assessment
		String sectionId = ContextUtil.lookupParam("sectionId");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		List<SelectItem> sectionList = assessmentBean.getSectionList();
		boolean foundPart = false;
		for (int i = 0; i < sectionList.size(); i++) {
		    SelectItem s = sectionList.get(i);
		    if (sectionId.equals((String)s.getValue())) foundPart = true;
		}
		if (!foundPart) {
		    FacesContext context=FacesContext.getCurrentInstance();
		    String err;
		    err=rb.getString("no_pools_error");
		    context.addMessage(null, new FacesMessage(err));
		    return "editAssessment";
		}
		
		// permission check to ensure the user should have access to the questions being copied
		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade af = assessmentService.getBasicInfoOfAnAssessmentFromSectionId(new Long(sectionId));
		String assessmentId = af.getAssessmentBaseId().toString();
		String createdBy = af.getCreatedBy();
		if (!authzBean.isUserAllowedToEditAssessment(assessmentId, createdBy, false))
		{
			FacesContext context = FacesContext.getCurrentInstance();
			String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
			context.addMessage(null, new FacesMessage(err));
			return "editAssessment";
		}

		if (iter.hasNext()) {
			// first pool, if there is one
			QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
			String poolId = pool.getQuestionPoolId().toString();
			buildTree();
			startEditPoolAgain(poolId);
			setActionType("item");
			this.sourcePart = sectionId;
			return "copyPool";
		}
		
        FacesContext context=FacesContext.getCurrentInstance();
        String err;
        err=rb.getString("no_pools_error");
        context.addMessage(null, new FacesMessage(err));

		return EDIT_ASSESSMENT;
	}
     
  public boolean hasItemInDestPool(String sourceItemId, String destId){
  
              QuestionPoolService delegate = new QuestionPoolService();
              // check if the item already exists in the destPool
              if (delegate.hasItem(sourceItemId, new Long(destId))){
                // we do not want to add duplicated items, show message

                FacesContext context=FacesContext.getCurrentInstance();
                String err;
                err=rb.getString("copy_duplicate_error");
                context.addMessage(null,new FacesMessage(err));
                return true;
              }
  	      else {	
                return false;
 	      } 
  }


  public String copyQuestion() {
		if (getSourcePart() != null)
			return copyQuestionsFromPart();

		// Long sourceId = new Long(0);
		String destId = "";
		ArrayList sourceItems = this.getCurrentItems();

		ArrayList destpools = ContextUtil.paramArrayValueLike("checkboxes");
		// sourceId = this.getCurrentPool().getId();
		String originId = Long.toString(ORIGIN_TOP.equals(getOutcome())?0:getOutcomePool());
		Iterator iter = destpools.iterator();
		while (iter.hasNext()) {

			destId = (String) iter.next();
			if ((sourceItems != null) && (destId != null)) {

				try {
					QuestionPoolService questionPoolService = new QuestionPoolService();
					QuestionPoolService delegate = new QuestionPoolService();

					Iterator iter2 = sourceItems.iterator();
					while (iter2.hasNext()) {
						ItemFacade sourceItem = (ItemFacade) iter2.next();
						String sourceItemId = sourceItem.getItemIdString();
						// originally this returned "copyPool" if we found it
						// in dest. This seems wrong. No error message, just
						// return to an irrelevant screen. I think it's better
						// just to skip that item. One could argue for a warning
						// message.
						if (!hasItemInDestPool(sourceItemId, destId)) {
							Long copyItemFacadeId = questionPoolService
									.copyItemFacade(sourceItem.getData());
							delegate.addItemToPool(copyItemFacadeId, new Long(
									destId));
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}

	setOutComeTree(originId);
	
	return getOutcome();
	}

  public String copyQuestionsFromPart() {

		SectionService sectiondelegate = new SectionService();

		SectionFacade section = sectiondelegate.getSection(new Long(
				getSourcePart()), AgentFacade.getAgentString());

		Set itemSet = section.getItemSet();

		ArrayList destpools = ContextUtil.paramArrayValueLike("checkboxes");
		// sourceId = this.getCurrentPool().getId();

		Iterator iter = destpools.iterator();
		while (iter.hasNext()) {

			String destId = (String) iter.next();
			if ((itemSet != null) && (destId != null)) {
				try {
					QuestionPoolService delegate = new QuestionPoolService();

					Iterator iter2 = itemSet.iterator();
					while (iter2.hasNext()) {
			            ItemDataIfc sourceItem = (ItemDataIfc)iter2.next();
			            Long sourceItemId = delegate.copyItemFacade(sourceItem);
		                delegate.addItemToPool(sourceItemId, new Long(destId));
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
		setSourcePart(null);
		return EDIT_ASSESSMENT;
	}

 public String removeQuestionsFromPool(){
     String sourceId = this.getCurrentPool().getId().toString();

     List itemlist = this.getItemsToDelete() == null ? new ArrayList() : this.getItemsToDelete();

     Iterator iter = itemlist.iterator();
     while(iter.hasNext())
     {

       ItemFacade itemfacade = (ItemFacade) iter.next();
       Long itemid = itemfacade.getItemId();
       QuestionPoolService delegate = new QuestionPoolService();
       delegate.removeQuestionFromPool(itemid, new Long(sourceId));

       //Questionpool has been deleted
       EventTrackingService.post(EventTrackingService.newEvent("sam.questionpool.deleteitem", "/sam/" +AgentFacade.getCurrentSiteId() + "/removed itemId=" + itemid, true));


       // check to see if any pools are linked to this item
       List poollist = delegate.getPoolIdsByItem(itemfacade.getItemIdString());
       if (poollist.isEmpty()) {

	 if (itemfacade.getSection() == null) {
            // if no assessment refers to this pool then delete this item from db
            ItemService itemdelegate = new ItemService();
	    itemdelegate.deleteItem(itemfacade.getItemId(), AgentFacade.getAgentString());
	 }
         else {
            // if has assessment refers , remove metadata for selectedPool .
            ItemService itemdelegate = new ItemService();
	    itemdelegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.POOLID, AgentFacade.getAgentString());

/*
	    Set itemmetadataSet= itemfacade.getItemMetaDataSet();
       	    ItemMetaData metaTodelete= null;
            Iterator metaiter = itemmetadataSet.iterator();
     	      while (metaiter.hasNext()){
       		ItemMetaData meta= (ItemMetaData) metaiter.next();
       		if (meta.getLabel().equals(ItemMetaData.POOLID)){
       		  metaTodelete= meta;
        	}
              }
              if (metaTodelete!=null) {
                itemmetadataSet.remove(metaTodelete);
              }
	    itemfacade.setItemMetaDataSet(itemmetadataSet);
	    itemdelegate.saveItem(itemfacade);
*/

         }
       }
     }

     buildTree();
     this.startEditPoolAgain(sourceId);  // return to edit pool
     return "editPool";
  }

  public void getCheckedQuestion()
  {
	String itemId= ContextUtil.lookupParam("itemid");
	ItemService delegate = new ItemService();
	ItemFacade itemfacade= delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
	ArrayList itemIds = new ArrayList();
	itemIds.add(itemId);
	setCurrentItemIds(itemIds);
	 
	ArrayList itemFacades = new ArrayList();
	itemFacades.add(itemfacade);
	setCurrentItems(itemFacades);
	setActionType("item");
  }

  public void getCheckedQuestions() {
		// String itemId= ContextUtil.lookupParam("itemid");

		ArrayList destItems = ContextUtil.paramArrayValueLike("removeCheckbox");
		ArrayList itemIds = new ArrayList();
		ArrayList itemFacades = new ArrayList();

		ItemService delegate = new ItemService();
		Iterator iter = destItems.iterator();

		while (iter.hasNext()) {
			String itemId = (String) iter.next();
			ItemFacade itemfacade = delegate.getItem(new Long(itemId),
					AgentFacade.getAgentString());
			itemFacades.add(itemfacade);
			itemIds.add(itemId);
		}

		setCurrentItemIds(itemIds);
		setCurrentItems(itemFacades);

		setActionType("item");
	}


// Pool level actions
  public String startCopyPool()
  {
	log.debug("inside startCopyPool()");
	setOutComeParams();
	getCheckedPool();
	buildTreeCopy();
	setActionType("pool");
	setQpDataModelByPropertyCopy(getSortCopyPoolProperty(), getSortCopyPoolAscending());
	return "copyPool";
  }

  public String startMovePool()
  {
	log.debug("inside startMovePool()");  
	setOutComeParams();
	getCheckedPool();
	buildTreeCopy();
	setActionType("pool");
	setQpDataModelByPropertyCopy(getSortMovePoolProperty(), getSortMovePoolAscending());
	return "movePool";
  }

  public void getCheckedPool()
  {
   try{
     QuestionPoolDataBean pool = new QuestionPoolDataBean();

          String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
          if((qpid != null) && (! qpid.equals("null")))
          {
          pool.setId(new Long(qpid));
          }
          else {
                // This should not ococur.  It should always have an id.
		return ;
          }

          // Get all data from the database
          QuestionPoolService delegate = new QuestionPoolService();

          // Does the user have permission to copy or move this pool?
          List<Long> poolsWithAccess = delegate.getPoolIdsByAgent(AgentFacade.getAgentString());
          if (!poolsWithAccess.contains(pool.getId())) {
              throw new IllegalArgumentException("User " + AgentFacade.getAgentString() + " does not have access to question pool id " + pool.getId() + " for move or copy");
          }

          QuestionPoolFacade thepool =
            delegate.getPool(
              new Long(qpid), AgentFacade.getAgentString());
          tree.setCurrentId(thepool.getQuestionPoolId());

          pool.setDisplayName(thepool.getDisplayName());
          pool.setParentPoolId(thepool.getParentPoolId());
          pool.setDescription(thepool.getDescription());
          pool.setOwner(thepool.getOwnerDisplayName());
          //pool.setOwner(thepool.getOwnerId());
          pool.setObjectives(thepool.getObjectives());
          pool.setKeywords(thepool.getKeywords());
          pool.setOrganizationName(thepool.getOrganizationName());
//          pool.setProperties((QuestionPoolData) thepool.getData());
// TODO  which one should I use?
//          pool.setNumberOfSubpools(
//            new Integer(tree.getChildList(thepool.getQuestionPoolId()).size()).toString());
          pool.setNumberOfSubpools(thepool.getSubPoolSize().toString());
          pool.setNumberOfQuestions(thepool.getQuestionSize().toString());
          //pool.setDateCreated(thepool.getDateCreated());

      pool.setLastModified(new Date());

      this.setCurrentPool(pool);
    }
    catch(RuntimeException e)
    {
      e.printStackTrace();
      throw e;
    }

  }


  public String copyPool(){

      Long sourceId =  Long.valueOf(0);
      String destId= "";
      boolean isUnique=true;

	ArrayList destpools= ContextUtil.paramArrayValueLike("checkboxes");
 	sourceId = this.getCurrentPool().getId();
        String currentName=this.getCurrentPool().getDisplayName();
    String originId = Long.toString(ORIGIN_TOP.equals(getOutcome())?0:getOutcomePool());
	Iterator iter = destpools.iterator();
     
      while(iter.hasNext())
      {

          destId = (String) iter.next();
	  QuestionPoolService delegate = new QuestionPoolService();
          

          if((sourceId.longValue() != 0) && (destId != null))
          {
            try
            {
               QuestionPoolFacade oldPool =delegate.getPool(sourceId, AgentFacade.getAgentString());
	      //if dest != source's parent, then throw error if dest has a duplicate pool name 
		if(!destId.equals((oldPool.getParentPoolId()).toString())){
		    isUnique=delegate.poolIsUnique(originId,currentName,destId, AgentFacade.getAgentString());
		    if(!isUnique){
		    	String err1=rb.getString("copy_duplicateName_error");
		    	FacesContext context=FacesContext.getCurrentInstance();
			context.addMessage(null,new FacesMessage(err1));
       
			return "copyPool";
		    }
		}

        // if dest = source's parent,i.e copying to it's own parent ,  then if there is an existing pool with the same name, copyPool() will create a new pool with Copy prepended in the pool name
		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages");
		String copy = rb.getString("prepend_copy");
		String of = rb.getString("prepend_of");
		delegate.copyPool(tree, AgentFacade.getAgentString(),
				  sourceId, new Long(destId), copy, of);
	    }
            catch(Exception e)
            {
		e.printStackTrace();
                throw new RuntimeException(e);
            
	    }
	  }
      }

      setOutComeTree(originId);

	return getOutcome();

  }


  public String movePool(){
     String sourceId = "";
      String destId = "";
      sourceId = this.getCurrentPool().getId().toString();
      String currentName=this.getCurrentPool().getDisplayName();
      String originId = Long.toString(ORIGIN_TOP.equals(getOutcome())?0:getOutcomePool());
      boolean isUnique=true;
	destId= ContextUtil.lookupParam("movePool:selectedRadioBtn");

      // added check for "".equals(destId) SAK-4435 , when no dest is selected
        if((sourceId != null) && (destId != null) && (!"".equals(destId)))
        {
          try
          {
            QuestionPoolService delegate = new QuestionPoolService();
 isUnique=delegate.poolIsUnique(originId,currentName,destId, AgentFacade.getAgentString());
              if(!isUnique){
            	  String err1=rb.getString("move_duplicateName_error");
            	  FacesContext context = FacesContext.getCurrentInstance();
	context.addMessage(null,new FacesMessage(err1));
       
	return "movePool";
	      }
            delegate.movePool(
              AgentFacade.getAgentString(),
              new Long(sourceId), new Long(destId));
       
	  }
          catch(Exception e)
          {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }

        setOutComeTree(originId);
      
	return getOutcome();
  }

  public String addPool() {
		String addsource = "poollist";
		setOutComeParams();
		addsource = getOutcome();
		this.setAddPoolSource(addsource);
		startCreatePool();
		this.setAddOrEdit("add");	
		
		QuestionPoolDataBean pool = new QuestionPoolDataBean();
		setPoolInfo(pool);
		this.setParentPool(pool);
		return "addPool";
	}

  public void startCreatePool()
  {
   try{
     QuestionPoolDataBean pool = new QuestionPoolDataBean();
      int htmlIdLevel = 0;


 // create a new pool with 2 properties: owner and parentpool
 	pool.setOwner(AgentFacade.getDisplayName(AgentFacade.getAgentString()));


          String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
	if((qpid !=null) && (!qpid.equals("0")))
// qpid = 0 if creating a new pool at root level
          {
            pool.setParentPoolId(new Long(qpid));

          }

// need to set indivdiual pool properties
      pool.setLastModified(new Date());
      this.setCurrentPool(pool);
      this.setHtmlIdLevel(htmlIdLevel);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  // To allow a user to remove a shared pool from the list of pools (previous confirmation)
  public String startUnsharePool(){
	
	 String poolId = ContextUtil.lookupParam("qpid");

	 QuestionPoolService delegate = new QuestionPoolService();
	 QuestionPoolFacade qPool = delegate.getPool(new Long(poolId), AgentFacade.getAgentString());
	        
	 this.setPoolToUnshare(qPool);
	 return "unsharePool";
  }
  
  // To allow a user to remove a shared pool from the list of pools (post confirmation)
  public String unsharePool(){
	  QuestionPoolService delegate = new QuestionPoolService();
      QuestionPoolFacade qpool = this.getPoolToUnshare();
      Long poolId= qpool.getQuestionPoolId();

      delegate.removeQuestionPoolAccess(tree, AgentFacade.getAgentString(), poolId, QuestionPoolData.READ_COPY);
      //Questionpool has been unshared
      EventTrackingService.post(EventTrackingService.newEvent("sam.questionpool.unshare", "/sam/" +AgentFacade.getCurrentSiteId() + "/unshared poolId=" + poolId, true));
      
      buildTree();
      setQpDataModelByLevel();
      
      return "poolList";
}


  public String confirmRemovePool(){
	  setOutComeParams();
// used by the editpool.jsp to remove one subpool at a time
	this.setDeletePoolSource("editpool");

//ItemAuthorBean itemauthorbean= (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
String poolId = ContextUtil.lookupParam("qpid");


// we are adding one pool to an arrayList so we can reuse the deletePool() method, because deletePool() method expects an arrayList of pools to be deleted.  It is used by the poolList.jsp to delete multiple pools.

    	List qpools = new ArrayList();
        QuestionPoolService delegate = new QuestionPoolService();
        QuestionPoolFacade qPool =
            delegate.getPool(new Long(poolId), AgentFacade.getAgentString());
        qpools.add(qPool);

        this.setPoolsToDelete(qpools);
        return "removePool";
  }

  public String startRemovePool(){
// need to check if the pool is used by any random draw assessments 

// used by the poolList.jsp
	this.setDeletePoolSource("poollist");
      String poolId= "";

        ArrayList destpools= ContextUtil.paramArrayValueLike("removeCheckbox");

    	List qpools = new ArrayList();
        Iterator iter = destpools.iterator();
      while(iter.hasNext())
      {

        poolId = (String) iter.next();

        QuestionPoolService delegate = new QuestionPoolService();
        QuestionPoolFacade qPool =
            delegate.getPool(new Long(poolId), AgentFacade.getAgentString());
        qpools.add(qPool);

        }

	this.setPoolsToDelete(qpools);
	return "removePool";
  }

  public String removePool(){

        QuestionPoolService delegate = new QuestionPoolService();
        List qpools = this.getPoolsToDelete();
        Iterator iter = qpools.iterator();
      while(iter.hasNext())
      {

        QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
	Long poolId= pool.getQuestionPoolId();

        delegate.deletePool(poolId, AgentFacade.getAgentString(), tree);

          //Questionpool has been deleted
          EventTrackingService.post(EventTrackingService.newEvent("sam.questionpool.delete", "/sam/" +AgentFacade.getCurrentSiteId() + "/removed poolId=" + poolId, true));

        }
	buildTree();
	
	if (this.getDeletePoolSource().equals("editpool")) {
    // #1a - so reset subpools tree
	Collection objects = tree.getSortedObjects(this.getCurrentPool().getId());
          this.setSortedSubqpools(objects);
	  QuestionPoolFacade thepool= delegate.getPool(this.getCurrentPool().getId(), AgentFacade.getAgentString());
          this.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());
      setSubQpDataModelByLevel();
	  return "editPool";
	}
	else {
      setQpDataModelByLevel();
	  return "poolList";
	}
  }

  public String cancelPool() {
	  if (getSourcePart() != null) {	
		  setSourcePart(null);
		  setOutcome(EDIT_ASSESSMENT);
	  }
	  else if (ORIGIN_TOP.equals(getOutcome()) || getOutcomePool() == 0){		  
		setCurrentPool(null);
		setOutcome(ORIGIN_TOP);
		buildTree();
		setQpDataModelByLevel();
	  }else{
		  startEditPoolAgain(Long.toString(getOutcomePool()));
	      buildTree();
	      setSubQpDataModelByLevel(); 
  }

	  return getOutcome();
  }

  public String importPool(){
	return "importPool";
  }

  public String importQuestion(){
	return "importQuestion";
  }

  public String exportQuestion(){
	return "exportQuestion";
  }

  public String returnToAuthoring(){
	return "author";
  }

  public String editPool(){
	if(notCurrentPool){	
		notCurrentPool = false;
		displayNameNotCPool = "";
	}
	  
 	startEditPool();
    this.setAddOrEdit("edit");
    setSubQpDataModelByLevel();
    
	return "editPool";
  }


  public void startEditPool()
  {
	String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
  	startEditPoolAgain(qpid);
  	setOutComeParams();
  }


  public void startEditPoolAgain(String qpid)
  {
   try{
	QuestionPoolDataBean pool = new QuestionPoolDataBean();
	int htmlIdLevel = 0;
	ArrayList allparentPools = new ArrayList();

          if(qpid != null)
          {
          	pool.setId(new Long(qpid));
          }
	  else {
		// should always have an id.
		return;
	  }

          // Get all data from the database
          QuestionPoolService delegate = new QuestionPoolService();
          // The call to getPool will retrieve all questions/answers/metadata/etc via Hibernate
          QuestionPoolFacade thepool = delegate.getPool(new Long(qpid), AgentFacade.getAgentString());
          tree.setCurrentId(thepool.getQuestionPoolId());

          Long ppoolid = thepool.getParentPoolId();

	 pool.setParentPools(allparentPools);
	 pool.setParentPoolsArray(allparentPools);
          while(! ppoolid.toString().equals("0"))
          {
            QuestionPoolFacade ppool =
              delegate.getPool(ppoolid, AgentFacade.getAgentString());
            if(ppool != null)
            {
              allparentPools.add(0, ppool);
              ppoolid = ppool.getParentPoolId();
            }

          }

          if(allparentPools != null)
          {
		pool.setParentPools(allparentPools);
		pool.setParentPoolsArray(allparentPools);
          }

	 this.setParentPoolSize(allparentPools.size());
          String htmlID = tree.getCurrentObjectHTMLId();

          // pass the htmlIdLevel to the collapseRowByLevel javascript
          String[] result = htmlID.split("-");
          htmlIdLevel = result.length + 1;

          pool.setDisplayName(thepool.getDisplayName());
          pool.setParentPoolId(thepool.getParentPoolId());
          pool.setDescription(thepool.getDescription());
          pool.setOwner(thepool.getOwnerDisplayName());
          pool.setObjectives(thepool.getObjectives());
          pool.setOrganizationName(thepool.getOrganizationName());
          pool.setKeywords(thepool.getKeywords());
          pool.setNumberOfSubpools(thepool.getSubPoolSize().toString());
          pool.setNumberOfQuestions(thepool.getQuestionSize().toString());
          pool.setDateCreated(thepool.getDateCreated());

          Collection objects = tree.getSortedObjects(thepool.getQuestionPoolId());
	  this.setSortedSubqpools(objects);

      pool.setLastModified(new Date());
      this.setCurrentPool(pool);
      this.setHtmlIdLevel(htmlIdLevel);

      // SAM-3024 the former call to delegate.getAllItems retrieved all questions/answers/etc again
      this.setAllItems(new ArrayList(thepool.getQuestions()));
    }
    catch(Exception e)
    {
      log.error("Error in startEditPoolAgain", e);
      throw new RuntimeException(e);
    }
  }


  public String selectQuestionType() {
		ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
		String poolid = ContextUtil.lookupParam("poolId");
		if (poolid != null) {
			setOutComeParams();
			itemauthorbean.setQpoolId(poolid);
			itemauthorbean.setTarget(ItemAuthorBean.FROM_QUESTIONPOOL);

			itemauthorbean.setItemType("");
			itemauthorbean.setItemTypeString("");

			//QuestionPoolDataBean pool = new QuestionPoolDataBean();
			QuestionPoolDataBean pool = this.getCurrentPool();
			this.setPoolInfo(pool);
			this.setCurrentPool(pool);
			return "selectQuestionType";
		} else {
			return "editPool"; // should not come to this
		}
	}

  public String sortByColumnHeader() {
    String sortString = ContextUtil.lookupParam("orderBy");
    String ascending = ContextUtil.lookupParam("ascending");
    this.setSortProperty(sortString);
    this.setSortAscending((Boolean.valueOf(ascending)).booleanValue());
    setQpDataModelByLevel();
    
    return "poolList";
  }

  public String sortCopyPoolByColumnHeader() {

    String sortString = ContextUtil.lookupParam("copyPoolOrderBy");
    String ascending = ContextUtil.lookupParam("copyPoolAscending");
    this.setSortCopyPoolProperty(sortString);
    this.setSortCopyPoolAscending((Boolean.valueOf(ascending)).booleanValue());
    setQpDataModelByLevelCopy(getSortCopyPoolProperty(), getSortCopyPoolAscending());
    
    return "copyPool";
  }

  public String sortMovePoolByColumnHeader() {

    String sortString = ContextUtil.lookupParam("movePoolOrderBy");
    String ascending = ContextUtil.lookupParam("movePoolAscending");
    this.setSortMovePoolProperty(sortString);
    this.setSortMovePoolAscending((Boolean.valueOf(ascending)).booleanValue());
    setQpDataModelByLevelCopy(getSortMovePoolProperty(), getSortMovePoolAscending());
    
    return "movePool";
  }

  public String sortSubPoolByColumnHeader() {

    String sortString = ContextUtil.lookupParam("subPoolOrderBy");
    String ascending = ContextUtil.lookupParam("subPoolAscending");
    this.setSortSubPoolProperty(sortString);
    this.setSortSubPoolAscending((Boolean.valueOf(ascending)).booleanValue());
    setSubQpDataModelByLevel();
    
    return "editPool";
  }

  public String doit(){
    return outcome;
  }

  public String cancelImport(){

    ItemAuthorBean itemauthorbean= (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    this.setImportToAuthoring(false);
    itemauthorbean.setItemTypeString("");
    return EDIT_ASSESSMENT;

  }

  public boolean getSelfOrDescendant(){
  // check if currentPool is ancester of tree.getCurrentObject

    QuestionPoolDataBean curPool = getCurrentPool();
    if (tree == null || tree.getCurrentId() == null || curPool == null)
    {
        return false;
    }

    boolean isDescendant = tree.isDescendantOf(tree.getCurrentId(), curPool.getId());
    boolean isSelf = tree.getCurrentId().equals(curPool.getId());

    return isSelf || isDescendant;
  }

  private HashMap buildHash(Collection objects){
    HashMap map = new HashMap();
    Iterator iter = objects.iterator();
    while(iter.hasNext()){
      QuestionPoolDataIfc pool = (QuestionPoolDataIfc) iter.next();
      Long parentPoolId = pool.getParentPoolId();
      ArrayList poolList = (ArrayList)map.get(parentPoolId);
      if (poolList == null){
        poolList = new ArrayList();
        map.put(parentPoolId, poolList);
      }
      poolList.add(pool);
    }
    return map;
  }

  public ArrayList sortPoolByLevel(Long level, Collection objects, String sortProperty, boolean sortAscending){
    HashMap map = buildHash(objects);
 
    for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
    	Map.Entry entry = (Map.Entry) it.next();
    	ArrayList poolList = (ArrayList) entry.getValue();
    	sortQpoolsByProperty(poolList, sortProperty, sortAscending);
    }
    // poolList in each level has been sorted, now we would put them in the right order
    ArrayList sortedList = new ArrayList();
    ArrayList firstLevelPoolList = (ArrayList) map.get(level);
    if (firstLevelPoolList != null)
      addPoolByLevel(sortedList, map, firstLevelPoolList);
    return sortedList;
  }

  private void addPoolByLevel(ArrayList sortedList, HashMap map, ArrayList poolList){
    for (int i=0; i<poolList.size();i++){
      QuestionPoolDataIfc pool = (QuestionPoolDataIfc) poolList.get(i); 
      sortedList.add(pool);
      ArrayList nextLevelPoolList = (ArrayList) map.get(pool.getQuestionPoolId());
      if (nextLevelPoolList !=null)
        addPoolByLevel(sortedList, map, nextLevelPoolList);
    }
  }

  /*
  private void printTree(Collection objects){
    Iterator iter = objects.iterator();
    String stars="********";
    while(iter.hasNext())
    {
      QuestionPoolDataIfc pool = (QuestionPoolDataIfc) iter.next();
      //System.out.println();
      //System.out.println("****** QPBean: "+pool.getTitle()+":"+pool.getLastModified()); 
      printChildrenPool(tree, pool, stars);
    }
  }
  */
  
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public ArrayList getAddedPools()
  {
    return addedPools;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setAddedPools(ArrayList addedPools)
  {
    this.addedPools = addedPools;
  }
  
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public ArrayList getAddedQuestions()
  {
    return addedQuestions;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param pPool DOCUMENTATION PENDING
   */
  public void setAddedQuestions(ArrayList addedQuestions)
  {
    this.addedQuestions = addedQuestions;
  }
  
  private void setPoolInfo(QuestionPoolDataBean pool) {
  	String nameField = ContextUtil.lookupParam("namefield");
	String orgField = ContextUtil.lookupParam("orgfield");
	String descField = ContextUtil.lookupParam("descfield");
	String objField = ContextUtil.lookupParam("objfield");
	String keyField = ContextUtil.lookupParam("keyfield");		
	
	pool.setDisplayName(nameField);
	pool.setOrganizationName(orgField);
	pool.setDescription(descField);
	pool.setObjectives(objField);
	pool.setKeywords(keyField);
  }
  
  	public void setQpDataModelByLevel() {
		// construct the sortedList, pools need to be sorted one level at a time
		// so the hierachical structure can be maintained. Here, we start from root = 0,
		setQpDataModelByLevel(new Long("0"));
	}
  	
  	public void setQpDataModelByLevel(Long poolId) {
		Collection objects = tree.getSortedObjects();

		if (objects != null) {
			ArrayList sortedList = sortPoolByLevel(poolId, objects,
					getSortProperty(), getSortAscending());
			ListDataModel model = new ListDataModel((List) sortedList);
			QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
					model);
			this.qpDataModel = qpDataModel;
		}
	}
  
  	public void setQpDataModelByProperty() {
		tree.sortByProperty(this.getSortProperty(), this.getSortAscending());

		Collection objects = tree.getSortedObjects();
		ListDataModel model = new ListDataModel((List) objects);
		QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
				model);
		this.qpDataModel = qpDataModel;
	}

  	public void setQpDataModelByLevelCopy(String sortProperty, boolean sortAscending) {
  		Collection objects = tree.getSortedObjects();

  		// construct the sortedList, pools need to be sorted one level at a time
  		// so the hierachical structure can be maintained. Here, we start from root = 0,
  		if (objects != null) {
  			ArrayList sortedList = sortPoolByLevel(new Long("0"), objects,
  					sortProperty, sortAscending);
  			ListDataModel model = new ListDataModel((List) sortedList);
  			QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
  					model);
  			this.qpDataModelCopy = qpDataModel;
  		}
  	}

  	public void setQpDataModelByPropertyCopy(String sortProperty, boolean sortAscending) {
  		tree.sortByProperty(sortProperty, sortAscending);

  		Collection objects = tree.getSortedObjects();
  		ListDataModel model = new ListDataModel((List) objects);
  		QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
  				model);
  		this.qpDataModelCopy = qpDataModel;
  	}
  	
  	public void setSubQpDataModelByLevel() {
		ArrayList subpools = (ArrayList) tree.getSortedObjects(getCurrentPool()
				.getId());
		if (subpools != null) {
			ArrayList sortedList = sortPoolByLevel(getCurrentPool().getId(),
					subpools, getSortSubPoolProperty(),
					getSortSubPoolAscending());

			ListDataModel model = new ListDataModel((List) sortedList);
			QuestionPoolDataModel subQpDataModel = new QuestionPoolDataModel(tree,
					model);
			this.subQpDataModel = subQpDataModel;
		}
	}
	
  	public String startPreviewPool()
	{
		String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
		if(qpid!=null) {
			Long qpidL = new Long(qpid);
			
	        QuestionPoolService delegate = new QuestionPoolService();
	        QuestionPoolFacade thepool = delegate.getPool(qpidL, AgentFacade.getAgentString());
	        
	        if(qpidL.compareTo(getCurrentPool().getId())!=0){
	        	notCurrentPool = true;
	        	displayNameNotCPool = thepool.getDisplayName();
			}

			List<ItemFacade> listAllItems = new ArrayList(thepool.getQuestions());
			
			// Creating the itemContentsBean
			ArrayList<ItemContentsBean> list = new ArrayList<>();
			int number=0;
			for (ItemDataIfc item : listAllItems) {
				ItemContentsBean itemBean = new ItemContentsBean(item);
				itemBean.setNumber(++number);
				list.add(itemBean);
			}
			setItemsBean(list);
			  		
			return "previewPool";
		}
		else {
			return "";
		}
	}
  	
  	public List<ItemContentsBean> getItemsBean() {
  		return this.itemsBean;
  	}
  	
  	public void setItemsBean(List<ItemContentsBean> itemsBean) {
  		this.itemsBean = itemsBean;
  	}
  	
  	public String getAgentId()
  	{
  		return AgentFacade.getAgentString();
  	}
  	
  	public String getOwner() {
  		String owner = AgentFacade.getDisplayName(getAgentId());
  		return owner;
  	}
  	
  	
	public String exportPool() {
		String poolId= ContextUtil.lookupParam("poolId");
		log.debug("exporting as Excel: poolid = {}", poolId);

		QuestionPoolService delegate = new QuestionPoolService();
		QuestionPoolFacade qPool =
				delegate.getPool(new Long(poolId), AgentFacade.getAgentString());

		// changed from above by gopalrc - Jan 2008
		// to allow local customization of spreadsheet output
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
		response.reset();	// Eliminate the added-on stuff
		response.setHeader("Pragma", "public");	// Override old-style cache control
		response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
		writeDataToResponse(getSpreadsheetData(poolId), getDownloadFileName(qPool.getDisplayName()), response);
		faces.responseComplete();
	
		return "";
	}
	
	private List<List<Object>> getSpreadsheetData(String poolId) {
		List exportResponsesDataList = getExportResponsesData(poolId);
		List<List<Object>> list = (List<List<Object>>) exportResponsesDataList.get(0);

		// Now insert the header line
		ArrayList<Object> headerList = new ArrayList<>();
		headerList.add(ExportResponsesBean.HEADER_MARKER);
		
		headerList.addAll((ArrayList) exportResponsesDataList.get(1));
		
		list.add(0,headerList);
		
		// gopalrc - Jan 2008 - New Sheet Marker
		ArrayList<Object> newSheetList = new ArrayList<>();
		newSheetList.add(ExportResponsesBean.NEW_SHEET_MARKER);
		newSheetList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages","responses"));
		list.add(0, newSheetList);
		
		return list;
	}

	/**
	 * Generates a default filename (minus the extension) for a download from this Gradebook. 
	 *
	 * @param   prefix for filename
	 * @return The appropriate filename for the export
	 */
	public String getDownloadFileName(String name) {
		Date now = new Date();
		String dateFormat = "yyyyMMddHHmmss";
		DateFormat df = new SimpleDateFormat(dateFormat);
		StringBuilder fileName = new StringBuilder();
		if(StringUtils.isNotEmpty(name)) {
			name = name.replaceAll("\\s", "_"); // replace whitespace with '_'
			fileName.append(name);
		}
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}
    
    
	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		String mimetype = "application/vnd.ms-excel;charset=UTF-8";
		String extension = ".xls";
		int columns = findColumnSize(spreadsheetData);
		if (columns >= 255) {
			// allows for greater than 255 columns - SAK-16560
			mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			extension = ".xlsx";
			log.info("Samigo export ("+columns+" columns): Using xlsx mimetype: " + mimetype);
		}
		response.setContentType(mimetype);
		
		String escapedFilename = org.sakaiproject.util.Validator.escapeUrl(fileName);
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String userAgent = request.getHeader("User-Agent"); 
		if (StringUtils.contains(userAgent, "MSIE")) { 
			response.setHeader("Content-disposition", "attachment; filename=" + escapedFilename + extension);
		}
		else {
			response.setHeader("Content-disposition", "attachment; filename*=utf-8''" + escapedFilename + extension);
		}
		
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(spreadsheetData).write(out);
			out.flush();
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error(e.getMessage());
			}
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * 
	 * @param spreadsheetData
	 * @return
	 */
	protected Workbook getAsWorkbookTest(List<List<Object>> spreadsheetData) {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet();
		
		short rowPos = 0;
		for( List<Object> rowData: spreadsheetData ) {
			short cellPos = 0;
			if (rowPos == 0) {
				// By convention, the first list in the list contains column headers.
				Row headerRow = sheet.createRow(rowPos++);
				for( Object header: rowData ) {
					createCell(headerRow, cellPos++, null).setCellValue(header.toString());
				}
			}
			else {
				Row row = sheet.createRow(rowPos++);
				for ( Object data : rowData ) {
					Cell cell = createCell(row, cellPos++, null);
					if (data != null) {
						if (data instanceof Double) {
							cell.setCellValue(((Double)data).doubleValue());
						} 
						else {
							cell.setCellValue(data.toString());
						}
					}
				}
			}
		}

		return wb;
	}
	
	/**
	 * 
	 * @param spreadsheetData
	 * @return
	 */
	public Workbook getAsWorkbook(List<List<Object>> spreadsheetData) {
		// outer list is rows, inner list is columns (cells in the row)
		int columns = findColumnSize(spreadsheetData);
		Workbook wb = new HSSFWorkbook();
		if (columns < 255) {
			log.info("Samigo export ("+columns+" columns): Using xsl format");
		} else {
			// allows for greater than 255 columns - SAK-16560
			log.info("Samigo export ("+columns+" columns): Using xslx format");
		}

		CellStyle boldStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldStyle.setFont(font);
		CellStyle headerStyle = boldStyle;
		
		Sheet sheet = null;

		short rowPos = 0;
		for ( List<Object>rowData : spreadsheetData ) {
			
			if (ExportResponsesBean.NEW_SHEET_MARKER.equals(rowData.get(0).toString())) {
				sheet = wb.createSheet(rowData.get(1).toString());
				rowPos = 0;
			}
			// By convention, the first list in the list contains column headers.
			// This should only happen once and usually only in a single-sheet workbook
			else if (ExportResponsesBean.HEADER_MARKER.equals(rowData.get(0).toString())) {
				if (sheet == null) {
					sheet = wb.createSheet("responses"); // avoid NPE
				}
				Row headerRow = sheet.createRow(rowPos++);
				short colPos = 0;
				for (Object data : rowData) {
					createCell(headerRow, colPos++, headerStyle).setCellValue(data.toString());
				}
			}
			else {
				if (sheet == null) {
					sheet = wb.createSheet("responses"); // avoid NPE
				}
				Row row = sheet.createRow(rowPos++);
				short colPos = 0;
				for ( Object data : rowData ) {
					Cell cell = null;
					
					if (data != null) {
						if (StringUtils.startsWith(data.toString(), ExportResponsesBean.FORMAT)) {
							if (ExportResponsesBean.FORMAT_BOLD.equals(data)) {
								cell = createCell(row, colPos++, boldStyle);
							}
						}
						else {
							cell = createCell(row, colPos++, null);
						}
						if (data != null) {
							if (data instanceof Double) {
								cell.setCellValue(((Double)data).doubleValue());
							} else {
								// stripping html for export, SAK-17021
								cell.setCellValue(data.toString());
							}
						}
					}
				}
			}
			
		}
		
		return wb;
	}

	private int findColumnSize(List<List<Object>> spreadsheetData) {
		int columns = 0; // the largest number of columns required for a row
		for (List<Object> list : spreadsheetData) {
			if (list != null && list.size() > columns) {
				columns = list.size();
			}
		}
		return columns;
	}
	
	private Cell createCell(Row row, short column, CellStyle cellStyle) {
		Cell cell = row.createCell(column);
		if (cellStyle != null) {
			cell.setCellStyle(cellStyle);
		}
		
		return cell;
	}
	
	public List<Object> getExportResponsesData(String poolId) {
		List<List<Object>> dataList = new ArrayList<>();
		List<Object> headerList = new ArrayList<>();
		List<Object> finalList = new ArrayList<>(2);
	
		Float itemScore = null;
		
		String questionString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_question");
		String responseString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_response");
		String feedbackString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_feedback");
		String keyString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_key");

		// Create Header List
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_text"));
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_type"));
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_points"));
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_discount"));
		headerList.add(responseString.concat(" A"));
		headerList.add(feedbackString.concat(" A"));
		headerList.add(responseString.concat(" B"));
		headerList.add(feedbackString.concat(" B"));
		headerList.add(responseString.concat(" C"));
		headerList.add(feedbackString.concat(" C"));
		headerList.add(responseString.concat(" D"));
		headerList.add(feedbackString.concat(" D"));
		headerList.add(keyString);	  
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_feedbackCorrect"));
		headerList.add(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","q_feedbackIncorrect"));

		// There are 4 answers by default in a multiple choice question 
		final int DEFAULT_MAX_ANSWERS = 4;
		int maxAnswers = DEFAULT_MAX_ANSWERS;
		
		// Get the items of questionpool
		List<ItemContentsBean>itemBeans = this.getItemsBean();
		
		int questionNumber = 1;
		for ( ItemContentsBean itemBean : itemBeans) {
			ItemFacade item = new ItemFacade(itemBean.getItemData());
				
			List<Object> row = new ArrayList<>();
			List<String> answerList = new ArrayList<>();
			List<String> feedbackList = new ArrayList<>();
			List<String> feedbackAnswerList = new ArrayList<>();
		
			// Get the question string
			row.add(questionString + " " + questionNumber++);
			
			// Get the question text
			row.add(FormattedText.convertFormattedTextToPlaintext(item.getData().getText()));
			
			// Get the question type
			row.add(getTypeQuestion(item.getData().getTypeId()));
			
			// Get the points
			row.add(item.getData().getScore());
		
			// Get the discount
			row.add(item.getData().getDiscount());

			StringBuilder contentBuffer = new StringBuilder();
			StringBuilder key = new StringBuilder();
		
			if (TypeIfc.AUDIO_RECORDING.equals(item.getData().getTypeId())) {
				// Key
				key.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","time_allowed_seconds"));
				key.append(" " + item.getData().getDuration() + "\n");
				key.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","number_of_tries"));
				key.append(" " + item.getData().getTriesAllowed());
				
				// Feedback
				if (StringUtils.isNotEmpty(item.getData().getGeneralItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getGeneralItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.FILE_UPLOAD.equals(item.getData().getTypeId())) {
				// Answer
				answerList.add("");
				
				// Feedback
				if (StringUtils.isNotEmpty(item.getData().getGeneralItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getGeneralItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.MULTIPLE_CORRECT.equals(item.getData().getTypeId()) ||
					TypeIfc.MULTIPLE_CHOICE.equals(item.getData().getTypeId()) ||
					TypeIfc.MULTIPLE_CHOICE_SURVEY.equals(item.getData().getTypeId()) ||
					TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.equals(item.getData().getTypeId()) ||
					TypeIfc.TRUE_FALSE.equals(item.getData().getTypeId()) ||
					TypeIfc.MATRIX_CHOICES_SURVEY.equals(item.getData().getTypeId())) {

				// Answer
				for ( ItemTextIfc itemtext : item.getData().getItemTextArraySorted() ) {
					List<AnswerIfc> answers = itemtext.getAnswerArraySorted();

					for ( AnswerIfc answer : answers ) {
						if (answer.getText() == null) {
							break;
						}
												
						contentBuffer.setLength(0);
						String answerText = "";
						if (TypeIfc.MULTIPLE_CHOICE_SURVEY.equals(item.getData().getTypeId())) {
							answerText = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", answer.getText());
						}
						else {
							answerText = FormattedText.convertFormattedTextToPlaintext(answer.getText());
						}
						contentBuffer.append(answerText);
						
						answerList.add(contentBuffer.toString());
						
						contentBuffer.setLength(0);
						if (StringUtils.isNotEmpty(answer.getGeneralAnswerFeedback())) {
							contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(answer.getGeneralAnswerFeedback()));
						}
						feedbackAnswerList.add(contentBuffer.toString());	
					}
					
					// If there are more columns than 4 we will need to add dynamically the others
					if (answers.size() > DEFAULT_MAX_ANSWERS) {
						for (int i=0; i<answers.size()-maxAnswers; i++) {
							int index = headerList.lastIndexOf(keyString);
							headerList.add(index, responseString.concat(" " + (char)('A' + maxAnswers + i)));
							headerList.add(index+1, feedbackString.concat(" " + (char)('A' + maxAnswers + i)));
							for ( List<Object> row2 : dataList) {
								row2.add(index, "");
								row2.add(index+1, "");
							}
						}
						maxAnswers = answers.size();
					}
				}
				
				// Key
				if (!TypeIfc.MULTIPLE_CHOICE_SURVEY.equals(item.getData().getTypeId()) && 
					!TypeIfc.MATRIX_CHOICES_SURVEY.equals(item.getData().getTypeId())) {
					key.append(item.getAnswerKey());
				}
				
				// Feedback
				if (!TypeIfc.MULTIPLE_CHOICE_SURVEY.equals(item.getData().getTypeId()) &&
					!TypeIfc.MATRIX_CHOICES_SURVEY.equals(item.getData().getTypeId())) {
					if (StringUtils.isNotEmpty(item.getData().getCorrectItemFeedback())) {
						contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getCorrectItemFeedback()));
					}
					feedbackList.add(contentBuffer.toString());
					
					contentBuffer.setLength(0);
					if (StringUtils.isNotEmpty(item.getData().getInCorrectItemFeedback())) {
						contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getInCorrectItemFeedback()));
					}
					feedbackList.add(contentBuffer.toString());
				}
				else {
					if (StringUtils.isNotEmpty(item.getData().getGeneralItemFeedback())) {
						contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getGeneralItemFeedback()));
					}
					feedbackList.add(contentBuffer.toString());
				}
			}
			else if (TypeIfc.MATCHING.equals(item.getData().getTypeId())) {
				
				// Answer
				for ( ItemTextIfc matching : item.getData().getItemTextArray() ) {
					contentBuffer.setLength(0);
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(matching.getText()));
					contentBuffer.append("--->");
					
					boolean first = true;
					for ( AnswerIfc answer : matching.getAnswerArraySorted() ) {
						if (answer.getText() == null) {
							break;
						}
						
						if (first) {
							contentBuffer.append(" | ");
							first = false;
						}
						contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(answer.getText()));
					}
					
					answerList.add(contentBuffer.toString());
				}

				// Key
				key.append(item.getAnswerKey());
				
				// Feedback
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
				
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getInCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getInCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.ESSAY_QUESTION.equals(item.getData().getTypeId())) {
				
				ItemTextIfc itemText = item.getData().getItemTextArray().get(0);
				String answerText = "";
				if (itemText.getAnswerArray() != null && itemText.getAnswerArray().size() > 0) {
					AnswerIfc answer = itemText.getAnswerArray().get(0);
					answerText = answer.getText();
				}
				
				// Key
				key.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages","answer_model"));
				key.append("\n");
				key.append(answerText);
				
				// Feedback
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getGeneralItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getGeneralItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.FILL_IN_BLANK.equals(item.getData().getTypeId()) ||
					TypeIfc.FILL_IN_NUMERIC.equals(item.getData().getTypeId())) {
				
				// Key
				ItemTextIfc itemText = item.getData().getItemTextArray().get(0);
				boolean first = true;
				for ( AnswerIfc answer : itemText.getAnswerArray() ) {
					if (answer.getText() == null) {
						break;
					}
					
					if (first) {
						contentBuffer.append(" | ");
						first = false;
					}
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(answer.getText()));
				}
				key.append(contentBuffer.toString());
				
				// Feedback
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
				
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getInCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getInCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.CALCULATED_QUESTION.equals(item.getData().getTypeId())) {
				// key
				
				// Feedback
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
				
				contentBuffer.setLength(0);
				if (StringUtils.isNotEmpty(item.getData().getInCorrectItemFeedback())) {
					contentBuffer.append(FormattedText.convertFormattedTextToPlaintext(item.getData().getInCorrectItemFeedback()));
				}
				feedbackList.add(contentBuffer.toString());
			}
			else if (TypeIfc.EXTENDED_MATCHING_ITEMS.equals(item.getData().getTypeId())) {
				//TODO:
				
			}
			
			// Responses && Feedback
			for (int i=0; i<maxAnswers; i++) {
				if (answerList.size() > i) {
					row.add(answerList.get(i));
					if (feedbackAnswerList.size() > i) {
						row.add(feedbackAnswerList.get(i));
					}
					else {
						row.add("");
					}
				}
				else {
					row.add(""); // blank answer
					row.add(""); // blank feedback answer
				}
			}
			
			// Key
			row.add(key.toString());
			
			// Feedback correct & incorrect
			for (int i=0; i<2; i++) {
				if (feedbackList.size() > i) {
					row.add(feedbackList.get(i));
				}
				else {
					row.add("");
				}
			}
		
			dataList.add(row);
		}
			
		finalList.add(dataList);
		finalList.add(headerList);
		return finalList;
	}

	private String getTypeQuestion(Long typeId) {
		String type = "";
		ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		ResourceLoader rc = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.CommonMessages");
		if (typeId == TypeIfc.MULTIPLE_CHOICE.intValue()) {
			type = rc.getString("multiple_choice_sin");
		}
		if (typeId == TypeIfc.MULTIPLE_CORRECT.intValue()) {
			type = rc.getString("multipl_mc_ms");
		}
		if (typeId == TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.intValue()) {
			type = rc.getString("multipl_mc_ss");
		}
		if (typeId == TypeIfc.MULTIPLE_CHOICE_SURVEY.intValue()) {
			type = rb.getString("q_mult_surv");
		}
		if (typeId == TypeIfc.TRUE_FALSE.intValue()) {
			type = rb.getString("q_tf");
		}
		if (typeId == TypeIfc.ESSAY_QUESTION.intValue()) {
			type = rb.getString("q_short_ess");
		}
		if (typeId == TypeIfc.FILE_UPLOAD.intValue()) {
			type = rb.getString("q_fu");
		}
		if (typeId == TypeIfc.AUDIO_RECORDING.intValue()) {
			type = rb.getString("q_aud");
		}
		if (typeId == TypeIfc.FILL_IN_BLANK.intValue()) {
			type = rb.getString("q_fib");
		}
		if (typeId == TypeIfc.MATCHING.intValue()) {
			type = rb.getString("q_match");
		}
		if (typeId == TypeIfc.FILL_IN_NUMERIC.intValue()) {
			type = rb.getString("q_fin");
		}
		if (typeId == TypeIfc.EXTENDED_MATCHING_ITEMS.intValue()) {
			type = rb.getString("q_emi");
		}
		if (typeId == TypeIfc.MATRIX_CHOICES_SURVEY.intValue()) {
			type = rb.getString("q_matrix_choices_surv");
		}
		if (typeId == TypeIfc.CALCULATED_QUESTION.intValue()) {
			type = rb.getString("q_cq");
		}
		if (typeId == TypeIfc.IMAGEMAP_QUESTION.intValue()) {
			type = rb.getString("q_imq");
		}
		return type;
	}
	
	// **********************************************
	// ****************** SAM-2049 ******************
	// **********************************************
	
	public boolean getSortTransferPoolAscending() {
		return sortTransferPoolAscending;
	}
	
	public void setSortTransferPoolAscending(boolean sspa) {
		sortTransferPoolAscending = sspa;
	}
	  
	public String getSortTransferPoolProperty() {
	    return sortTransferPoolProperty;
	}
	  
	public void setSortTransferPoolProperty(String newProperty) {
	    sortTransferPoolProperty= newProperty;
	}
	
	public String transferPoolConfirmBack() {		
		return "transferPoolInputUser";
	}

	public List<Long> getTransferPools() {
		return transferPools;
	}

	public void setTransferPools(List<Long> transferPools) {
		this.transferPools = transferPools;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public boolean isCheckAll() {
		return checkAll;
	}

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}
  	
  	public QuestionPoolDataModel getTransferQpools() {	
		buildTreeCopy();
		setQpDataModelByLevelTransferPool();
		log.debug("getSelectedQpools");
		return qpDataModelTransfer;
	}
   
  	public QuestionPoolDataModel getTransferSelectedQpools() {
  		buildTreeTransferPool();
  		setQpDataModelByLevelTransferSelectedPools();
  		log.debug("qpDataModelTransferSelected");
  		return qpDataModelTransferSelected;
  	}
  
  	public void buildTreeTransferPool() {
  		try {
  			QuestionPoolService delegate = new QuestionPoolService();
  			List<QuestionPoolFacade> qpList = new ArrayList<QuestionPoolFacade>();
  
  		  	if (transferPools != null ) {
  		  		for (Long poolId : transferPools) {
  		  			QuestionPoolFacade qpFacade =  delegate.getPool(poolId, AgentFacade.getAgentString() );
  
  		  			IdImpl parentId = (IdImpl) qpFacade.getParentId();
  		  			Long parentIdLong = new Long("0");
  
  		  			try {
  					  parentIdLong = Long.valueOf(parentId.getIdString());
  		  			} catch (SharedException e) {
  					  log.warn("error setting pool id to Long." + e.getMessage());
  		  			}
  		  			
  		  			// If just the child pool will transfer but not the parent pool, set this child pool has no parent.
  		  			if (!transferPools.contains(parentIdLong)) {
  		  				IdImpl updateParentId = new IdImpl("0");
  		  				qpFacade.setParentId(updateParentId);
  		  			}
  				  				
  		  			qpList.add(qpFacade);
  		  		}
  		  		QuestionPoolIteratorFacade qpif = new QuestionPoolIteratorFacade(qpList);
  		  		tree = new QuestionPoolTreeImpl(qpif);
  		  	} else {
  			  tree = new QuestionPoolTreeImpl(new QuestionPoolIteratorFacade(new ArrayList<QuestionPoolFacade>()));
  		  	}
  		} catch (Exception e) {
  		  log.warn("error building transfer pool tree." + e.getMessage());
  		  throw new RuntimeException(e);
  	  	}	  
  	}
	
	public String sortTransferPoolByColumnHeader() {
		String sortString = ContextUtil.lookupParam("transferPoolOrderBy");
		String ascending = ContextUtil.lookupParam("transferPoolAscending");
		this.setSortTransferPoolProperty(sortString);
		this.setSortTransferPoolAscending((Boolean.valueOf(ascending)).booleanValue());
		setQpDataModelByLevelTransferPool();
		return "transferPool";
	}
	
	public void setQpDataModelByLevelTransferPool() {
  		Collection<QuestionPoolDataIfc> objects = tree.getSortedObjects();

  		// Construct the sortedList, pools need to be sorted one level at a time
  		// so the hierachical structure can be maintained. Here, we start from root = 0,
  		if (objects != null) {
  			ArrayList<QuestionPoolDataIfc> sortedList = sortPoolByLevel(new Long("0"), objects,
  					getSortTransferPoolProperty(), getSortTransferPoolAscending());
  			ListDataModel model = new ListDataModel((List<QuestionPoolDataIfc>) sortedList);
  			QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
  					model);
  			this.qpDataModelTransfer = qpDataModel;
  		}
  	}

 	public void setQpDataModelByLevelTransferSelectedPools() {
		Collection<QuestionPoolDataIfc> objects = tree.getSortedObjects();

		// Construct the sortedList, pools need to be sorted one level at a time
		// so the hierachical structure can be maintained. Here, we start from root = 0,
		if (objects != null) {
			ArrayList<QuestionPoolDataIfc> sortedList = sortPoolByLevel(new Long("0"), objects,
					"title", true);
			ListDataModel model = new ListDataModel((List<QuestionPoolDataIfc>) sortedList);
			QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
					model);
			this.qpDataModelTransferSelected = qpDataModel;
		}
	}
 	
 	// Click transfer ownership link in main page
	public String transferPool() {
		// Reset transferPools, checkAll checkbox, ownerId
		this.transferPools = null;
		this.checkAll = false;
		this.ownerId = null;
		
		buildTree();
		setQpDataModelByLevelTransferPool();
		return "transferPool";
	}
	
	// Transfer pool tree page click continue button
	public String transferPoolContinue() {
               // Setup selected pools for transfer; if no pools selected, post error message
		String transferPoolIds = ContextUtil.paramValueLike("transferPoolIds");
               if (transferPoolIds == null || transferPoolIds.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(rb.getString("transfer_pool_ids_null_error")));
                    return "transferPool";
               }

		QuestionPoolService delegate = new QuestionPoolService();
		List<Long> poolsWithAccess = delegate.getPoolIdsByAgent(AgentFacade.getAgentString());

		log.debug("transferPoolIds {}", transferPoolIds);
		String[] poolIds = transferPoolIds.split(",");
		List<Long> transferPoolIdLong = new ArrayList<Long>();
		for (int i = 0; i < poolIds.length; i++) {
			if (StringUtils.isNotBlank(poolIds[i])) {
				try {
					Long poolId = new Long(poolIds[i]);
					if (poolsWithAccess.contains(poolId)) {
						transferPoolIdLong.add(poolId);
					}
					else {
						throw new IllegalArgumentException("Cannot transfer pool: userId " + AgentFacade.getAgentString() + " does not have access to pool id " + poolId);
					}
				}
				catch (NumberFormatException e) {
					log.warn("transferPoolContinue: Format Exception, skipping poolId {}. Check javascript passSelectedPoolIds for issues.", poolIds[i]);
				}
			}
		}
		this.transferPools = transferPoolIdLong;	
		
		String checkAllChecked = ContextUtil.paramValueLike("checkAllCheckbox");
		if (checkAllChecked == null || "false".equals(checkAllChecked)) {
			this.checkAll = false;
		} else {
			this.checkAll = true;
		}
		
		return "transferPoolInputUser";
	}
	
	// Transfer pool tree page click cancel button
	public String cancelTransferPool() {
		buildTree();
		setQpDataModelByLevel();
		return "poolList";
	}
	
	// Transfer pool input user id page, click continue button
	public String transferPoolInputUserContinue() {
		String ownerUserId = ContextUtil.paramValueLike("owneruserId");
		
		// Check if the userId is null or ""
		FacesContext context=FacesContext.getCurrentInstance();
		String err;
		if (ownerUserId == null || "".equals(ownerUserId)) {
			err = rb.getString("transfer_pool_userId_null_error");
			context.addMessage(null, new FacesMessage(err));
			return "transferPoolInputUser";
		}
		
		// Check if userId is valid
		try { 
			User user = UserDirectoryService.getUserByEid(ownerUserId);
		} catch (UserNotDefinedException e) {
			log.debug("Unable to get user by eid: " + ownerUserId);
			err = rb.getString("transfer_pool_user_invalid");
			context.addMessage(null, new FacesMessage(err));
			return "transferPoolInputUser";
		}
		
		this.ownerId = ownerUserId;
		return "transferPoolConfirm";
	}
	
	// Transfer pool input user id page, click back button
	public String transferPoolInputUserBack() {
		buildTree();
		setQpDataModelByLevelTransferPool();
		return "transferPool";
	}
	
	public String getConfirmMessage() {
		try {
			User user = UserDirectoryService.getUserByEid(ownerId);
			String userInfo = user.getDisplayName() +  " (" + this.ownerId +  ")";
			confirmMessage = rb.getFormattedMessage("transfer_pool_confirm_owner", new String[] {userInfo});
			return confirmMessage;
		} catch(UserNotDefinedException e) {
			log.warn("Unable to get user by eid: " + ownerId);
			e.printStackTrace();
			return "";
		}
	}
	
	public String transferPoolOwnership() {		
		QuestionPoolService delegate = new QuestionPoolService();
		try {
			// Need to pass userId not eid to transfer pool.
			String userId = UserDirectoryService.getUserId(ownerId);
			delegate.transferPoolsOwnership(userId, transferPools);
			
			// Aggregate pool IDs
			String poolIdString = "";
			String prefix = "";
			for (Long poolId : transferPools) {
				poolIdString += prefix + poolId.toString();
				prefix = ",";
			}
			
			// Post event
			Date now = new Date();	
			User currentUser = UserDirectoryService.getCurrentUser();
			String userEID = "";
			if (currentUser != null) {
				userEID = currentUser.getEid();
			} else {
				userEID = "[current user is null]";
			}
			EventTrackingService.post(EventTrackingService.newEvent("sam.questionpool.transfer", "pool(s) [" + poolIdString + "] transferred " +
					"from " + userEID + " to " + this.ownerId + " on " + now , true));

			buildTree();
			setQpDataModelByLevel();
			return "poolList";
		} catch (UserNotDefinedException e) {
			log.warn("Unable to get user by eid: " + ownerId);
			e.printStackTrace();
			return "";
		}
	}
	
	public void setOutComeTree(String originId){
		if(ORIGIN_TOP.equals(getOutcome())){
	    	  buildTree();
	    	  setQpDataModelByLevel();
	      }else{
	    	  startEditPoolAgain(originId);
		      buildTree();
		      setSubQpDataModelByLevel();
}
	}
	
	public void setOutComeParams(){
		setOutComeParams(null);
	}
	
	public void setOutComeParams(String outcome){
		if(outcome==null){
			setOutcome((String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("outCome"));
		}else{
			setOutcome(outcome);
		}
		setOutcomePool((getCurrentPool()!=null)?getCurrentPool().getId():0);
	}
}

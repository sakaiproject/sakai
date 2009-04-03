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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.questionpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.upload.FormFile;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTreeImpl;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.util.ResourceLoader;

// from navigo


/**
 * This holds question pool information.
 *
 * Used to be org.navigoproject.ui.web.form.questionpool.QuestionPoolForm
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Lydia Li<lydial@stanford.edu>
 * $Id$
 */
public class QuestionPoolBean implements Serializable
{
	
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = 418920360211039758L;

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
  private List itemsToDelete;
  private String[] selectedPools;
  private String[] selectedQuestions;
  private String[] destPools = {  }; // for multibox
  private String[] destItems = {  }; // items to delete
  private String sourcePart = null; // copy all questions from part
  private String destPool=""; // for Move Pool Destination
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

  private static Log log = LogFactory.getLog(QuestionPoolBean.class);


  // for JSF
  private Tree tree;
  private Collection qpools;
  private Collection copyQpools;
  private Collection moveQpools;
  private Collection sortedSubqpools;
  private QuestionPoolDataModel qpDataModel;
  private QuestionPoolDataModel subQpDataModel;

 private String addOrEdit;
  private String outcome;
  private String outcomeEdit;
  private String deletePoolSource;  // either from poolList.jsp , or from editPool.jsp
  private String addPoolSource;  // either from poolList.jsp , or from editPool.jsp

  private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages");
  /**
   * Creates a new QuestionPoolBean object.
   */
  public QuestionPoolBean()
  {
    resetFields();
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
	  log.debug("getCopyQpools()");
	  return qpDataModel;
  }

  public QuestionPoolDataModel getMoveQpools()
  {
	  log.debug("getMoveQpools()");
      return qpDataModel;
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
  
  class titleComparator implements Comparator {
	  public int compare(Object o1, Object o2) {
		  QuestionPoolFacade i1 = (QuestionPoolFacade)o1;
		  QuestionPoolFacade i2 = (QuestionPoolFacade)o2;
		  return i1.getTitle().compareToIgnoreCase(i2.getTitle());
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
		  Collections.sort(sortedList, new titleComparator());
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

/*
 * String result = previewQuestion();
 */
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

  public boolean getSortQuestionAscending()
  {
    return sortQuestionAscending;
  }

  public void setSortQuestionAscending(boolean pstr)
  {
    sortQuestionAscending = pstr;
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

  public String getSortQuestionProperty()
  {
    return sortQuestionProperty;
  }

  public void setSortQuestionProperty(String newProperty)
  {
    sortQuestionProperty= newProperty;
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
	    this.sourcePart = null;
        getCheckedQuestion();
        return "copyPool";
  }

  public String startMoveQuestion()
  {
        getCheckedQuestion();
        return "movePool";
  }

	public String startCopyQuestions() {
		this.sourcePart = null;
		getCheckedQuestions();
		return "copyPool";
	}

	public String startMoveQuestions() {
		getCheckedQuestions();
		return "movePool";
	}
  
     public String moveQuestion() {
		String sourceId = "";
		String destId = "";
		sourceId = this.getCurrentPool().getId().toString();
		ArrayList sourceItemIds = this.getCurrentItemIds();

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
						delegate.moveItemToPool(sourceItemId,
								new Long(sourceId), new Long(destId));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		buildTree();
		setQpDataModelByLevel();
		
		return "poolList";
	}

    // This is the link in edit assessment to copy all questions to a pool
	// in order to use copyPool, we need to set up a valid pool context.
	// that's what most of this is. The only actual work is setting sourcePart
	public String startCopyFromAssessment() {
		System.out.println("startcopyfromassessment");

		// find the first pool, and set it up
		QuestionPoolService delegate = new QuestionPoolService();
		ArrayList pools = delegate.getBasicInfoOfAllPools(AgentFacade
				.getAgentString());
		Iterator iter = pools.iterator();

		if (iter.hasNext()) {
			// first pool, if there is one
			QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
			String poolId = pool.getQuestionPoolId().toString();
			buildTree();
			startEditPoolAgain(poolId);
			setActionType("item");
			this.sourcePart = ContextUtil.lookupParam("sectionId");
			return "copyPool";
		}
		
        FacesContext context=FacesContext.getCurrentInstance();
        String err;
        err=rb.getString("no_pools_error");
        context.addMessage(null, new FacesMessage(err));

		return "editAssessment";
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
							String copyItemFacadeId = questionPoolService
									.copyItemFacade(sourceItem.getData())
									.toString();
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

		buildTree();
		setQpDataModelByLevel();
		return "poolList";
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
			            String sourceItemId = delegate.copyItemFacade(sourceItem).toString();
		                delegate.addItemToPool(sourceItemId, new Long(destId));
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
		
		return "editAssessment";
	}

 public String removeQuestionsFromPool(){
     String sourceId = this.getCurrentPool().getId().toString();

     List itemlist = this.getItemsToDelete() == null ? new ArrayList() : this.getItemsToDelete();

     Iterator iter = itemlist.iterator();
     while(iter.hasNext())
     {

       ItemFacade itemfacade = (ItemFacade) iter.next();
       String itemid = itemfacade.getItemIdString();
       QuestionPoolService delegate = new QuestionPoolService();
       delegate.removeQuestionFromPool(itemid, new Long(sourceId));

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
	getCheckedPool();
	setActionType("pool");
	setQpDataModelByProperty();
	return "copyPool";
  }

  public String startMovePool()
  {
	log.debug("inside startMovePool()");  
	getCheckedPool();
	setActionType("pool");
	setQpDataModelByProperty();
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
		    isUnique=delegate.poolIsUnique("0",currentName,destId, AgentFacade.getAgentString());
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

      buildTree();
      setQpDataModelByLevel();
      return "poolList";

  }


  public String movePool(){
     String sourceId = "";
      String destId = "";
      sourceId = this.getCurrentPool().getId().toString();
      String currentName=this.getCurrentPool().getDisplayName();
      boolean isUnique=true;
	destId= ContextUtil.lookupParam("movePool:selectedRadioBtn");

      // added check for "".equals(destId) SAK-4435 , when no dest is selected
        if((sourceId != null) && (destId != null) && (!"".equals(destId)))
        {
          try
          {
            QuestionPoolService delegate = new QuestionPoolService();
 isUnique=delegate.poolIsUnique("0",currentName,destId, AgentFacade.getAgentString());
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

      buildTree();
      setQpDataModelByLevel();
      
	return "poolList";
  }

  public String addPool() {
		String addsource = "poollist";
		addsource = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("addsource");
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




  public String confirmRemovePool(){
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

  public String importPool(){
	return "importPool";
  }

  public String importQuestion(){
	return "importQuestion";
  }

  public String exportPool(){
	return "exportPool";
  }

  public String exportQuestion(){
	return "exportQuestion";
  }

  public String returnToAuthoring(){
	return "author";
  }

  public String editPool(){
 	startEditPool();
    this.setAddOrEdit("edit");
    setSubQpDataModelByLevel();
    
	return "editPool";
  }


  public void startEditPool()
  {
	String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
  	startEditPoolAgain(qpid);
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
          QuestionPoolFacade thepool =
            delegate.getPool(
              new Long(qpid),
              AgentFacade.getAgentString());
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

      //    pool.setProperties((QuestionPoolData) thepool.getData());
//          pool.setNumberOfSubpools(
//            new Integer(tree.getChildList(thepool.getQuestionPoolId()).size()).toString());
          pool.setNumberOfSubpools(thepool.getSubPoolSize().toString());
          pool.setNumberOfQuestions(thepool.getQuestionSize().toString());
          pool.setDateCreated(thepool.getDateCreated());

          Collection objects = tree.getSortedObjects(thepool.getQuestionPoolId());
	  this.setSortedSubqpools(objects);

      pool.setLastModified(new Date());
      this.setCurrentPool(pool);
      this.setHtmlIdLevel(htmlIdLevel);

      ArrayList list = delegate.getAllItemsSorted(this.getCurrentPool().getId(), this.getSortQuestionProperty(), String.valueOf(this.getSortQuestionAscending()));
      this.setAllItems(list);

    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }


  public String selectQuestionType() {
		ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
		String poolid = ContextUtil.lookupParam("poolId");
		if (poolid != null) {
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
    setQpDataModelByLevel();
    
    return "copyPool";
  }

  public String sortMovePoolByColumnHeader() {

    String sortString = ContextUtil.lookupParam("movePoolOrderBy");
    String ascending = ContextUtil.lookupParam("movePoolAscending");
    this.setSortMovePoolProperty(sortString);
    this.setSortMovePoolAscending((Boolean.valueOf(ascending)).booleanValue());
    setQpDataModelByLevel();
    
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

 public void previewQuestion(ActionEvent event){

        ItemService delegate = new ItemService();
        String itemId= ContextUtil.lookupParam("itemid");

        ItemFacade itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        ArrayList itemFacades = new ArrayList();
        itemFacades.add(itemf);
        setCurrentItems(itemFacades);
  }

 public String previewQuestion(){

        ItemService delegate = new ItemService();
        String itemId= ContextUtil.lookupParam("itemid");

        ItemFacade itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        ArrayList itemFacades = new ArrayList();
        itemFacades.add(itemf);
        setCurrentItems(itemFacades);

    return "previewItem";
  }


  public String cancelImport(){

    ItemAuthorBean itemauthorbean= (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    this.setImportToAuthoring(false);
    itemauthorbean.setItemTypeString("");
    return "editAssessment";

  }

  public boolean getSelfOrDescendant(){
  // check if currentPool is ancester of tree.getCurrentObject
    boolean isDescendant  = false;
    boolean isSelf= false;

    isDescendant = tree.isDescendantOf(tree.getCurrentId(), this.getCurrentPool().getId());
    if (tree.getCurrentId().equals(this.getCurrentPool().getId())){
      isSelf= true;
    }
    if (isSelf || isDescendant) {
      return true ;
    }
    else {
      return false;
    }

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
		Collection objects = tree.getSortedObjects();

		// construct the sortedList, pools need to be sorted one level at a time
		// so the hierachical structure can be maintained. Here, we start from root = 0,
		if (objects != null) {
			ArrayList sortedList = sortPoolByLevel(new Long("0"), objects,
					getSortProperty(), getSortAscending());
			ListDataModel model = new ListDataModel((List) sortedList);
			QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
					model);
			this.qpDataModel = qpDataModel;
		}
	}
  
  	public void setQpDataModelByProperty() {
		tree.sortByProperty(this.getSortCopyPoolProperty(), this
				.getSortCopyPoolAscending());

		Collection objects = tree.getSortedObjects();
		ListDataModel model = new ListDataModel((List) objects);
		QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree,
				model);
		this.qpDataModel = qpDataModel;
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
  	
  	public String getAgentId()
  	{
  		return AgentFacade.getAgentString();
  	}
  	
  	public String getOwner() {
  		String owner = AgentFacade.getDisplayName(getAgentId());
  		return owner;
  	}
}

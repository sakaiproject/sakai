
/*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
*/

package org.sakaiproject.tool.assessment.ui.bean.questionpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.upload.FormFile;
import org.sakaiproject.tool.assessment.business.AAMTree;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTreeImpl;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

// from navigo


/**
 * This holds question pool information.
 *
 * Used to be org.navigoproject.ui.web.form.questionpool.QuestionPoolForm
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @author Lydia Li<lydial@stanford.edu>
 * $Id: QuestionPoolBean.java,v 1.49 2005/05/31 19:14:29 janderse.umich.edu Exp $
 */
public class QuestionPoolBean
  implements Serializable
{
  private String name;
  private Collection pools;
  private QuestionPoolDataBean currentPool;
  private QuestionPoolDataBean parentPool;

  private String currentItemId;
  private ItemFacade currentItem;

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

  private ItemFacade itemToPreview;

  private static Log log = LogFactory.getLog(QuestionPoolBean.class);


  // for JSF
  private AAMTree tree;
  private Collection qpools;
  private Collection copyQpools;
  private Collection moveQpools;
  private Collection sortedSubqpools;
  // private QuestionPoolDataModel qpDataModel;


  private String outcome;
  private String deletePoolSource;  // either from poolList.jsp , or from editPool.jsp
  private String addPoolSource;  // either from poolList.jsp , or from editPool.jsp
 
  /**
   * Creates a new QuestionPoolBean object.
   */
  public QuestionPoolBean()
  {
    resetFields();
  }
 
  public QuestionPoolDataModel getQpools()
  {
     
	buildTree();
	//System.out.println("lydiatest inside getQpools, this.sortProperty = " + this.sortProperty);
        //if ((this.sortProperty!=null) && (!this.sortProperty.equals("lastModified"))) {
	//System.out.println("lydiatest inside getQpools, not sort by lastModified, call tree.sortByProperty " );
 	  tree.sortByProperty(this.getSortProperty(),this.getSortAscending());
	//}
        Collection objects = tree.getSortedObjects(); 
        ListDataModel model = new ListDataModel((List) objects);
        QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree, model); 
	return qpDataModel;
  }

  public QuestionPoolDataModel getCopyQpools()
  {
      if (tree == null) 
      {
        buildTree();
      }
       	tree.sortByProperty(this.getSortCopyPoolProperty(),this.getSortCopyPoolAscending());
	 
        Collection objects = tree.getSortedObjects(); 
        ListDataModel model = new ListDataModel((List) objects);
        QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree, model); 
	return qpDataModel;
  }

  public QuestionPoolDataModel getMoveQpools()
  {
      if (tree == null)
      {
	buildTree();
      }
        tree.sortByProperty(this.getSortMovePoolProperty(),this.getSortMovePoolAscending());
   
        Collection objects = tree.getSortedObjects(); 
        ListDataModel model = new ListDataModel((List) objects);
        QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree, model); 
	return qpDataModel;
  }

  public QuestionPoolDataModel getSortedSubqpools()
  {

      this.sortSubqpoolsByProperty((ArrayList)sortedSubqpools,this.getSortSubPoolProperty(),this.getSortSubPoolAscending());
      ListDataModel model = new ListDataModel((List) sortedSubqpools);
      QuestionPoolDataModel qpDataModel = new QuestionPoolDataModel(tree, model); 
      return qpDataModel;
  }

  public void sortSubqpoolsByProperty(ArrayList sortedList, String sortProperty, boolean sortAscending)
  {
        BeanSort sort = new BeanSort(sortedList, sortProperty);

        if (sortProperty.equals("lastModified"))
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

  public void setAllItems(ArrayList list) {
      allItems = list;

  }


  public ArrayList getAllItems()
  {
      QuestionPoolService delegate = new QuestionPoolService();
      ArrayList list = delegate.getAllItems(this.getCurrentPool().getId());
      return allItems;
  }

  public void setSortedSubqpools(Collection spools)
  {
	sortedSubqpools = spools;

  } 


  // This builds the tree.
  public void buildTree()
  {
//          System.out.println("lydiatest in buildTree: " );
    try
    {
      QuestionPoolService delegate = new QuestionPoolService();
      tree=
        new QuestionPoolTreeImpl(
          (QuestionPoolIteratorFacade) delegate.getAllPools(AgentFacade.getAgentString()));

      /*** debug code ***/
/*
      Collection objects = tree.getSortedObjects();
      Iterator iter = objects.iterator();
      while(iter.hasNext())
      {
        try
        {
          QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
	tree.setCurrentId(pool.getQuestionPoolId());
	// System.out.println("lydiatest pool "  + pool.getDisplayName());
        }
        catch(Exception e)
        {
          e.printStackTrace();
          throw new Error(e);
        }
      }
*/
      /*** end debug code ***/
    }
    catch(Exception e)
    {
      throw new Error(e);
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
          throw new Error(e);
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
          throw new Error(e);
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
  public AAMTree getTree()
  {
    return tree;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newPools DOCUMENTATION PENDING
   */
  public void setTree(AAMTree newtree)
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


  public String getCurrentItemId()
  {
        return currentItemId;
  }
  public void setCurrentItemId(String pstr)
  {
    currentItemId= pstr;
  }

  public ItemFacade getCurrentItem()
  {
        return currentItem;
  }
  public void setCurrentItem(ItemFacade param)
  {
    currentItem = param ;
  }

  public ItemFacade getItemToPreview()
  {

/*
   System.out.println("lydiatest in QuestionPoolBean:getItemToPreview()");

   String result =  previewQuestion();
   System.out.println("lydiatest in getItemToPreview preview is " + currentItem.getItemId());

        
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
        getCheckedQuestion();
        return "copyPool";
  }

  public String startMoveQuestion()
  {
        getCheckedQuestion();
        return "movePool";
  }


  public String moveQuestion(){
        //System.out.println("lydiatest in moveQuestion");
     String sourceId = "";
      String destId = "";
      sourceId = this.getCurrentPool().getId().toString();
      String sourceItemId = this.getCurrentItemId();

        //System.out.println("lydiatest in movePool source pool id = "+ sourceId);
        //System.out.println("lydiatest in movePool source item id = "+ sourceItemId);

        destId= ContextUtil.lookupParam("movePool:selectedRadioBtn");

        //System.out.println("lydiatest in movePool 2  dest pool Id = "+ destId);
        if((sourceId != null) && (destId != null) && (sourceItemId !=null))
        {
          try
          {
            QuestionPoolService delegate = new QuestionPoolService();
            delegate.moveItemToPool(sourceItemId, new Long(sourceId), new Long(destId));
          }
          catch(Exception e)
          {
            e.printStackTrace();
                throw new Error(e);
          }
        }

      buildTree();

    //System.out.println("lydiatest END movepool");
        return "poolList";

  }

  public String copyQuestion(){

    //System.out.println("lydiatest inside copyQuestion ");
      Long sourceId = new Long(0);
      String destId= "";
      String sourceItemId = this.getCurrentItemId();
    
        ArrayList destpools= ContextUtil.paramArrayValueLike("checkboxes");
        sourceId = this.getCurrentPool().getId();
    //System.out.println("lydiatest inside copyPool  src = " + sourceId);
    //System.out.println("lydiatest inside copyquestion srcitemid  = " + sourceItemId);

        Iterator iter = destpools.iterator();
      while(iter.hasNext())
      {

          destId = (String) iter.next();
          if((sourceItemId != null) && (destId != null))
          {
            try
            {
    //System.out.println("lydiatest inside copyQuestion copy to dest : " + destId);
              QuestionPoolService delegate = new QuestionPoolService();
                delegate.addItemToPool(sourceItemId, new Long(destId));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Error(e);
            }
          }
        }

      buildTree();
        //System.out.println("lydiatest DONE copyQuestion ");
      return "poolList";


  }


/*
// use listener instead
public String startRemoveQuestions(){
// used by the editPool.jsp, to remove one or more items
    //System.out.println("lydiatest in startRemoveQuestions");
      String itemId= "";
   
        ArrayList destItems= ContextUtil.paramArrayValueLike("removeCheckbox");

        if (destItems.size() > 0) {
		// only go to remove confirmatin page when at least one  checkbox is checked

        List items= new ArrayList();
        Iterator iter = destItems.iterator();
      while(iter.hasNext())
      {

        itemId = (String) iter.next();

        ItemService delegate = new ItemService();
        ItemFacade itemfacade= delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        items.add(itemfacade);

        }

        this.setItemsToDelete(items);
        return "removeQuestionFromPool";
        }
	else {
	 // otherwise go to poollist
          return "poolList";
	}
  }

*/




 public String removeQuestionsFromPool(){
     //System.out.println("lydiatest in removeQuestionFromPool");
     String sourceId = this.getCurrentPool().getId().toString();

     List itemlist = this.getItemsToDelete();

     Iterator iter = itemlist.iterator();
     while(iter.hasNext())
     {

       ItemFacade itemfacade = (ItemFacade) iter.next();
       String itemid = itemfacade.getItemIdString();
       QuestionPoolService delegate = new QuestionPoolService();
       delegate.removeQuestionFromPool(itemid, new Long(sourceId));
       //System.out.println("lydiatest removed " + itemid);
	

       // check to see if any pools are linked to this item 
       ArrayList poollist = (ArrayList) delegate.getPoolIdsByItem(itemfacade.getItemIdString());
       //System.out.println("lydiatest are there any other pool reference to this " + itemid);
       if (poollist.isEmpty()) {
       //System.out.println("lydiatest NO  other pool reference to this " + itemid);

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
       	    //System.out.println("lydiatest Set count = " + itemmetadataSet.size());
       	    ItemMetaData metaTodelete= null;
            Iterator metaiter = itemmetadataSet.iterator();
     	      while (metaiter.hasNext()){
       		ItemMetaData meta= (ItemMetaData) metaiter.next();
       		if (meta.getLabel().equals(ItemMetaData.POOLID)){
       		  //System.out.println("lydiatest found , deleting ");
       		  metaTodelete= meta;
        	}
              }
              if (metaTodelete!=null) {
                itemmetadataSet.remove(metaTodelete);
              }
       	    //System.out.println("lydiatest now after removing poolid Set count = " + itemmetadataSet.size());
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
	setCurrentItemId(itemId);
	setCurrentItem(itemfacade);
	setActionType("item");
  }



// Pool level actions
  public String startCopyPool()
  {
	getCheckedPool();	
	setActionType("pool");
	return "copyPool";
  }

  public String startMovePool()
  {
	getCheckedPool();	
	setActionType("pool");
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
          pool.setOwner(thepool.getOwnerId());
          pool.setObjectives(thepool.getObjectives());
          pool.setKeywords(thepool.getKeywords());
          pool.setOrganizationName(thepool.getOrganizationName());
//          pool.setProperties((QuestionPoolData) thepool.getData());
// TODO  which one should I use?
//          pool.setNumberOfSubpools(
//            new Integer(tree.getChildList(thepool.getQuestionPoolId()).size()).toString());
          pool.setNumberOfSubpools(thepool.getSubPoolSize().toString());
          pool.setNumberOfQuestions(thepool.getQuestionSize().toString());

	
      pool.setLastModified(new Date());

      this.setCurrentPool(pool);
    //System.out.println("lydiatest getchecked pool , pool id = " + this.getCurrentPool().getId());
    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new Error(e);
    }

  }


  public String copyPool(){

    //System.out.println("lydiatest inside copyPool ");
      Long sourceId = new Long(0); 
      String destId= ""; 
     
	ArrayList destpools= ContextUtil.paramArrayValueLike("checkboxes");
 	sourceId = this.getCurrentPool().getId();
    //System.out.println("lydiatest inside copyPool  src = " + sourceId);
	
	Iterator iter = destpools.iterator();
      while(iter.hasNext())
      {

          destId = (String) iter.next();
          if((sourceId.longValue() != 0) && (destId != null))
          {
            try
            {
    //System.out.println("lydiatest inside copyPool  copy to dest : " + destId);
              QuestionPoolService delegate = new QuestionPoolService();
		// TODO Uncomment the line below to test copyPool, 
		delegate.copyPool(tree, AgentFacade.getAgentString(), 
                  sourceId, new Long(destId));
            }
            catch(Exception e)
            {
		e.printStackTrace();
		throw new Error(e);
            }
          }
        }

      buildTree();
    	//System.out.println("lydiatest DONE copyPool");
      return "poolList";

  }

 
  public String movePool(){
	//System.out.println("lydiatest in movePool");
     String sourceId = "";
      String destId = "";
      sourceId = this.getCurrentPool().getId().toString();

	//System.out.println("lydiatest in movePool source id = "+ sourceId);

	destId= ContextUtil.lookupParam("movePool:selectedRadioBtn");

	//System.out.println("lydiatest in movePool 2  destId = "+ destId);
        if((sourceId != null) && (destId != null))
        {
          try
          {
            QuestionPoolService delegate = new QuestionPoolService();
            delegate.movePool(
              AgentFacade.getAgentString(),
              new Long(sourceId), new Long(destId));
          }
          catch(Exception e)
          {
            e.printStackTrace();
		throw new Error(e);
          }
        }

      buildTree();

    //System.out.println("lydiatest END movepool");
	return "poolList";
  }

  public String addPool(){
    //System.out.println("lydiatest action addPool");
          String addsource = "poollist";
          addsource = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("addsource");
	this.setAddPoolSource(addsource);
 	startCreatePool();

	return "addPool";
  }

  public void startCreatePool()
  {
   try{
    //System.out.println("lydiatest in startCreatePool");
     QuestionPoolDataBean pool = new QuestionPoolDataBean();
      int htmlIdLevel = 0;


 // create a new pool with 2 properties: owner and parentpool
 	pool.setOwner(AgentFacade.getAgentString());


          String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
    //System.out.println("lydiatest qpid = " + qpid);
	if((qpid != "0") && (qpid !=null))
// qpid = 0 if creating a new pool at root level 
          {
    //System.out.println("lydiatest not null qpid = " + qpid);
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
      throw new Error(e);
    }
  }




  public String confirmRemovePool(){
// used by the editpool.jsp to remove one subpool at a time
    //System.out.println("lydiatest in confirm remove pool");
	this.setDeletePoolSource("editpool");

ItemAuthorBean itemauthorbean= (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
String poolId = ContextUtil.lookupParam("qpid");

//System.out.println("lydiatest deleting.. "  + poolId);

// we are add one element to the arrayList so we can reuse the deletePool() method. deletePool method expects an arrayList of pools to be deleted.  It is used by the poolList.jsp to delete multiple pools. 

    	List qpools = new ArrayList();
        QuestionPoolService delegate = new QuestionPoolService();
        QuestionPoolFacade qPool =
            delegate.getPool(new Long(poolId), AgentFacade.getAgentString());
        qpools.add(qPool);

        this.setPoolsToDelete(qpools);
        return "removePool";
  }

  public String startRemovePool(){
// used by the poolList.jsp
    //System.out.println("lydiatest in startRemovePool");
	this.setDeletePoolSource("poollist");
      String poolId= "";
    
        ArrayList destpools= ContextUtil.paramArrayValueLike("removeCheckbox");

    	List qpools = new ArrayList();
        Iterator iter = destpools.iterator();
      while(iter.hasNext())
      {

        poolId = (String) iter.next();
//System.out.println("lydiatest deleting.. "  + poolId);

        QuestionPoolService delegate = new QuestionPoolService();
        QuestionPoolFacade qPool =
            delegate.getPool(new Long(poolId), AgentFacade.getAgentString());
        qpools.add(qPool);

        }

	this.setPoolsToDelete(qpools);
	return "removePool";
  }

  public String removePool(){
    //System.out.println("lydiatest in removepool");

        QuestionPoolService delegate = new QuestionPoolService();
        List qpools = this.getPoolsToDelete();
        Iterator iter = qpools.iterator();
      while(iter.hasNext())
      {

        QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
	Long poolId= pool.getQuestionPoolId();
//System.out.println("lydiatest deleting.. "  + poolId);

        delegate.deletePool(poolId, AgentFacade.getAgentString(), tree);
        }
	buildTree();

	if (this.getDeletePoolSource().equals("editpool")) {
    // #1a - so reset subpools tree 
	Collection objects = tree.getSortedObjects(this.getCurrentPool().getId());
          this.setSortedSubqpools(objects);
	  QuestionPoolFacade thepool= delegate.getPool(this.getCurrentPool().getId(), AgentFacade.getAgentString());
          this.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());

	  return "editPool";
	}
	else {
	  return "poolList";
	}
  }

  public String importPool(){
    //System.out.println("action importPool");
	return "importPool";
  }

  public String importQuestion(){
    //System.out.println("action importQuestion");
	return "importQuestion";
  }

  public String exportPool(){
    //System.out.println("action exportPool");
	return "exportPool";
  }

  public String exportQuestion(){
    //System.out.println("action exportQuestion");
	return "exportQuestion";
  }

  public String returnToAuthoring(){
    //System.out.println("action authoring");
	return "author";
  }

/*
// use the Listener instead, because for edit pool, i need to do savePool and remove subpool when 'update' button is clicked 
  public String savePool(){
// save newly created pool
    //System.out.println("lydiatest action savePool");

    try
    {

      QuestionPoolDataBean bean = this.getCurrentPool();
      Long beanid = new Long ("0");
      if(bean.getId() != null)
      {
        beanid = bean.getId();
      }

      Long parentid = new Long("0");
      if(bean.getParentPoolId() != null)
      {
        parentid = bean.getParentPoolId();
      }

      QuestionPoolFacade questionpool =
        new QuestionPoolFacade (beanid, parentid);
      questionpool.updateDisplayName(bean.getDisplayName());
      questionpool.updateDescription(bean.getDescription());
//System.out.println("lydiatest orgname " + bean.getOrganizationName() );
      questionpool.setOrganizationName(bean.getOrganizationName());
//System.out.println("lydiatest orgname " + bean.getObjectives() );
//System.out.println("lydiatest orgname " + bean.getKeywords() );
      questionpool.setObjectives(bean.getObjectives());
      questionpool.setKeywords(bean.getKeywords());
// need to set owner and accesstype
//owner is hardcoded for now 
      questionpool.setOwnerId(AgentFacade.getAgentString());
      questionpool.setAccessTypeId(QuestionPoolFacade.ACCESS_DENIED); // set as default


      QuestionPoolService delegate = new QuestionPoolService();
      //System.out.println("Saving pool");
      delegate.savePool(questionpool);

      // Rebuild the tree with the new pool
      buildTree();
      this.setCurrentPool(null);
    }
    catch(Exception e)
    {
      throw new Error(e);
    }


    //System.out.println("lydiatest savepool done");


	return "poolList";
  }


*/ 

  public String editPool(){
    //System.out.println("lydiatest action editPool");
 	startEditPool();
	return "editPool";
  }


  public void startEditPool()
  {
	String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
    //System.out.println("lydiatest qpid = " + qpid);
  	startEditPoolAgain(qpid);
  }


  public void startEditPoolAgain(String qpid)
  {
   try{
    //System.out.println("lydiatest in startEditPool Again");
	QuestionPoolDataBean pool = new QuestionPoolDataBean();
	int htmlIdLevel = 0;
	ArrayList allparentPools = new ArrayList();

/*
	String qpid = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("qpid");
    //System.out.println("lydiatest qpid = " + qpid);
*/

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
    //System.out.println("lydiatest pool.getparentpoolid = " + ppoolid);
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

//System.out.println("lydiatest editpool parentppoolsize = " + allparentPools.size());
	 this.setParentPoolSize(allparentPools.size());
          String htmlID = tree.getCurrentObjectHTMLId();

          // pass the htmlIdLevel to the collapseRowByLevel javascript
          String[] result = htmlID.split("-");
          htmlIdLevel = result.length + 1;

          pool.setDisplayName(thepool.getDisplayName());
    //System.out.println("lydiatest pool.title = " + pool.getDisplayName());
          pool.setParentPoolId(thepool.getParentPoolId());
    //System.out.println("lydiatest pool.title = " + pool.getParentPoolId());
          pool.setDescription(thepool.getDescription());
    //System.out.println("lydiatest pool.title = " + pool.getDescription());
          pool.setOwner(thepool.getOwnerId());
          pool.setObjectives(thepool.getObjectives());
          pool.setOrganizationName(thepool.getOrganizationName());
          pool.setKeywords(thepool.getKeywords());

      //    pool.setProperties((QuestionPoolData) thepool.getData());
//          pool.setNumberOfSubpools(
//            new Integer(tree.getChildList(thepool.getQuestionPoolId()).size()).toString());
          pool.setNumberOfSubpools(thepool.getSubPoolSize().toString());
          pool.setNumberOfQuestions(thepool.getQuestionSize().toString());

    //System.out.println("lydiatest pool.numberofsubpool = " + pool.getNumberOfSubpools());
    //System.out.println("lydiatest pool.quesitonsize = " + pool.getNumberOfQuestions());

          Collection objects = tree.getSortedObjects(thepool.getQuestionPoolId());
	  this.setSortedSubqpools(objects);

      pool.setLastModified(new Date());
      this.setCurrentPool(pool);
      this.setHtmlIdLevel(htmlIdLevel);

      ArrayList list = delegate.getAllItems(this.getCurrentPool().getId());
      this.setAllItems(list);

    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new Error(e);
    }
  }


  public String selectQuestionType() {
    ItemAuthorBean itemauthorbean= (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
String poolid = ContextUtil.lookupParam("poolId");
    //System.out.println("lydiatest lookup param poolId : " + poolid);
     if(poolid!=null) {
       itemauthorbean.setQpoolId(poolid);
       itemauthorbean.setTarget(ItemAuthorBean.FROM_QUESTIONPOOL);
    //System.out.println("lydiatest we are adding to pool : " + itemauthorbean.getQpoolId());

      itemauthorbean.setItemType("");
      itemauthorbean.setItemTypeString("");
        return "selectQuestionType";
     }
     else {
        return "editPool";  // should not come to this
     }
  }

  public String sortByColumnHeader() {
    //System.out.println("lydiatest in sortByColumnHeader : " );
    String sortString = ContextUtil.lookupParam("orderBy");
    String ascending = ContextUtil.lookupParam("ascending");
    this.setSortProperty(sortString);
    this.setSortAscending((Boolean.valueOf(ascending)).booleanValue());
    //System.out.println("lydiatest we are sorting by column : " + this.getSortProperty());
    return "poolList";
  }
 
  public String sortCopyPoolByColumnHeader() {
    
    String sortString = ContextUtil.lookupParam("copyPoolOrderBy");
    String ascending = ContextUtil.lookupParam("copyPoolAscending");
    this.setSortCopyPoolProperty(sortString);
    this.setSortCopyPoolAscending((Boolean.valueOf(ascending)).booleanValue());
    
    return "copyPool";
  }

  public String sortMovePoolByColumnHeader() {
    
    String sortString = ContextUtil.lookupParam("movePoolOrderBy");
    String ascending = ContextUtil.lookupParam("movePoolAscending");
    this.setSortMovePoolProperty(sortString);
    this.setSortMovePoolAscending((Boolean.valueOf(ascending)).booleanValue());
    
    return "movePool";
  }

  public String sortSubPoolByColumnHeader() {
    
    String sortString = ContextUtil.lookupParam("subPoolOrderBy");
    String ascending = ContextUtil.lookupParam("subPoolAscending");
    this.setSortSubPoolProperty(sortString);
    this.setSortSubPoolAscending((Boolean.valueOf(ascending)).booleanValue());
    
    return "editPool";
  }

  public String doit(){
    return outcome;
  }

 public void previewQuestion(ActionEvent event){
    //System.out.println("lydiatest in QuestionPoolBean:previewQuestion()");

        ItemService delegate = new ItemService();
        String itemId= ContextUtil.lookupParam("itemid");
    //System.out.println("lydiatest itemId to preview is " + itemId);

        ItemFacade itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        setCurrentItem(itemf);
    //System.out.println("lydiatest itemf preview is " + itemf.getItemId());

  }


 public String previewQuestion(){
    //System.out.println("lydiatest in QuestionPoolBean:previewQuestion()");

        ItemService delegate = new ItemService();
        String itemId= ContextUtil.lookupParam("itemid");
    //System.out.println("lydiatest itemId to preview is " + itemId);

        ItemFacade itemf = delegate.getItem(new Long(itemId), AgentFacade.getAgentString());
        setCurrentItem(itemf);
    //System.out.println("lydiatest itemf preview is " + itemf.getItemId());

    return "previewItem";
  }


  public String cancelImport(){

    this.setImportToAuthoring(false);
    return "editAssessment";

  }

}

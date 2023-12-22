/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.business.questionpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.osid.shared.SharedException;

import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.TagIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolIteratorFacade;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
@Slf4j
public class QuestionPoolTreeImpl
  implements Tree
{

  /**
	 * 
	 */
	private static final long serialVersionUID = 2173986944623441011L;

  private Map<String, QuestionPoolFacade> poolMap;
  private Map<String, List<Long>> poolFamilies;
  private Long currentPoolId;
  private String currentObjectHTMLId;
  private String currentLevel;
  private String sortString = "lastModified";
  private Set<Long> filteredIds;

  public QuestionPoolTreeImpl()
  {
    poolMap = new HashMap<>();
    poolFamilies = new HashMap();
  }

  /**
   * Constucts the representation of the tree of pools.
   * @param iter QuestionPoolIteratorFacade for the pools in question
   */
  public QuestionPoolTreeImpl(QuestionPoolIteratorFacade iter)
  {
    // this is a table of pools by Id
    poolMap = new HashMap<>();

    // this is a cross reference of pool ids by parent id
    // the pool ids in an Arraylist where the key is parent id
    poolFamilies = new HashMap();

    try
    {
      while(iter.hasNext())
      {
        QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();

        Long parentId  = pool.getParentPoolId();

        Long poolId = pool.getQuestionPoolId();
        poolMap.put(poolId.toString(), pool);
        ArrayList childList = new ArrayList();
        if(poolFamilies.containsKey(parentId.toString()))
        {
          childList = (ArrayList) poolFamilies.get(parentId.toString());
        }

        childList.add(poolId);
        poolFamilies.put(parentId.toString(), childList);
      }

      // Now sort the sibling lists.
      Iterator iter2 = poolFamilies.keySet().iterator();
      while(iter2.hasNext())
      {
        String key = (String) iter2.next();
        Iterator children = ((ArrayList) poolFamilies.get(key)).iterator();
        Collection sortedList = new ArrayList();
        while(children.hasNext())
        {
          QuestionPoolFacade pool = poolMap.get(children.next().toString());
          sortedList.add(pool.getData());
        }

        BeanSort sort = new BeanSort(sortedList, sortString);
        sortedList = sort.sort();
        ArrayList ids = new ArrayList();
        Iterator siblings = sortedList.iterator();
        while(siblings.hasNext())
        {
          QuestionPoolData next = null;
          try {
            next = (QuestionPoolData) siblings.next();
            if (poolMap != null) {
            	// Add at 0 because we want a reverse list.
            	if ("lastModified".equals(sortString)){
            		ids.add(0, (poolMap.get(next.getQuestionPoolId().toString())).getQuestionPoolId());
            	}
            	// Add to the end of list if not sorted by lastModified.
            	else {
            		ids.add((poolMap.get(next.getQuestionPoolId().toString())).getQuestionPoolId());
            	}
            }
            else {
            	log.error("poolMap is null");
            }
          } catch (RuntimeException e) {
            log.error("Couldn't get ID " + next.getQuestionPoolId());
          }
        }
        poolFamilies.put(key, ids);
      }
    }
    catch (SharedException se)
    {
      log.error(se.getMessage(), se);
    }
    catch(RuntimeException e)
    {
      log.error(e.getMessage(), e);
    }
  }


  /**
   * Get a List of pools having parentId as parent
   * @param parentId the Id of the parent pool
   * @return a List with the Ids of all momma's children
   */
  public List<Long> getChildList(Long parentId)
  {
    if(parentId == null || !poolFamilies.containsKey(parentId.toString()))
    {
      return new ArrayList();
    }

    return (ArrayList) poolFamilies.get(parentId.toString());
  }

  /**
   * Get a List of top level pools.
   * @return List of top level pool id strings
   */
  public List getRootNodeList()
  {
    try
    {

      return getChildList(new Long("0"));
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Get the object we're currently looking at.
   *
   * @return Current QuestionPool.
   */
  public Optional<QuestionPoolFacade> getCurrentObject()
  {
    return Optional.ofNullable(currentPoolId).map(id->poolMap.get(id.toString()));
  }

  /**
   * Get the parent of the object we're currently looking at.
   *
   * @return The parent pool of the current object, or empty if
   * it's a root node.
   */
  public Optional<QuestionPoolFacade> getParent()
  {
    return getCurrentObject().map(current -> poolMap.get(current.getParentPoolId().toString()));
  }


  public void setCurrentObjectHTMLId(String param)
  {
       currentObjectHTMLId = param;
  }

  /**
   * Get the HTML id of the current object.
   *
   * @return An HTML representation of the pool Id.
   */
  public String getCurrentObjectHTMLId()
  {
    return getCurrentObject().map(current ->
    {
      try
      {
        Optional<QuestionPoolFacade> parent = getParent();
        if(!parent.isPresent())
        {
          Collection childList = getChildList(Long.valueOf(0));

          return Integer.toString(
            ((ArrayList) childList).indexOf(current.getQuestionPoolId()) + 1);
        }
        else
        {
          setCurrentId(current.getParentPoolId());
          String result = getCurrentObjectHTMLId();
          Collection childList = getChildList(parent.get().getQuestionPoolId());
          setCurrentId(current.getQuestionPoolId());

          return result + "-" +
          (
            ((ArrayList) childList).indexOf(getCurrentObject().get().getQuestionPoolId()) + 1
          );
        }
      }
      catch(RuntimeException e)
      {
        log.error(e.getMessage(), e);
        return "0";
      }
    }).orElse("0");
  }

  /**
   * Get the current level.
   *
   * @return A String that represents the level we're on (1 is root node,
   * 2 is first level child, etc..
   */
  public String getCurrentLevel()
  {
    return getCurrentObject().map(current ->
    {
      int index1 = 1;
      try
      {
        while(!current.getParentPoolId().toString().equals("0"))
        {
          current = poolMap.get(current.getParentPoolId().toString());
          index1++;
        }
      }
      catch(Exception e)
      {
        log.error(e.getMessage(), e);
        return "0";
      }

      return Integer.toString(index1);
    }).orElse("0");
  }


  /**
   * Return in order the current:<ul>
   * <li> Pool Name
   * <li> Author
   * <li> Last Modified
   * <li> Total # of Questions
   * <li> Total # of Subpools
   */
  public Collection getCurrentObjectProperties()
  {
    Collection properties = new ArrayList();
/*
   // not used anymore,

    if(currentPoolId == null)
    {
      properties.add("Pool Name");
      properties.add("Author");
      properties.add("Last Modified");
      properties.add("Total # of Questions");
      properties.add("Total # of Subpools");
    }
    else
    {
      try
      {
        QuestionPoolFacade pool = (QuestionPoolFacade) getCurrentObject();
        QuestionPoolData props = (QuestionPoolData) pool.getData();
        if(props == null)
        {
          props = new QuestionPoolData();
        }

        //properties.add(pool.getDisplayName());

        // commenting the following temporarily because getOwner is null  - daisyf (9/19/04)
        properties.add(props.getOwner().getDisplayName());

        if(props.getLastModified() != null)
        {
        //  SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         // properties.add(sdf.format(props.getLastModified()));
         properties.add(props.getLastModified());

        }
        else
        {
          properties.add("N/A");
        }

        if(props.getQuestions() != null)
        {
          properties.add(new Integer(props.getQuestions().size()).toString());
        }
        else
        {
          properties.add("0");
        }

        if(getChildren(currentPoolId) != null)
        {
          properties.add(
            new Integer(getChildren(currentPoolId).size()).toString());
        }
        else
        {
          properties.add("0");
        }
      }
      catch(Exception e)
      {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);

      }
    }

*/
    return properties;
  }

  /**
   * Set the properties to request.  Not needed here.
   */
  public void setPropertyMethods(String[] methods)
  {
  }

  /**
   * Get Map of QuestionPoolImpls.
   * @return Map of all QuestionPoolImpls
   */
  public Map getAllObjects()
  {
    return poolMap;
  }

  /**
   * Dump the tree into a long collection of objects of the form:<ul>
   * <li> Pool 1
   * <ul> <li> Pool 2
   * <ul> <li>     Pool 3
   *      <li>     Pool 4
   * </ul><li> Pool 5
   *
   */
  public Collection getSortedObjects()
  {
    return getSortedObjects(QuestionPoolFacade.ROOT_POOL);
  }

  /**
   * get sorted objects for a subpool tree
   */
  public Collection getSortedObjects(Long poolId)
  {
    Collection total = new ArrayList();
    try
    {
      if (QuestionPoolFacade.ROOT_POOL.equals(poolId) && filteredIds != null) {
        addFilteredChildren(total);
      } else {
        addChildren(total, poolId);
      }
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return total;
  }

  private void addFilteredChildren(Collection<Object> total) {
    for(Long childId : filteredIds) {
      // Add child
      total.add(poolMap.get(childId.toString()));

      // Add grant children
      addChildren(total, childId);
    }
  }

  /**
   * Auxiliary method for recursion.
   */
  private void addChildren(Collection total, Long parentId)
  { 
    List childList = getChildList(parentId);
    if(childList.isEmpty())
    {
      return;
    }

    Iterator iter = childList.iterator();
    while(iter.hasNext())
    {
      Long nextId = (Long) iter.next();
      total.add(poolMap.get(nextId.toString()));
      addChildren(total, nextId);
    }
  }

  /**
   * Get Map of QuestionPoolImpls retricted to a given
   * Parent Id string
   * @param parentId parent of all returned children
   * @return  a Map of QuestionPoolImpls that are childen
   */
  public Map getChildren(Long parentId)
  {
    HashMap childPool = new HashMap();
    List childList = getChildList(parentId);
    Iterator children = childList.iterator();

    while(children.hasNext())
    {
      Long poolId = (Long) children.next();
      childPool.put(poolId.toString(), poolMap.get(poolId.toString()));
    }

    return childPool;
  }

  /**
   * Get Map of QuestionPoolImpls retricted to childeren of the currently
   * selected pool Id string
   * @return  a Map of QuestionPoolImpls that are children
   */
  public Map getChildren()
  {
    return getChildren(getCurrentId());
  }

  /**
   * Obtain the poolId set as the current working poolId-designated node
   * @return the current working poolId String
   */
  public Long getCurrentId()
  {
    return currentPoolId;
  }

  /**
   * Set the current working poolId String, from which context relative
   * child lists are calculated from the poolId-designated node
   * @param poolId
   */
  public void setCurrentId(Long poolId)
  {
    currentPoolId = poolId;
  }

  /**
   * Determine if the pool has childeren
   * @return true if it has children
   */
  public boolean currentObjectIsParent()
  {
    return poolFamilies.containsKey(getCurrentId().toString());
  }

  /**
   * List the child pool id strings
   * @return a list of children's pool id strings
   */
  public List getChildList()
  {
    return getChildList(getCurrentId());
  }


  /**
   * @return true if childlist is not empty, false otherwise
   */
  public boolean getHasChildList(){
        if (this.getChildList().isEmpty()) {
		return false;
	}
	else {
		return true;
	}
  }

  public boolean getHasNoChildList(){
        if (this.getChildList().isEmpty()) {
		return true;
	}
	else {
		return false;
	}
  }

  /**
   * This gets the property by which siblings will be sorted.
   *
   * @return A String representation of the sort property.
   */
  public String getSortProperty()
  {
    return sortString;
  }

  /**
   * This sets the property by which siblings will be sorted.
   */
  public void setSortProperty(String newProperty)
  {
    sortString = newProperty;
  }


  public void sortByProperty(String sortProperty, boolean sortAscending)
  {
      // Now sort the sibling lists.
      Iterator iter2 = poolFamilies.keySet().iterator();
      while(iter2.hasNext())
      {
        String key = (String) iter2.next();
        Iterator children = ((ArrayList) poolFamilies.get(key)).iterator();
        ArrayList sortedList = new ArrayList();
        while(children.hasNext())
        {
          QuestionPoolFacade pool = poolMap.get(children.next().toString());
          sortedList.add(pool.getData());
        }

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

        ArrayList ids = new ArrayList();
        Iterator siblings = sortedList.iterator();
        while(siblings.hasNext())
        {
          QuestionPoolData next = null;
          try {
            next = (QuestionPoolData) siblings.next();
            if (next != null) {
            	// Add at 0 because we want a reverse list.
            	if ("lastModified".equals(sortProperty)){
            		ids.add(0, (poolMap.get(next.getQuestionPoolId().toString())).getQuestionPoolId());
            	}
            	else {
            		// Add at the end , if not sorted by lastModified.
            		ids.add((poolMap.get(next.getQuestionPoolId().toString())).getQuestionPoolId());
            	}
            }
            else {
            	log.error("next is null");
            }
          } catch (RuntimeException e) {
            log.error("Couldn't get ID " + next.getQuestionPoolId());
          }
        }

        poolFamilies.put(key, ids);
      }


  }

  /**
   * THis checks to see if given two pools have a common ancestor
   */
  public boolean haveCommonRoot(Long poolIdA,Long poolIdB)
  {
    try{
    	Long rootA=poolIdA;
    	Long rootB=poolIdB;

    	QuestionPoolFacade tempPool=poolMap.get(rootA.toString());
    	while(tempPool!=null){
      		if((tempPool.getParentPoolId()==null)||(((tempPool.getParentPoolId()).toString()).equals("0"))){
        		tempPool=null;
      		}else{
        		rootA = tempPool.getParentPoolId();
        		tempPool = poolMap.get(rootA.toString());
      		}
    	}
    	tempPool=poolMap.get(rootB.toString());
    	while(tempPool!=null){
      		if((tempPool.getParentPoolId()==null)||(((tempPool.getParentPoolId()).toString()).equals("0"))){
        		tempPool=null;
      		}else{
        		rootB = tempPool.getParentPoolId();
        		tempPool = poolMap.get(rootB.toString());
      		}
   	}
    	return rootA.equals(rootB);
    }
	catch(Exception e){
                log.error(e.getMessage(), e);
      		return false;
    	}
  }

  /**
   * Is a pool (pool A) a descendant of the other (Pool B)?
   */
  public boolean isDescendantOf(Long poolA,Long poolB)
  {
    try{
      Long tempPoolId = poolA;
      while((tempPoolId !=null)&&(tempPoolId.toString().compareTo("0")>0)){
        QuestionPoolFacade tempPool = poolMap.get(tempPoolId.toString());
        if(tempPool.getParentPoolId().toString().compareTo(poolB.toString())==0) return true;
        tempPoolId = tempPool.getParentPoolId();
      }
      return false;

    }catch(Exception e){
      log.error(e.getMessage(), e);
      return false;
    }
  }

 /**
   * This returns the level of the pool inside a pool tree, Root being 0.
   */
  public int poolLevel(Long poolId){
    try{
    Long rootId=poolId;
    int level=0;

    QuestionPoolFacade tempPool=poolMap.get(rootId.toString());
    while(tempPool!=null){
      if((tempPool.getParentPoolId()==null)||(((tempPool.getParentPoolId()).toString()).equals("0"))){
        tempPool=null;
      }else{
        level++;
        rootId = tempPool.getParentPoolId();
        tempPool = poolMap.get(rootId.toString());
      }
    }
    return level;
    }catch(Exception e){
      log.error(e.getMessage(), e);
      return 0;
    }
  }


  public void clearFilters() {
    filteredIds = null;
  }

  public void filterByTags(Collection<TagIfc> tags) {
    Set<Long> filteredPoolIds = getChildList(QuestionPoolFacade.ROOT_POOL).stream()
        .filter(poolId -> tagFilter(poolId, tags))
        .collect(Collectors.toSet());

    filteredIds = filteredPoolIds;
  }

  private boolean tagFilter(Long poolId, Collection<TagIfc> filterTags) {
    QuestionPoolFacade pool = poolMap.get(poolId.toString());
    if (pool != null) {
      Set<QuestionPoolTag> poolTags = pool.getTags();

      if (poolTags != null && poolTags.containsAll(filterTags)) {
        return true;
      } else {
        // Does any child match the filter?
        return getChildList(poolId).stream()
            .filter(childId -> tagFilter(childId, filterTags))
            .findAny()
            .isPresent();
      }
    } else {
      log.warn("Pool with id {} not found in tree", poolId);
      return false;
    }
  }











}

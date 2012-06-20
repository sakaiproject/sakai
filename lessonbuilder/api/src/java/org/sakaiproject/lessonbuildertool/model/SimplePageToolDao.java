/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.model;

import java.util.List;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;

public interface SimplePageToolDao {

	public class PageData {
	    public Long itemId;
	    public Long pageId;
	    public String name;
	}

    // can edit pages in current site. Make sure that the page you are going to
    // edit is actually part of the current site.
	public boolean canEditPage();

    // returns a list of all items on the page, ordered by sequence number
	public List<SimplePageItem> findItemsOnPage(long pageId);


    // returns the next item on the page. argument is sequence no. I.e.
    // finds item with sequence + 1 on specified page. just pages
	public SimplePageItem findNextPageItemOnPage(long pageId, int sequence);

    // returns the next item on the page. argument is sequence no. I.e.
    // finds item with sequence + 1 on specified page. any item type
	public SimplePageItem findNextItemOnPage(long pageId, int sequence);

    // for a specific tool (i.e. instance of lessonbuilder), find the most
    // recently visited page. Used so we can offer to return user to where he was last
	public PageData findMostRecentlyVisitedPage(final String userId, final String tooldId);

    // list of top-level pages in the site. However we return the items for the
    // pages, not the pages themselves. Ordered by site order, so this should be the
    // same order in which they are shown in the left margin
	public List<SimplePageItem> findItemsInSite(String siteId);

	public List<SimplePageItem> findDummyItemsInSite(String siteId);

	public SimplePageItem findItem(long id);
	
	public List<SimplePageComment> findComments(long commentWidgetId);
	
	public List<SimplePageComment> findCommentsOnItems(List<Long> commentItemIds);
	
	public List<SimplePageComment> findCommentsOnItemsByAuthor(List<Long> commentItemIds, String author);

	public List<SimplePageComment> findCommentsOnItemByAuthor(long commentWidgetId, String author);

	public List<SimplePageComment> findCommentsOnPageByAuthor(long pageId, String author);
	
	public SimplePageComment findCommentById(long commentId);
	
	public SimplePageComment findCommentByUUID(String commentUUID);
	
	public SimplePageItem findCommentsToolBySakaiId(String sakaiId);

    // this is a generic one. Only use it for nearly unique sakaiids.
	public List<SimplePageItem> findItemsBySakaiId(String sakaiId);
	
	public SimpleStudentPage findStudentPage(long itemId, String owner);
	
	public SimpleStudentPage findStudentPage(long id);
	
	public SimpleStudentPage findStudentPageByPageId(long pageId);
	
	public List<SimpleStudentPage> findStudentPages(long itemId);
	
	/**
	 * Finds the SimplePageItem based on the pageId of a page created
	 * in the Student Content tool.
	 * @param pageId ID of the student SimplePage object
	 * @return SimplePageItem of the collection that this page belongs to.
	 */
	public SimplePageItem findItemFromStudentPage(long pageId);
	
    // find the item corresponding to a top level page. the page id is
    // stored as the sakaiId of the item, which is the reason for the method name
    // of course the String argument is just a long converted to a string.
	public SimplePageItem findTopLevelPageItemBySakaiId(String id);

    // find all items with given page ID
	public List<SimplePageItem> findPageItemsBySakaiId(String id);

    // find resource items with access control involving specified sakaiid
	public List findControlledResourcesBySakaiId(String id, String siteid);

    // basically, this is the Hibernate save. It works with any of our object types.
    // Checks for canEditPage, Except for SimplePageLog, where the code is assumed to 
    //   only write things it's allowed to. NB the limitation of canEditPage. You had
    //   better be updating an item or page in the current site, or canEditPage will give
    //   the wrong answer.
    // Also generates events showing the update.
    // elist is a list where saveItem will add error messages, nowriteerr is the message to use if
    //   the user doesn't have write permission. See saveitem in SimplePageBean for why we need
    //   to use this convoluted approach to getting back errors
	public boolean saveItem(Object o, List<String> elist, String nowriteerr, boolean requiresEditPermission);

    // just do the save, no permission checking and no logging
	public boolean quickSaveItem(Object o);

    // see saveItem for details and caveats, same function except delete instead of save
	public boolean deleteItem(Object o);

    // see saveItem for details and caveats, same function except update instead of save
	public boolean update(Object o, List<String> elist, String nowriteerr, boolean requiresEditPermission);

    // version without permission checking and logging
	public boolean quickUpdate(Object o);

	public Long getTopLevelPageId(String toolId);

	public SimplePage getPage(long pageId);

    // list of all pages in the site, not just top level
	public List<SimplePage> getSitePages(String siteId);

	/**
	 * studentPageId is only to be used if it is a student-made page.  It corresponds to the <b>pageId</b>
	 * found in either lesson_builder_student_pages or lesson_builder_pages.
	 * 
	 * There should only be one log entry for each combination.
	 */
	public SimplePageLogEntry getLogEntry(String userId, long itemId, Long studentPageId);
	
    // includes the dummy entries for preauthoized pages, but that's OK
	public boolean isPageVisited(long pageId, String userId, String owner);

	public List<SimplePageLogEntry> getStudentPageLogEntries(long itemId, String userId);

    // users with log entries showing item complete
	public List<String> findUserWithCompletePages(Long itemId);

    // find group controlling a given item. Note that the argument is the
    // sakaiId, not the item ID. So if the same assignment, etc., appears
    // on several pages, the same group controls all of the appearances
	public SimplePageGroup findGroup(String itemId);

    // constructors, so code doesn't have to use the Impl's directly
    public SimplePage makePage(String toolId, String siteId, String title, Long parent, Long topParent);

    public SimplePageItem makeItem(long id, long pageId, int sequence, int type, String sakaiId, String name);

    public SimplePageItem makeItem(long pageId, int sequence, int type, String sakaiId, String name);

    public SimplePageGroup makeGroup(String itemId, String groupId, String groups);

    public SimplePageLogEntry makeLogEntry(String userId, long itemId, Long studentPageId);
    
    public SimplePageComment makeComment(long itemId, long pageId, String author, String comment, String UUID, boolean html);

    public SimpleStudentPage makeStudentPage(long itemId, long pageId, String title, String author, boolean groupOwned);
    
    public SimplePageItem copyItem(SimplePageItem old);

}

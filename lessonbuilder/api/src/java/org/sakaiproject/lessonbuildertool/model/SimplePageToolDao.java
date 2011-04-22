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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;

public interface SimplePageToolDao {

	public class PageData {
	    public Long itemId;
	    public Long pageId;
	    public String name;
	}

    // can edit pages in current site. Make sure that the page you are going to
    // edit is actually part of the current site.
	public boolean canEditPage();

    // returns a list of all items on the page, orderd by sequence number
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

	public SimplePageItem findItem(long id);

    // find the item corresponding to a top level page. the page id is
    // stored as the sakaiId of the item, which is the reason for the method name
    // of course the String argument is just a long converted to a string.
	public SimplePageItem findTopLevelPageItemBySakaiId(String id);

    // find all items with given page ID
        public List<SimplePageItem> findPageItemsBySakaiId(String id);

    // basically, this is the Hibernate save. It works with any of our object types.
    // Checks for canEditPage, Except for SimplePageLog, where the code is assumed to 
    //   only write things it's allowed to. NB the limitation of canEditPage. You had
    //   better be updating an item or page in the current site, or canEditPage will give
    //   the wrong answer.
    // Also generates events showing the update.
    // elist is a list where saveItem will add error messages, nowriteerr is the messge to use if
    //   the user doesn't have write permission. See saveitem in SimplePageBean for why we need
    //   to use this convoluted approach to getting back errors
	public boolean saveItem(Object o, List<String> elist, String nowriteerr);

    // just do the save, no permission checking and no logging
	public boolean quickSaveItem(Object o);

    // see saveItem for details and caveats, same function except delete instead of save
	public boolean deleteItem(Object o);

    // see saveItem for details and caveats, same function except update instead of save
	public boolean update(Object o, List<String> elist, String nowriteerr);

    // version without permission checking and logging
	public boolean quickUpdate(Object o);

	public Long getTopLevelPageId(String toolId);

	public SimplePage getPage(long pageId);

    // list of all pages in the site, not just top level
        public List<SimplePage> getSitePages(String siteId);

    // log entry for a specific item. There can only be one.
	public SimplePageLogEntry getLogEntry(String userId, long itemId);

    // users with log entries showing item complete
        public List<String> findUserWithCompletePages(Long itemId);

    // find group controlling a given item. Note that the argument is the
    // sakaiId, not the item ID. So if the same assignment, etc., appears
    // on several pages, the same group controls all of the appearences
	public SimplePageGroup findGroup(String itemId);

    // constructors, so code doesn't have to use the Impl's directly
    public SimplePage makePage(String toolId, String siteId, String title, Long parent, Long topParent);

    public SimplePageItem makeItem(long id, long pageId, int sequence, int type, String sakaiId, String name);

    public SimplePageItem makeItem(long pageId, int sequence, int type, String sakaiId, String name);

    public SimplePageGroup makeGroup(String itemId, String groupId);

    public SimplePageLogEntry makeLogEntry(String userId, long itemId);

    public SimplePageItem copyItem(SimplePageItem old);

}

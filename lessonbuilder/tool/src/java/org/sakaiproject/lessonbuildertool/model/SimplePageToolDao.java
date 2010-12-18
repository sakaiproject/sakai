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

	public List<SimplePageItem> findItemsOnPage(long pageId);

        public SimplePageItem findNextPageItemOnPage(long pageId, int sequence);

        public List<SimplePageItem> findItemsInSite(String siteId);

	public SimplePageItem findItem(long id);

	public SimplePageItem findTopLevelPageItemBySakaiId(String id);

	public boolean saveItem(Object o);

	public boolean deleteItem(Object o);

	public boolean update(Object o);

	public Long getTopLevelPageId(String toolId);

	public SimplePage getPage(long pageId);

        public List<SimplePage> getSitePages(String siteId);

	public SimplePageLogEntry getLogEntry(String userId, long itemId);

	/**
	 * 
	 * @param itemId
	 *            The ID of Assignment or Assessment, not the typical itemId
	 * @return
	 */
	public SimplePageGroup findGroup(String itemId);
}

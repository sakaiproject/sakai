/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;


/**
 * Represents a record from the SST_LESSONBULDER table.
 *
 * This must be {@link java.lang.Comparable} so that the updates can be sorted before being inserted into the database
 * to avoid deadlocks.
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public interface LessonBuilderStat extends Stat, Comparable<LessonBuilderStat> {

	/** Get the the page reference (eg. '/lessonbuilder/page/2') this record refers to. */
	public String getPageRef();

	/** Set the the page reference (eg. '/lessonbuilder/page/2') this record refers to. */
	public void setPageRef(String pageRef);

	/** Get the the page action (one of 'create','read' ...) this record refers to. */
	public String getPageAction();

	/** Set the the page action (one of 'create','read' ...) this record refers to. */
	public void setPageAction(String pageAction);

	/** Get the the page title */
	public String getPageTitle();

	/** Get the the page title */
	public void setPageTitle(String pageTitle);

	/** Get the the page id */
	public long getPageId();

	/** Set the the page id */
	public void setPageId(long pageId);
}

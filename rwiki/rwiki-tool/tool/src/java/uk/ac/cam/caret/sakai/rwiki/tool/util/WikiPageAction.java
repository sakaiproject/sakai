/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.util;

import java.util.HashMap;

/**
 * @author andrew
 */

public class WikiPageAction
{

	private static final HashMap INDEX = new HashMap();

	public static final WikiPageAction PUBLICVIEW_ACTION = new WikiPageAction(
			"publicview");

	public static final WikiPageAction VIEW_ACTION = new WikiPageAction("view");

	public static final WikiPageAction EDIT_ACTION = new WikiPageAction("edit");

	public static final WikiPageAction INFO_ACTION = new WikiPageAction("info");

	public static final WikiPageAction SEARCH_ACTION = new WikiPageAction(
			"search");

	public static final WikiPageAction FULL_SEARCH_ACTION = new WikiPageAction(
			"full_search");

	public static final WikiPageAction TITLE_ACTION = new WikiPageAction(
			"Title");

	public static final WikiPageAction REVERT_ACTION = new WikiPageAction(
			"revert");

	public static final WikiPageAction DIFF_ACTION = new WikiPageAction("diff");

	public static final WikiPageAction SAVE_ACTION = new WikiPageAction("save");

	public static final WikiPageAction REVIEW_ACTION = new WikiPageAction(
			"review");

	public static final WikiPageAction HISTORY_ACTION = new WikiPageAction(
			"history");

	public static final WikiPageAction EDIT_REALM_ACTION = new WikiPageAction(
			"editRealm");

	public static final WikiPageAction EDIT_REALM_MANY_ACTION = new WikiPageAction(
			"editRealm-many");

	public static final WikiPageAction NEWCOMMENT_ACTION = new WikiPageAction(
			"commentnew");

	public static final WikiPageAction EDITCOMMENT_ACTION = new WikiPageAction(
			"commentedit");

	public static final WikiPageAction LISTCOMMENT_ACTION = new WikiPageAction(
			"commentslist");

	public static final WikiPageAction NEWCOMMENT_SAVE_ACTION = new WikiPageAction(
			"commentnewsave");

	public static final WikiPageAction EDITCOMMENT_SAVE_ACTION = new WikiPageAction(
			"commenteditsave");

	public static final WikiPageAction LISTPRESENCE_ACTION = new WikiPageAction(
			"presencelist");

	public static final WikiPageAction OPENPAGECHAT_ACTION = new WikiPageAction(
			"presencechat");

	public static final WikiPageAction OPENSPACECHAT_ACTION = new WikiPageAction(
			"presencechat");

	public static final WikiPageAction LISTPAGECHAT_ACTION = new WikiPageAction(
			"presencechatlist");

	public static final WikiPageAction LISTSPACECHAT_ACTION = new WikiPageAction(
			"presencechatlist");

	public static final WikiPageAction EXPORT_ACTION = new WikiPageAction(
			"export");

	public static final WikiPageAction LINK_ATTACHMENT_RETURN_ACTION = new WikiPageAction(
			"addAttachmentReturnLink");

	public static final WikiPageAction EMBED_ATTACHMENT_RETURN_ACTION = new WikiPageAction(
			"addAttachmentReturnEmbed");

	public static final WikiPageAction PREFERENCES_ACTION = new WikiPageAction(
			"preferences");


	private String name;

	private WikiPageAction(String name)
	{
		this.name = name;
		INDEX.put(name, this);
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return name;
	}

	public WikiPageAction lookup(String name)
	{
		return (WikiPageAction) INDEX.get(name);
	}
}

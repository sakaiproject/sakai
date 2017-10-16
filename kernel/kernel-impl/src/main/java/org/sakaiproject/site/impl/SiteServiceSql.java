/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.site.impl;

/**
 * database methods.
 */
public interface SiteServiceSql
{
	/**
	 * returns the sql statement which deletes the groups for a given site from the sakai_site_group table.
	 */
	String getDeleteGroupsSql();

	/**
	 * returns the sql statement which deletes the group properties for a given site from the sakai_site_group_property table.
	 */
	String getDeleteGroupPropertiesSql();

	/**
	 * returns the sql statement which deletes the pages for a given site from the sakai_site_page table.
	 */
	String getDeletePagesSql();

	/**
	 * returns the sql statement which deletes the page properties for a given site from the sakai_site_page_property table.
	 */
	String getDeletePagePropertiesSql();

	/**
	 * returns the sql statement which deletes an individual tool for a given site from the sakai_site_tool table.
	 */
	String getDeleteToolSql();

	/**
	 * returns the sql statement which deletes the tools for a given site from the sakai_site_tool table.
	 */
	String getDeleteToolsSql();

	/**
	 * returns the sql statement which deletes the tool properties for a given site from the sakai_site_tool_property table.
	 */
	String getDeleteToolPropertiesSql();

	/**
	 * returns the sql statement which deletes an individual tool property for a given site from the sakai_site_tool_property table.
	 */
	String getDeleteToolPropertySql();

	/**
	 * returns the sql statement which deletes a user from a given site.
	 */
	String getDeleteUserSql();

	/**
	 * returns the sql statement which deletes users for a given site from the sakai_site_user table.
	 */
	String getDeleteUsersSql();

	/**
	 * returns the sql statement which inserts a group into the sakai_site_group table.
	 */
	String getInsertGroupSql();

	/**
	 * returns the sql statement which inserts a page into the sakai_site_page table.
	 */
	String getInsertPageSql();

	/**
	 * returns the sql statement which inserts a tool into the sakai_site_tool table.
	 */
	String getInsertToolSql();

	/**
	 * returns the sql statement which inserts a user into a given site.
	 */
	String getInsertUserSql();

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	String getSitesJoin1Sql();

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	String getSitesJoin2Sql();

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	String getSitesJoin3Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder1Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder2Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder3Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder4Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder5Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder6Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder7Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder8Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder9Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder10Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder11Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder12Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder13Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder14Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder15Sql();

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	String getSitesOrder16Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere1Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere2Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere3Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere4Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere5Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere6Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere7Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere8Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere9Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere10Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere11Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere12Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere13Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere14Sql();

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere15Sql();
	
	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	String getSitesWhere16Sql(int size);

	/**
	 * returns the sql statement which retrieves the skin and whether the site has been published from the sakai_site table.
	 */
	String getSkinSql();

	/**
	 * returns the sql statement which retrieves some fields from the sakai_site and sakai_site_tool tables.
	 */
	String getToolFields1Sql();

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given page.
	 */
	String getToolFields2Sql();

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given site.
	 */
	String getToolFields3Sql();

	/**
	 * returns the sql statement which retrieves the types of sites that have been created from the sakai_site table.
	 */
	String getTypesSql();

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given site.
	 */
	String getGroupFieldsSql();

	/**
	 * returns the sql statement which retrieves some fields from the sakai_site and sakai_site_page tables.
	 */
	String getPageFields1Sql();

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_page table for a given site.
	 */
	String getPageFields2Sql();

	/**
	 * returns the sql statement which retrieves the site id for a given page from the sakai_site_page table.
	 */
	String getSiteId1Sql();

	/**
	 * returns the sql statement which retrieves the site id for a given group from the sakai_site_group table.
	 */
	String getSiteId2Sql();

	/**
	 * returns the sql statement which retrieves the site id for a given tool from the sakai_site_tool table.
	 */
	String getSiteId3Sql();

	/**
	 * returns the sql statement which retrieves the site id and permission for a given user from the sakai_site_user table.
	 */
	String getSiteId4Sql();

	/**
	 * returns the sql statement which retrieves the user id for a given site from the sakai_site_user table.
	 */
	String getUserIdSql();

	/**
	 * returns the sql statement which retrieves the group properties from the sakai_site_group_property table for a given site.
	 */
	String getGroupPropertiesSql();

	/**
	 * returns the sql statement which retrieves the page properties from the sakai_site_page_property table for a given site.
	 */
	String getPagePropertiesSql();

	/**
	 * returns the sql statement which retrieves the tool properties from the sakai_site_tool_property table for a given site.
	 */
	String getToolPropertiesSql();

	/**
	 * returns the sql statement which updates a site into the sakai_site table.
	 */
	String getUpdateSiteSql(String table);
	
	/**
	 * returns the sql statement which is part of the where clause to retrieve sites which are softly deleted
	 * TODO this could take a param which further filters on date-gracetime
	 */
	String getSitesWhereSoftlyDeletedOnlySql();
	
	/**
	 * returns the sql statement which is part of the where clause to only retrieve sites that are NOT softly deleted
	 * @return
	 */
	String getSitesWhereNotSoftlyDeletedSql();

	/**
	 * returns part of the where clause to retrieve sites that are unpublished
	 */
	String getUnpublishedSitesOnlySql();
}

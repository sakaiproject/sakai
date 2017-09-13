/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/site/site-impl/impl/src/java/org/sakaiproject/site/impl/SiteServiceSqlDefault.java $
 * $Id: SiteServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
 * methods for accessing site data in a database.
 */
public class SiteServiceSqlDefault implements SiteServiceSql
{
	/**
	 * returns the sql statement which deletes the groups for a given site from the sakai_site_group table.
	 */
	public String getDeleteGroupsSql()
	{
		return "delete from SAKAI_SITE_GROUP where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes the group properties for a given site from the sakai_site_group_property table.
	 */
	public String getDeleteGroupPropertiesSql()
	{
		return "delete from SAKAI_SITE_GROUP_PROPERTY where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes the pages for a given site from the sakai_site_page table.
	 */
	public String getDeletePagesSql()
	{
		return "delete from SAKAI_SITE_PAGE where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes the page properties for a given site from the sakai_site_page_property table.
	 */
	public String getDeletePagePropertiesSql()
	{
		return "delete from SAKAI_SITE_PAGE_PROPERTY where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes an individual tool for a given site from the sakai_site_tool table.
	 */
	public String getDeleteToolSql()
	{
		return "delete from SAKAI_SITE_TOOL where SITE_ID = ? and TOOL_ID = ?";
	}

	/**
	 * returns the sql statement which deletes the tools for a given site from the sakai_site_tool table.
	 */
	public String getDeleteToolsSql()
	{
		return "delete from SAKAI_SITE_TOOL where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes the tool properties for a given site from the sakai_site_tool_property table.
	 */
	public String getDeleteToolPropertiesSql()
	{
		return "delete from SAKAI_SITE_TOOL_PROPERTY where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which deletes an individual tool property for a given site from the sakai_site_tool_property table.
	 */
	public String getDeleteToolPropertySql()
	{
		return "delete from SAKAI_SITE_TOOL_PROPERTY where SITE_ID = ? and TOOL_ID = ?";
	}

	/**
	 * returns the sql statement which deletes a user from a given site.
	 */
	public String getDeleteUserSql()
	{
		return "delete from SAKAI_SITE_USER where SITE_ID = ? and USER_ID = ?";
	}

	/**
	 * returns the sql statement which deletes users for a given site from the sakai_site_user table.
	 */
	public String getDeleteUsersSql()
	{
		return "delete from SAKAI_SITE_USER where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which inserts a group into the sakai_site_group table.
	 */
	public String getInsertGroupSql()
	{
		return "insert into SAKAI_SITE_GROUP (GROUP_ID, SITE_ID, TITLE, DESCRIPTION) values (?,?,?,?)";
	}

	/**
	 * returns the sql statement which inserts a page into the sakai_site_page table.
	 */
	public String getInsertPageSql()
	{
		return "insert into SAKAI_SITE_PAGE (PAGE_ID, SITE_ID, TITLE, LAYOUT, POPUP, SITE_ORDER) values (?,?,?,?,?,?)";
	}

	/**
	 * returns the sql statement which inserts a tool into the sakai_site_tool table.
	 */
	public String getInsertToolSql()
	{
		return "insert into SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS) values (?,?,?,?,?,?,?)";
	}

	/**
	 * returns the sql statement which inserts a user into a given site.
	 */
	public String getInsertUserSql()
	{
		return "insert into SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) values (?, ?, ?)";
	}

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	public String getSitesJoin1Sql()
	{
		return "SAKAI_SITE_USER";
	}

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	public String getSitesJoin2Sql()
	{
		return ", SAKAI_USER_ID_MAP";
	}

	/**
	 * returns the sql statement which is part of the join clause to retrieve sites.
	 */
	public String getSitesJoin3Sql()
	{
		return "SAKAI_USER_ID_MAP";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder1Sql()
	{
		return "SAKAI_SITE.SITE_ID ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder2Sql()
	{
		return "SAKAI_SITE.SITE_ID DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder3Sql()
	{
		return "SAKAI_SITE.TITLE ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder4Sql()
	{
		return "SAKAI_SITE.TITLE DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder5Sql()
	{
		return "SAKAI_SITE.TYPE ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder6Sql()
	{
		return "SAKAI_SITE.TYPE DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder7Sql()
	{
		return "SAKAI_SITE.PUBLISHED ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder8Sql()
	{
		return "SAKAI_SITE.PUBLISHED DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder9Sql()
	{
		return "SAKAI_USER_ID_MAP.EID ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder10Sql()
	{
		return "SAKAI_USER_ID_MAP.EID DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder11Sql()
	{
		return "SAKAI_USER_ID_MAP.EID ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder12Sql()
	{
		return "SAKAI_USER_ID_MAP.EID DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder13Sql()
	{
		return "SAKAI_SITE.CREATEDON ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder14Sql()
	{
		return "SAKAI_SITE.CREATEDON DESC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder15Sql()
	{
		return "SAKAI_SITE.MODIFIEDON ASC";
	}

	/**
	 * returns the sql statement which is part of the order clause to retrieve sites.
	 */
	public String getSitesOrder16Sql()
	{
		return "SAKAI_SITE.MODIFIEDON DESC";
	}
	
	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere1Sql()
	{
		return "SAKAI_SITE.SITE_ID = SAKAI_SITE_USER.SITE_ID and SAKAI_SITE_USER.USER_ID = ? and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere2Sql()
	{
		return "SAKAI_SITE.IS_USER = '0' and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere3Sql()
	{
		return "SAKAI_SITE.IS_SPECIAL = '0' and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere4Sql()
	{
		return "SAKAI_SITE.PUBLISHED = 1 and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere5Sql()
	{
		return "SAKAI_SITE.TYPE = ? and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere6Sql()
	{
		return "SAKAI_SITE.TYPE IN (?";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere7Sql()
	{
		return "SAKAI_SITE.JOINABLE = '1' and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere8Sql()
	{
		return "SAKAI_SITE.PUBVIEW = '1' and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere9Sql()
	{
		return "UPPER(SAKAI_SITE.TITLE) like UPPER(?) and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere10Sql()
	{
		return "SAKAI_SITE_USER.PERMISSION <= -1 and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere11Sql()
	{
		return "SAKAI_SITE_USER.PERMISSION <= SAKAI_SITE.PUBLISHED and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere12Sql()
	{
		return "SITE_ID not in (select SITE_ID from SAKAI_SITE_USER where USER_ID = ? and PERMISSION <= PUBLISHED) and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere13Sql()
	{
		return "SAKAI_SITE.SITE_ID in (select SITE_ID from SAKAI_SITE_PROPERTY where NAME = ? and UPPER(VALUE) like UPPER(?)) and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere14Sql()
	{
		return "SAKAI_SITE.CREATEDBY = SAKAI_USER_ID_MAP.USER_ID and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere15Sql()
	{
		return "SAKAI_SITE.MODIFIEDBY = SAKAI_USER_ID_MAP.USER_ID and ";
	}
	
	/**
	 * returns the sql statement which is part of the where clause to retrieve sites.
	 */
	public String getSitesWhere16Sql(int size)
	{
		StringBuilder values = new StringBuilder();
		for(int i=0; i < size; i++){
			if(i>0){
				values.append(",");
			}
			values.append("?");
		}
		return "SAKAI_SITE.SITE_ID not in ("+values.toString()+") and ";
	}

	/**
	 * returns the sql statement which is part of the where clause to retrieve the number of sites.
	 */
	/*
	 * public String getSitesCountWhere12Sql() { return "SAKAI_SITE.SITE_ID not in (select SITE_ID from SAKAI_SITE_USER where USER_ID = ? and
	 * PERMISSION <= PUBLISHED) and "; } "SITE_ID not in (select SITE_ID from SAKAI_SITE_USER where USER_ID = ? and PERMISSION <= PUBLISHED) and "
	 */
	/**
	 * returns the sql statement which retrieves the skin and whether the site has been published from the sakai_site table.
	 */
	public String getSkinSql()
	{
		return "select SKIN, PUBLISHED from SAKAI_SITE where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves some fields from the sakai_site and sakai_site_tool tables.
	 */
	public String getToolFields1Sql()
	{
		return "select REGISTRATION, SAKAI_SITE_TOOL.TITLE, LAYOUT_HINTS, SAKAI_SITE_TOOL.SITE_ID, PAGE_ID, SKIN, PUBLISHED, PAGE_ORDER "
				+ "from   SAKAI_SITE_TOOL, SAKAI_SITE " + "where  SAKAI_SITE_TOOL.SITE_ID = SAKAI_SITE.SITE_ID and TOOL_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given page.
	 */
	public String getToolFields2Sql()
	{
		return "select TOOL_ID, REGISTRATION, TITLE, LAYOUT_HINTS, PAGE_ORDER from SAKAI_SITE_TOOL where PAGE_ID = ? order by PAGE_ORDER ASC";
	}

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given site.
	 */
	public String getToolFields3Sql()
	{
		return "select TOOL_ID, PAGE_ID, REGISTRATION, TITLE, LAYOUT_HINTS, PAGE_ORDER from SAKAI_SITE_TOOL where SITE_ID = ? order by PAGE_ID, PAGE_ORDER ASC";
	}

	/**
	 * returns the sql statement which retrieves the types of sites that have been created from the sakai_site table.
	 */
	public String getTypesSql()
	{
		return "select distinct TYPE from SAKAI_SITE order by TYPE";
	}

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_tool table for a given site.
	 */
	public String getGroupFieldsSql()
	{
		return "select SS.GROUP_ID, SS.TITLE, SS.DESCRIPTION " + "from SAKAI_SITE_GROUP SS where SS.SITE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves some fields from the sakai_site and sakai_site_page tables.
	 */
	public String getPageFields1Sql()
	{
		return "select PAGE_ID, SAKAI_SITE_PAGE.TITLE, LAYOUT, SAKAI_SITE_PAGE.SITE_ID, SKIN, PUBLISHED, POPUP "
				+ "from  SAKAI_SITE_PAGE, SAKAI_SITE " + "where SAKAI_SITE_PAGE.SITE_ID = SAKAI_SITE.SITE_ID " + "and PAGE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves various fields from the sakai_site_page table for a given site.
	 */
	public String getPageFields2Sql()
	{
		return "select PAGE_ID, TITLE, LAYOUT, POPUP from SAKAI_SITE_PAGE where SITE_ID = ? order by SITE_ORDER ASC";
	}

	/**
	 * returns the sql statement which retrieves the site id for a given page from the sakai_site_page table.
	 */
	public String getSiteId1Sql()
	{
		return "select SITE_ID from SAKAI_SITE_PAGE where PAGE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves the site id for a given group from the sakai_site_group table.
	 */
	public String getSiteId2Sql()
	{
		return "select SS.SITE_ID from SAKAI_SITE_GROUP SS where SS.GROUP_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves the site id for a given tool from the sakai_site_tool table.
	 */
	public String getSiteId3Sql()
	{
		return "select SITE_ID from SAKAI_SITE_TOOL where TOOL_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves the site id and permission for a given user from the sakai_site_user table.
	 */
	public String getSiteId4Sql()
	{
		return "select SITE_ID, PERMISSION from SAKAI_SITE_USER " + "where USER_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves the user id for a given site from the sakai_site_user table.
	 */
	public String getUserIdSql()
	{
		return "select USER_ID, PERMISSION from SAKAI_SITE_USER " + "where SITE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves the group properties from the sakai_site_group_property table for a given site.
	 */
	public String getGroupPropertiesSql()
	{
		return "select GROUP_ID, NAME, VALUE from SAKAI_SITE_GROUP_PROPERTY where ( SITE_ID = ? )";
	}

	/**
	 * returns the sql statement which retrieves the page properties from the sakai_site_page_property table for a given site.
	 */
	public String getPagePropertiesSql()
	{
		return "select PAGE_ID, NAME, VALUE from SAKAI_SITE_PAGE_PROPERTY where ( SITE_ID = ? )";
	}

	/**
	 * returns the sql statement which retrieves the tool properties from the sakai_site_tool_property table for a given site.
	 */
	public String getToolPropertiesSql()
	{
		return "select TOOL_ID, NAME, VALUE from SAKAI_SITE_TOOL_PROPERTY where ( SITE_ID = ? )";
	}

	/**
	 * returns the sql statement which updates a site into the sakai_site table.
	 */
	public String getUpdateSiteSql(String table)
	{
		return "update " + table + " set DESCRIPTION = ?, INFO_URL = ? where SITE_ID = ?";
	}
	
	/**
	 * returns the sql statement which is part of the where clause to retrieve sites which are softly deleted
	 */
	public String getSitesWhereSoftlyDeletedOnlySql()
	{
		return "SAKAI_SITE.IS_SOFTLY_DELETED = '1' and ";
	}
	
	/**
	 * returns the sql statement which is part of the where clause to only retrieve sites that are NOT softly deleted
	 * @return
	 */
	public String getSitesWhereNotSoftlyDeletedSql()
	{
		return "SAKAI_SITE.IS_SOFTLY_DELETED = '0' and ";
	}

	/**
	 * returns part of the where clause to retrieve sites that are unpublished
	 */
	public String getUnpublishedSitesOnlySql() {
		return "SAKAI_SITE.PUBLISHED = '0' and ";
	}
}

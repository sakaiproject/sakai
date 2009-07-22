/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/site/site-impl/impl/src/java/org/sakaiproject/site/impl/SiteServiceSqlDb2.java $
 * $Id: SiteServiceSqlDb2.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.site.impl;

/**
 * methods for accessing site data in a db2 database.
 */
public class SiteServiceSqlDb2 extends SiteServiceSqlDefault
{
     /**
      * returns the sql statement which is part of the where clause to retrieve sites.
      */
     public String getSitesWhere9Sql()
     {
         return "UPPER(SAKAI_SITE.TITLE) like UPPER(?||'') and ";
     }
     /**
      * returns the sql statement which is part of the where clause to retrieve sites.
      */
     public String getSitesWhere13Sql()
     {
         return "SAKAI_SITE.SITE_ID in (select SITE_ID from SAKAI_SITE_PROPERTY where NAME = ? and UPPER(VALUE) like UPPER(?||'')) and ";
     }
}

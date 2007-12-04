/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.dao;

import java.util.List;

import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContentPackageDaoImpl extends HibernateDaoSupport implements ContentPackageDao {

	public ContentPackage load(long id) {
		return (ContentPackage)getHibernateTemplate().load(ContentPackage.class, id);
	}
	
	public List<ContentPackage> find(String context) {
		String statement = new StringBuilder("from ").append(ContentPackage.class.getName())
			.append(" where context = ? ").toString();
		
		return getHibernateTemplate().find(statement, new Object[] { context });
	}

	public void save(ContentPackage contentPackage) {
		getHibernateTemplate().saveOrUpdate(contentPackage);
	}

	public void remove(ContentPackage contentPackage) {
		getHibernateTemplate().delete(contentPackage);
	}
	
}

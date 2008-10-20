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
package org.sakaiproject.scorm.dao.hibernate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContentPackageDaoImpl extends HibernateDaoSupport implements ContentPackageDao {

	public int countContentPackages(String context, String name) {
		int count = 1;
		
		List<ContentPackage> contentPackages = find(context);

		for (ContentPackage cp : contentPackages) {
			
			Pattern p = Pattern.compile(name + "\\s*\\(?\\d*\\)?");
			Matcher m = p.matcher(cp.getTitle());
			if (m.matches())
				count++;
			
		}
		
		return count;
	}
	
	public ContentPackage load(long id) {
		return (ContentPackage)getHibernateTemplate().load(ContentPackage.class, id);
	}
	
	public List<ContentPackage> find(String context) {
		String statement = new StringBuilder("from ").append(ContentPackage.class.getName())
			.append(" where context = ? and deleted = ? ").toString();
		
		return getHibernateTemplate().find(statement, new Object[] { context, false });
	}

	public void save(ContentPackage contentPackage) {
		getHibernateTemplate().saveOrUpdate(contentPackage);
	}

	public void remove(ContentPackage contentPackage) {
		contentPackage.setDeleted(true);
		getHibernateTemplate().saveOrUpdate(contentPackage);
	}
	
}

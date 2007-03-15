/**********************************************************************************
 * $URL: 
 * $Id: 
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

package org.sakaiproject.contentreview.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.contentreview.dao.ContentReviewDao;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;

/**
 * Implementations of any specialized DAO methods from the specialized DAO 
 * that allows the developer to extend the functionality of the generic dao package
 * @author Sakai App Builder -AZ
 */
public class ContentReviewDaoImpl 
	extends HibernateCompleteGenericDao 
		implements ContentReviewDao {

	private static Log log = LogFactory.getLog(ContentReviewDaoImpl.class);

	public void init() {
		log.debug("init");
		try{
			super.initDao();
		} catch (Exception e) {
			log.error(e);
		}
	}

}

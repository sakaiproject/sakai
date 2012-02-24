/**
 * $Id: ValidationLogicDao.java 81430 2010-08-18 14:12:46Z david.horwitz@uct.ac.za $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-impl/src/java/org/sakaiproject/accountvalidator/dao/impl/ValidationLogicDao.java $
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */
package org.sakaiproject.content.copyright.api;

import java.util.List;

public interface CopyrightInfo{
	
	public void add(CopyrightItem item);
	public List<CopyrightItem> getItems();
	
}
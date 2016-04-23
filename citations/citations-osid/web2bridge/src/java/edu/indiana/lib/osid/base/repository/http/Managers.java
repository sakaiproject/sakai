package edu.indiana.lib.osid.base.repository.http;

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2007, 2008 The Sakai Foundation
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
/**
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */

 public class Managers extends edu.indiana.lib.osid.base.repository.Managers
 {
	 private static Managers managers = new Managers();
	 private static org.osid.id.IdManager idManager = null;

	 protected static Managers getInstance()
	 {
		 return managers;
	 }

	 public static void setIdManager(org.osid.id.IdManager manager)
	 {
		 idManager = manager;
	 }

	 public static org.osid.id.IdManager getIdManager()
	 {
		 return idManager;
	 }
 }


/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: Dec 1, 2005
 * Time: 12:35:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface JobBeanWrapper {

   public static final String SPRING_BEAN_NAME = "org.sakaiproject.api.app.scheduler.JobBeanWrapper.bean";
   public static final String JOB_TYPE = "org.sakaiproject.api.app.scheduler.JobBeanWrapper.jobType";

   public String getBeanId();

   public Class getJobClass();

   public String getJobType();

}

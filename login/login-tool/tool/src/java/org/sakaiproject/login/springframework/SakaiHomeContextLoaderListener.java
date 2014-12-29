/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.login.springframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ContextLoaderListener;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/29/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class SakaiHomeContextLoaderListener extends ContextLoaderListener {
    private static final Log log = LogFactory.getLog(SakaiHomeContextLoaderListener.class);

    protected org.springframework.web.context.ContextLoader createContextLoader()
   	{
    		return new SakaiHomeContextLoader();
   	}

}

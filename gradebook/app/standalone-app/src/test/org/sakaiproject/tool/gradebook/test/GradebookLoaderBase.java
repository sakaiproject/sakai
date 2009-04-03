/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for data loading. Unlike the standard suite of tests which are run
 * against a standalone implmentations of the facades, this uses the facades implementation
 * defined for the build.
 */
public abstract class GradebookLoaderBase extends GradebookTestBase {
	private static Log log = LogFactory.getLog(GradebookLoaderBase.class);

    protected String[] getConfigLocations() {
        String[] configLocations = {"spring-db.xml", "spring-beans.xml", "spring-facades.xml", "spring-service.xml", "spring-hib.xml",
        	"spring-beans-test.xml",
        	"spring-hib-test.xml",
        	/* SectionAwareness integration support. */
        	"classpath*:org/sakaiproject/component/section/support/spring-integrationSupport.xml",
			/*
				We could just go with
					"classpath*:org/sakaiproject/component/section/spring-*.xml"
				except that for now we need to strip away a transactionManager defined
				in section/spring-hib.xml.
			*/
			"classpath*:org/sakaiproject/component/section/spring-beans.xml",
			"classpath*:org/sakaiproject/component/section/spring-services.xml",
        };
        return configLocations;
    }
}

/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.util;

import uk.org.ponder.rsf.servlet.ServletRootHandlerBean;

/**
 * This class exists because rootHandlerBeanBase has the init-method set, and you can't disable init-methods
 * you can only override them. See <a href="http://www.caret.cam.ac.uk/jira/browse/RSF-123">RSF-123</a> and {@link RootHandlerBeanOverride} 
 * 
 * @author Andrew Thornton.
 * @see RootHandlerBeanOverride
 *
 */
public class OverridedServletRootHandlerBean extends ServletRootHandlerBean {

    /**
     *  This is here because I can't override the init-method of "rootHandlerBeanBase"
     *  without giving it something to override to.
     */
    public void doNothing() {
        
    }
}

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

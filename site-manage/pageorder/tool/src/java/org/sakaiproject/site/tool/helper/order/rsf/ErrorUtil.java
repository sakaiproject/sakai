/*
 * Created on 11 Sep 2008
 */
package org.sakaiproject.site.tool.helper.order.rsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;

public class ErrorUtil {
  private static Log M_log = LogFactory.getLog(PageListProducer.class);
  
    public static final void renderError(UIContainer tofill, Exception e) {
        UIBranchContainer mode = UIBranchContainer.make(tofill, "mode-failed:");
        UIOutput.make(mode, "message", e.getLocalizedMessage());
        
        M_log.warn(e);
    }
}

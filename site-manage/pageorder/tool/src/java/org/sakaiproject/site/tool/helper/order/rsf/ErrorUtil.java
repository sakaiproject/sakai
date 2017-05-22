/*
 * Created on 11 Sep 2008
 */
package org.sakaiproject.site.tool.helper.order.rsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;

public class ErrorUtil {
  private static Logger M_log = LoggerFactory.getLogger(PageListProducer.class);
  
    public static final void renderError(UIContainer tofill, Exception e) {
        UIBranchContainer mode = UIBranchContainer.make(tofill, "mode-failed:");
        UIOutput.make(mode, "message", e.getLocalizedMessage());
        
        M_log.warn(e.getMessage());
    }
}

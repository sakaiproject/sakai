/*
 * Created on 31 Oct 2006
 */
package uk.ac.cam.caret.sakai.rsf.producers;

import org.sakaiproject.tool.api.ToolManager;

import uk.ac.cam.caret.sakai.rsf.copies.Web;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;

/**
 * Emits a Javascript block containing a function call which may be used
 * whenever any AJAX/DOM manipulation has altered the size of the rendered pane.
 * NB - should detect a non-frames portal and emit a noop.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class FrameAdjustingProducer {
  public static final String deriveFrameTitle(String placementID) {
    return Web.escapeJavascript("Main" + placementID);
  }

  private ToolManager toolmanager;

  public void setToolManager(ToolManager toolmanager) {
    this.toolmanager = toolmanager;
  }

  public void fillComponents(UIContainer tofill, String ID, String functionname) {
    UIVerbatim.make(tofill, ID, "\n<!-- \n\tfunction " + functionname + "()"
        + " {\n\t\tsetMainFrameHeight('"
        + deriveFrameTitle(toolmanager.getCurrentPlacement().getId())
        + "');\n\t\t}\n//-->\n");
  }

}

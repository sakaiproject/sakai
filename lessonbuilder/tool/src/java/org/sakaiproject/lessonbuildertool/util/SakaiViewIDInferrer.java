/*
 * Created on 11 Jul 2006
 */
package org.sakaiproject.lessonbuildertool.util;

import java.util.Map;

import uk.org.ponder.rsf.viewstate.StaticViewIDInferrer;
import uk.org.ponder.rsf.viewstate.ViewParamUtil;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/** The RSF default ViewIDInferrer which adopts a fixed strategy of inferring
 * the View ID from a request environment.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *
 */
// TODO: this should ultimately connect up with ViewParamsMapInfo for some kind
// of global inference strategy.
public class SakaiViewIDInferrer implements StaticViewIDInferrer {

  private String viewIDspec = "@0";

    String printArray(String[] a) {
        String ret = "[";
        for (String s:a) {
            ret = ret + s + ",";
        }
        ret = ret + "]";
        return ret;
    }

  public String inferViewID(String[] paths, Map requestmap) {
      if (paths.length == 0)
	  return "";
      else if (paths.length > 1 && paths[paths.length-2].equals("tool"))
          return "ShowPage";
      else
	  return paths[paths.length-1];
  }

  public String getViewIDSpec() {
    return viewIDspec;
  }
  
  public void setViewIDSpec(String viewIDspec) {
    this.viewIDspec = viewIDspec;
  }


}

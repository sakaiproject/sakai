package uk.ac.cam.caret.sakai.rsf.helper;

import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * @author Andrew Thornton
 * 
 */

public class HelperHandlerHook implements HandlerHook {
  private ViewParameters viewParametersProxy;
  private HelperHandlerHookBean hhhb;

  public boolean handle() {
    if (viewParametersProxy.get() instanceof HelperViewParameters) {
      return hhhb.handle();
    }
    else return false;
  }

  public void setViewParametersProxy(ViewParameters viewParameters) {
    this.viewParametersProxy = viewParameters;
  }

  public void setHelperHandlerHookBean(HelperHandlerHookBean hhhb) {
    this.hhhb = hhhb;
  }

}

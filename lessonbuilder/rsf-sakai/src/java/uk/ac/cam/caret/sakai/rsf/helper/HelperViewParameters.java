package uk.ac.cam.caret.sakai.rsf.helper;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * @author Andrew Thornton
 */

public class HelperViewParameters extends SimpleViewParameters {
  /**
   * Name of the component, the value of which is the name of the sakai helper
   * to call
   */
  public static final String HELPER_ID = "helper-id";

  /**
   * Name of the component whose value is the method binding to call after the
   * helper has returned. This is in order to infer the action result, if any is
   * required.
   */
  public static final String POST_HELPER_BINDING = "helper-binding";

  public HelperViewParameters() {
    super();
  }

  public HelperViewParameters(String viewID) {
    super(viewID);
  }
}

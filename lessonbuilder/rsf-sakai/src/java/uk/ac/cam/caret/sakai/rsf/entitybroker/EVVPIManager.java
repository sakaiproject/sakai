/*
 * Created on 18 May 2007
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;

import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

public class EVVPIManager implements ViewParamsReporter {
  private List inferrers;
  private EntityViewAccessRegistrar accessRegistrar;
  private EntityView entityViewProxy;
  private Map inferrermap = new HashMap();
  
  public void setAccessRegistrar(EntityViewAccessRegistrar accessRegistrar) {
    this.accessRegistrar = accessRegistrar;
  }

  public void setEntityViewViewParamsInferrers(List inferrers) {
    this.inferrers = inferrers;
  }
  
  public void init() {
    StringList allprefixes = RegistrationUtil.collectPrefixes(inferrers, inferrermap);
    accessRegistrar.registerPrefixes(allprefixes.toStringArray());
  }

  public void setSakaiEntityView(EntityView entityViewProxy) {
    this.entityViewProxy = entityViewProxy;
  }

  public ViewParameters getViewParameters() {
    EntityViewViewParamsInferrer evvpi = null;
    if (entityViewProxy.getEntityReference() != null) {
      String prefix = entityViewProxy.getEntityReference().prefix;
      evvpi = (EntityViewViewParamsInferrer) inferrermap.get(prefix);
    }
    return evvpi == null? null : evvpi.inferDefaultViewParameters(entityViewProxy);
  }
}

/*
 * Created on 21 Jul 2008
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.springframework.beans.factory.DisposableBean;

import uk.org.ponder.beanutil.WBLAcceptor;
import uk.org.ponder.beanutil.WriteableBeanLocator;

public class EntityViewAccessRegistrar implements EntityViewAccessProvider, DisposableBean {

  private EntityViewAccessProviderManager entityViewAccessProviderManager;

  private CommonAccessHandler commonAccessHandler;

  private String[] prefixes;

  public void setCommonAccessHandler(CommonAccessHandler commonAccessHandler) {
    this.commonAccessHandler = commonAccessHandler;
  }

  public void setEntityViewAccessProviderManager(
      EntityViewAccessProviderManager entityViewAccessProviderManager) {
    this.entityViewAccessProviderManager = entityViewAccessProviderManager;
  }

  public void registerPrefixes(String[] prefixes) {
    this.prefixes = prefixes;
    for (int i = 0; i < prefixes.length; ++i) {
      entityViewAccessProviderManager.registerProvider(prefixes[i], this);
    }
  }

  public void destroy() {
    if (prefixes != null) {
      for (int i = 0; i < prefixes.length; ++i) {
        entityViewAccessProviderManager.unregisterProvider(prefixes[i]);
      }
    }
  }
  
  
  public void handleAccess(final EntityView view, HttpServletRequest req,
      HttpServletResponse res) {
    commonAccessHandler.handleAccess(req, res, view.getEntityReference(), new WBLAcceptor() {
      public Object acceptWBL(WriteableBeanLocator toaccept) {
        toaccept.set("sakai-EntityView", view);
        return null;
      }});
  }

}

/*
 * Created on Dec 7, 2006
 */
package uk.ac.cam.caret.sakai.rsf.genericdao;

import org.sakaiproject.genericdao.api.CoreGenericDao;

import uk.org.ponder.util.RunnableInvoker;

public class GenericDAORunnableInvoker implements RunnableInvoker {
  private CoreGenericDao genericDAO;

  public void setGenericDAO(CoreGenericDao genericDAO) {
    this.genericDAO = genericDAO;
  }
  
  public void invokeRunnable(Runnable torun) {
    genericDAO.invokeTransactionalAccess(torun);
  }
}

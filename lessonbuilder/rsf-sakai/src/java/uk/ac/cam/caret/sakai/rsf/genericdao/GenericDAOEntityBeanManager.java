/*
 * Created on Dec 3, 2006
 */
package uk.ac.cam.caret.sakai.rsf.genericdao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.genericdao.api.CoreGenericDao;
import org.sakaiproject.genericdao.api.InitializingCoreGenericDAO;

import uk.org.ponder.beanutil.FallbackBeanLocator;
import uk.org.ponder.rsf.state.entity.DefaultEntityMapper;
import uk.org.ponder.rsf.state.entity.EntityNameInferrer;
import uk.org.ponder.rsf.state.entity.support.BasicObstinateEBL;
import uk.org.ponder.saxalizer.AccessMethod;
import uk.org.ponder.saxalizer.support.MethodAnalyser;
import uk.org.ponder.saxalizer.SAXalizerMappingContext;
import uk.org.ponder.util.ObjectFactory;

public class GenericDAOEntityBeanManager implements FallbackBeanLocator,
    EntityNameInferrer {
  private Map locators = new HashMap();

  private CoreGenericDao genericDAO;
  private SAXalizerMappingContext mappingcontext;

  public void setGenericDAO(CoreGenericDao genericDAO) {
    this.genericDAO = genericDAO;
  }

  public void setMappingContext(SAXalizerMappingContext mappingcontext) {
    this.mappingcontext = mappingcontext;
  }

  public String getEntityName(Class entityclazz) {
    // NB, this is the H2 implementation, until GenericDAO is upgraded to use
    // entity names.
    String classname = entityclazz.getName();
    int lastdotpos = classname.lastIndexOf('.');
    String nick = classname.substring(lastdotpos + 1);
    return nick;
  }

  public void init() {
    List classes = genericDAO.getPersistentClasses();
    for (int i = 0; i < classes.size(); ++i) {
      Class clazz = (Class) classes.get(i);
      String idprop = genericDAO.getIdProperty(clazz);
      MethodAnalyser ma = mappingcontext.getAnalyser(clazz);
      AccessMethod am = ma.getAccessMethod(idprop);
      Class idclazz = am.getDeclaredType();

      DefaultEntityMapper dem = new DefaultEntityMapper();
      dem.setEntityClass(clazz);
      dem.setIDClass(idclazz);
      dem.setMappingContext(mappingcontext);
      if (genericDAO instanceof InitializingCoreGenericDAO) {
        dem.setObjectFactory(new ObjectFactory() {
          public Object getObject() {
            return ((InitializingCoreGenericDAO)genericDAO).instantiate();
          }});
      }

      GenericDAOEntityHandler gdeh = new GenericDAOEntityHandler();
      gdeh.setGenericDAO(genericDAO);
      gdeh.setPersistentClass(clazz);
      BasicObstinateEBL boebl = new BasicObstinateEBL();
      boebl.setEntityHandler(gdeh);
      boebl.setEntityMapper(dem);
      String entityname = getEntityName(clazz);
      locators.put(entityname, boebl);
    }
  }

  public Object locateBean(String path) {
    return locators.get(path);
  }

}

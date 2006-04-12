/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.component.message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.service.framework.log.Logger;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerHandler;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.TriggerDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;

/**
 * @author ieb
 *
 */
public class TriggerServiceImpl implements TriggerService {
    private Logger log;

    private TriggerDao triggerDao;
    private Map triggerHandlers;

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#fireSpaceTriggers(java.lang.String)
     */
    public void fireSpaceTriggers(String space) {
        List l = triggerDao.findBySpace(space);
        for ( Iterator il = l.iterator(); il.hasNext();) {
            Trigger t = (Trigger) il.next();
            String triggerSpec = t.getTriggerspec();
            TriggerHandler ts = (TriggerHandler) triggerHandlers.get(triggerSpec);
            if ( ts != null ) {
                ts.fireOnSpace(space);
            }
        }
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#firePageTriggers(java.lang.String, java.lang.String)
     */
    public void firePageTriggers(String space, String page) {
        List l = triggerDao.findByPage(space,page);
        for ( Iterator il = l.iterator(); il.hasNext();) {
            Trigger t = (Trigger) il.next();
            String triggerSpec = t.getTriggerspec();
            TriggerHandler ts = (TriggerHandler) triggerHandlers.get(triggerSpec);
            if ( ts != null ) {
                ts.fireOnPage(space,page);
            }
        }
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#addTrigger(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addTrigger(String user, String space, String page, String spec) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#removeTrigger(uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger)
     */
    public void removeTrigger(Trigger trigger) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#updateTrigger(uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger)
     */
    public void updateTrigger(Trigger trigger) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String, java.lang.String, java.lang.String)
     */
    public List getUserTriggers(String user, String space, String page) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String, java.lang.String)
     */
    public List getUserTriggers(String user, String space) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getUserTriggers(java.lang.String)
     */
    public List getUserTriggers(String user) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getPageTriggers(java.lang.String, java.lang.String)
     */
    public List getPageTriggers(String space, String page) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.TriggerService#getSpaceTriggers(java.lang.String)
     */
    public List getSpaceTriggers(String space) {
        // TODO Auto-generated method stub
        return null;
    }
    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * @return Returns the triggerDao.
     */
    public TriggerDao getTriggerDao() {
        return triggerDao;
    }

    /**
     * @param triggerDao The triggerDao to set.
     */
    public void setTriggerDao(TriggerDao triggerDao) {
        this.triggerDao = triggerDao;
    }

    /**
     * @return Returns the triggerHandlers.
     */
    public Map getTriggerHandlers() {
        return triggerHandlers;
    }

    /**
     * @param triggerHandlers The triggerHandlers to set.
     */
    public void setTriggerHandlers(Map triggerHandlers) {
        this.triggerHandlers = triggerHandlers;
    }


}

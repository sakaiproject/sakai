package org.sakaiproject.blti.entityprovider;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;


/**
 * This is a simple skeleton to demonstrate how this is done
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class SkeletonEntityProvider extends AbstractEntityProvider 
    implements EntityProvider, ActionsExecutable /*, RESTful */ {

    @EntityCustomAction
    public String hello(EntityView ev) {
        // a url like /direct/blti-skeleton/hello/aaron will cause this to output "hello aaron"
        String name = ev.getPathSegment(2);
        return "hello "+name;
    }

    @EntityCustomAction(action="goodbye", viewKey=EntityView.VIEW_SHOW)
    public String doGoodByeAction(EntityView ev) {
        // a url like /direct/blti-skeleton/goodbye/aaron will cause this to output "goodbye aaron", GET only
        String name = ev.getPathSegment(2);
        return "goodbye "+name;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    public String getEntityPrefix() {
        return "blti-skeleton"; // TODO change this to something real
    }


    /* TODO if you want to do all the REST basics then uncomment this section and the implements "Restful" above 

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getSampleEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        // TODO Auto-generated method stub
        
    }

    public Object getEntity(EntityReference ref) {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        // TODO Auto-generated method stub
        
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.TXT, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.JSON, Formats.XML};
    }
    
    */

}

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroupService;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupCollectionBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

public class AuthZGroupCollectionBeanHelper {

    public static AuthZGroupCollectionBean createAuthZCollectionBean(AuthzGroupService realmService, RWikiObject currentRWikiObject, ViewBean viewBean, RWikiObjectService objectService) {
        Entity entity = objectService.getEntity(currentRWikiObject);
        
        Collection groupRefs = objectService.getEntityAuthzGroups(EntityManager.newReference(entity.getReference()));
        
        List groups = new ArrayList(groupRefs.size());
        
        for (Iterator it = groupRefs.iterator(); it.hasNext(); ) {
            String groupRef = (String) it.next();
            AuthZGroupBean ab = new AuthZGroupBean(viewBean.getPageName(), viewBean.getLocalSpace());
            ab.setRealmId(groupRef);
            try {
            	
                AuthzGroup azg = realmService.getAuthzGroup(groupRef);
                ab.setCurrentRealm(azg);
                ab.setRealmId(azg.getId());
                Logger.info("Got Id "+ groupRef);
            } catch (IdUnusedException e) {
            	   Logger.info("Id Unused: " + groupRef + " doesnt exist for this user . ");
            }
             
            groups.add(ab);
        }
        
        AuthZGroupCollectionBean collectionBean = new AuthZGroupCollectionBean();
        collectionBean.setCurrentRealms(groups);
        collectionBean.setVb(viewBean);        
        return collectionBean;
    }
    

}

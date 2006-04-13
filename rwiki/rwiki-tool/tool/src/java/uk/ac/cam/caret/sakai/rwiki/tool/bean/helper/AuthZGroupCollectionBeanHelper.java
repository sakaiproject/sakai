package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.cover.EntityManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupCollectionBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

public class AuthZGroupCollectionBeanHelper
{
	private static Log log = LogFactory
			.getLog(AuthZGroupCollectionBeanHelper.class);

	public static AuthZGroupCollectionBean createAuthZCollectionBean(
			AuthzGroupService realmService, RWikiObject currentRWikiObject,
			ViewBean viewBean, RWikiObjectService objectService)
	{
		Entity entity = objectService.getEntity(currentRWikiObject);

		Collection groupRefs = objectService.getEntityAuthzGroups(EntityManager
				.newReference(entity.getReference()));

		List groups = new ArrayList(groupRefs.size());

		for (Iterator it = groupRefs.iterator(); it.hasNext();)
		{
			String groupRef = (String) it.next();
			AuthZGroupBean ab = new AuthZGroupBean(viewBean.getPageName(),
					viewBean.getLocalSpace());
			ab.setRealmId(groupRef);
			try
			{

				AuthzGroup azg = realmService.getAuthzGroup(groupRef);
				ab.setCurrentRealm(azg);
				ab.setRealmId(azg.getId());
				log.info("Got Id " + groupRef);
			}
			catch (GroupNotDefinedException e)
			{
				log.info("Id Unused: " + groupRef
						+ " doesnt exist for this user . ");
			}

			groups.add(ab);
		}

		AuthZGroupCollectionBean collectionBean = new AuthZGroupCollectionBean();
		collectionBean.setCurrentRealms(groups);
		collectionBean.setVb(viewBean);
		return collectionBean;
	}

}

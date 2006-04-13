package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

//FIXME: Tool

public class AuthZGroupBeanHelper
{

	public static AuthZGroupBean createRealmBean(
			AuthzGroupService realmService, RWikiObject rwikiObject,
			ErrorBean errorBean, ViewBean vb)
	{
		AuthZGroupBean rb = new AuthZGroupBean(vb.getPageName(), vb
				.getLocalSpace());

		String realmId = rwikiObject.getRealm();
		rb.setRealmId(realmId);
		try
		{
			AuthzGroup realm = realmService.getAuthzGroup(realmId);
			rb.setCurrentRealm(realm);
		}
		catch (GroupNotDefinedException e)
		{
			// FIXME localise!
			errorBean.addError("Realm: " + realmId
					+ " is not recognised in the system.");
		}

		rb.setSiteUpdateAllowed(realmService.allowUpdate(realmId));

		return rb;
	}

}

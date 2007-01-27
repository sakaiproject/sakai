package org.sakaiproject.portal.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Properties;
import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortletApplicationDescriptor;
import org.sakaiproject.portal.api.PortletDescriptor;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.util.Web;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.authz.cover.SecurityService;

public class PortalServiceImpl implements PortalService
{
	private static final Log log = LogFactory.getLog(PortalServiceImpl.class);

	/**
	 * Parameter to force state reset
	 */
	public static final String PARM_STATE_RESET = "sakai.state.reset";

	public StoredState getStoredState()
	{
		Session s = SessionManager.getCurrentSession();
		StoredState ss = (StoredState) s.getAttribute("direct-stored-state");
		return ss;
	}

	public void setStoredState(StoredState ss)
	{
		Session s = SessionManager.getCurrentSession();
		if (s.getAttribute("direct-stored-state") == null || ss == null)
		{
			s.setAttribute("direct-stored-state", ss);
		}
	}

	// To allow us to retain reset state across redirects
	public String getResetState()
	{
		Session s = SessionManager.getCurrentSession();
		String ss = (String) s.getAttribute("reset-stored-state");
		return ss;
	}

	public void setResetState(String ss)
	{
		Session s = SessionManager.getCurrentSession();
		if (s.getAttribute("reset-stored-state") == null || ss == null)
		{
			s.setAttribute("reset-stored-state", ss);
		}
	}

	public boolean isEnableDirect()
	{
		return "true".equals(ServerConfigurationService.getString(
				"charon.directurl", "true"));
	}

	public boolean isResetRequested(HttpServletRequest req)
	{
		return "true".equals(req.getParameter(PARM_STATE_RESET))
				|| "true".equals(getResetState());
	}

	public String getResetStateParam()
	{
		// TODO Auto-generated method stub
		return PARM_STATE_RESET;
	}

	public StoredState newStoredState(String marker, String replacement)
	{
		return new StoredStateImpl(marker, replacement);
	}

	public Iterator<PortletApplicationDescriptor> getRegisteredApplications()
	{
		try
		{
			PortletRegistryService registry = PortletContextManager
					.getManager();
			final Iterator apps = registry.getRegisteredPortletApplications();
			return new Iterator<PortletApplicationDescriptor>()
			{

				public boolean hasNext()
				{
					return apps.hasNext();
				}

				public PortletApplicationDescriptor next()
				{
					final InternalPortletContext pc = (InternalPortletContext) apps
							.next();

					final PortletAppDD appDD = pc
							.getPortletApplicationDefinition();
					return new PortletApplicationDescriptor()
					{

						public String getApplicationContext()
						{
							return pc.getPortletContextName();
						}

						public String getApplicationId()
						{
							return pc.getApplicationId();
						}

						public String getApplicationName()
						{
							return pc.getApplicationId();
						}

						public Iterator<PortletDescriptor> getPortlets()
						{
							if (appDD != null)
							{
								List portlets = appDD.getPortlets();

								final Iterator portletsI = portlets.iterator();
								return new Iterator<PortletDescriptor>()
								{

									public boolean hasNext()
									{
										return portletsI.hasNext();
									}

									public PortletDescriptor next()
									{
										final PortletDD pdd = (PortletDD) portletsI
												.next();
										return new PortletDescriptor()
										{

											public String getPortletId()
											{
												return pdd.getPortletName();
											}

											public String getPortletName()
											{
												return pdd.getPortletName();
											}

										};
									}

									public void remove()
									{
									}

								};
							}
							else
							{
								log
										.warn(" Portlet Application has no portlets "
												+ pc.getPortletContextName());
								return new Iterator<PortletDescriptor>()
								{

									public boolean hasNext()
									{
										return false;
									}

									public PortletDescriptor next()
									{
										return null;
									}

									public void remove()
									{
									}

								};
							}
						}

					};
				}

				public void remove()
				{
				}

			};
		}
		catch (PortletContainerException e)
		{
			log.error("Failed to get portlet applications ", e);
		}
		return new Iterator<PortletApplicationDescriptor>()
		{

			public boolean hasNext()
			{
				return false;
			}

			public PortletApplicationDescriptor next()
			{
				return null;
			}

			public void remove()
			{
			}

		};
	}

    public boolean allowTool(Site site, Placement placement) {
	// No way to render an opinion
        if (placement == null || site == null) return true; 

        boolean retval = true;

        String TOOL_CFG_FUNCTIONS = "functions.require";
        Properties roleConfig = placement.getConfig();
        String roleList = roleConfig.getProperty(TOOL_CFG_FUNCTIONS);

        // allow by default, when no config keys are present
        if (roleList != null && roleList.trim().length() > 0) {
            String[] result = roleConfig.getProperty(TOOL_CFG_FUNCTIONS).split(
                "\\,");
            for (int x = 0; x < result.length; x++) {
                if (!SecurityService.unlock(result[x].trim(), site
                    .getReference())) retval = false;
            }
        }
        return retval;
    }

    // Determine if we are to do multiple tabs for the anonymous view (Gateway)
    public boolean doGatewaySiteList()
    {
	String gatewaySiteListPref = ServerConfigurationService.getString("gatewaySiteList");
	if ( gatewaySiteListPref == null ) return false;
	return ( gatewaySiteListPref.trim().length() > 0 );
    }

    // Return the list of tabs for the anonymous view (Gateway)
    private String[] getGatewaySiteList()
    {
	String gatewaySiteListPref = ServerConfigurationService.getString("gatewaySiteList");

	if ( gatewaySiteListPref == null ) return null;
	if ( gatewaySiteListPref.trim().length() < 1 ) return null;

	String[] gatewaySites = gatewaySiteListPref.split(",");
	if ( gatewaySites.length < 1 ) return null;

	return gatewaySites;
    }

    // Get the sites which are to be displayed for the gateway
    private List getGatewaySites()
    {
	List mySites = new Vector();
	String[] gatewaySiteIds = getGatewaySiteList();
	if ( gatewaySiteIds == null ) 
	{
		return mySites;  // An empty list - deal with this higher up in the food chain
	}

	// Loop throught the sites making sure they exist and are visitable
	for ( int i = 0; i < gatewaySiteIds.length; i++ ) 
	{
	    String siteId = gatewaySiteIds[i];
	
            Site site = null;
            try {
                site = getSiteVisit(siteId);
            }
            catch (IdUnusedException e) {
		continue;
            }
            catch (PermissionException e) {
		continue;
            }

	    if ( site != null ) {
		mySites.add(site);
	    }
        }

	if ( mySites.size() < 1 ) {
            log
                .warn("No suitable gateway sites found, gatewaySiteList preference had "
			+gatewaySiteIds.length+" sites.");
	}
	return mySites;
    }

    /* 
     * Get All Sites for the current user.  If the user is not logged in we return the 
     * list of publically viewable gateway sites.
     *
     * @param includeMyWorkspace When this is true - include the user's My Workspace as the 
     * first parameter.  If false, do not include the MyWorkspace anywhere in the list.
     *
     * Some uses - such as the portlet styled portal or the rss styled portal simply want all of the
     * sites with the MyWorkspace first.  Other portals like the basic tabbed portal treats 
     * My Workspace separately from all of the rest of the workspaces.
     */
     
    public List getAllSites(HttpServletRequest req, Session session,
                               boolean includeMyWorkspace) throws IOException {

            boolean loggedIn = session.getUserId() != null;

	    // Get the list of sites in the right order
	    List mySites;
	    if ( ! loggedIn ) {
            	// collect the Publically Viewable Sites
            	mySites = getGatewaySites();
	    } else {
            	// collect the user's sites
            	mySites = SiteService.getSites(
                	org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
                	null, null, null,
                	org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
                	null);

                // collect the user's preferences
                List prefExclude = new Vector();
                List prefOrder = new Vector();
                if (session.getUserId() != null) {
                    Preferences prefs = PreferencesService.getPreferences(session
                        .getUserId());
                    ResourceProperties props = prefs
                        .getProperties("sakai:portal:sitenav");

                    List l = props.getPropertyList("exclude");
                    if (l != null) {
                        prefExclude = l;
                    }

                    l = props.getPropertyList("order");
                    if (l != null) {
                        prefOrder = l;
                    }
                }

                // remove all in exclude from mySites
                mySites.removeAll(prefExclude);

                // Prepare to put sites in the right order
                List ordered = new Vector();

	    	// First, place or remove MyWorkspace as requested
	    	Site myWorkspace = getMyWorkspace(session);
		if ( myWorkspace !=  null ) {
			if ( includeMyWorkspace ) 
	        	{
				ordered.add(myWorkspace);
			} 
			else 
			{
                    		int pos = listIndexOf(myWorkspace.getId(), mySites);
                    		if (pos != -1) mySites.remove(pos);
                    	}
		}

		// re-order mySites to have order first, the rest later
                for (Iterator i = prefOrder.iterator(); i.hasNext();) {
                    String id = (String) i.next();

                    // find this site in the mySites list
                    int pos = listIndexOf(id, mySites);
                    if (pos != -1) {
                        // move it from mySites to order
                        Site s = (Site) mySites.get(pos);
                        ordered.add(s);
                        mySites.remove(pos);
                    }
                }

                // pick up the rest of the sites
                ordered.addAll(mySites);
                mySites = ordered;
            }  // End if ( loggedIn )

/*            for (Iterator i = mySites.iterator(); i.hasNext();) {
                Site s = (Site) i.next();
                System.out.println("Site:"+Web.escapeHtml(s.getTitle())+" id="+s.getId());
            }
*/
	    return mySites;
    }

    public Site getMyWorkspace(Session session)
    {
        String siteId = SiteService.getUserSiteId(session.getUserId());

	// Make sure we can visit
        Site site = null;
        try {
            site = getSiteVisit(siteId);
        }
        catch (IdUnusedException e) {
            site = null;
        }
        catch (PermissionException e) {
            site = null;
        }

	return site;
    }

    public Map convertSiteToMap(HttpServletRequest req, Site s, String prefix, 
	String currentSiteId, String myWorkspaceSiteId)
    {
	if ( s == null ) return null;
       	Map m = new HashMap();
       	m.put("isCurrentSite", Boolean.valueOf(currentSiteId != null && s.getId().equals(currentSiteId)));
       	m.put("isMyWorkspace", Boolean.valueOf(myWorkspaceSiteId != null && s.getId().equals(myWorkspaceSiteId)));
       	m.put("siteTitle", Web.escapeHtml(s.getTitle()));
       	String siteUrl = Web.serverUrl(req)
           	  + ServerConfigurationService.getString("portalPath")
           	  + "/" + prefix + "/"
           	  + Web.escapeUrl(getSiteEffectiveId(s));
       	m.put("siteUrl", siteUrl);
	return m;
    }

    public List convertSitesToMaps(HttpServletRequest req, List mySites, String prefix,
	String currentSiteId, String myWorkspaceSiteId)
    {
            List l = new ArrayList();
            // first n tabs
            for (Iterator i = mySites.iterator(); i.hasNext();) {
                Site s = (Site) i.next();
               l.add(convertSiteToMap(req, s, prefix, currentSiteId, myWorkspaceSiteId) );
            }
	    return l;
    }

    /**
     * If this is a user site, return an id based on the user EID, otherwise
     * just return the site id.
     *
     * @param site The site.
     * @return The effective site id.
     */
    public String getSiteEffectiveId(Site site) {
        if (SiteService.isUserSite(site.getId())) {
            try {
                String userId = SiteService.getSiteUserId(site.getId());
                String eid = UserDirectoryService.getUserEid(userId);
                return SiteService.getUserSiteId(eid);
            }
            catch (UserNotDefinedException e) {
                log
                    .warn("getSiteEffectiveId: user eid not found for user site: "
                        + site.getId());
            }
        }

        return site.getId();
    }

    /**
     * Do the getSiteVisit, but if not found and the id is a user site, try
     * translating from user EID to ID.
     *
     * @param siteId The Site Id.
     * @return The Site.
     * @throws PermissionException If not allowed.
     * @throws IdUnusedException   If not found.
     */
    public Site getSiteVisit(String siteId) throws PermissionException,
        IdUnusedException {
        try {
            return SiteService.getSiteVisit(siteId);
        }
        catch (IdUnusedException e) {
            if (SiteService.isUserSite(siteId)) {
                try {
                    String userEid = SiteService.getSiteUserId(siteId);
                    String userId = UserDirectoryService.getUserId(userEid);
                    String alternateSiteId = SiteService.getUserSiteId(userId);
                    return SiteService.getSiteVisit(alternateSiteId);
                }
                catch (UserNotDefinedException ee) {
                }
            }

            // re-throw if that didn't work
            throw e;
        }
    }

    /**
     * Find the site in the list that has this id - return the position.
     *
     * @param value    The site id to find.
     * @param siteList The list of Site objects.
     * @return The index position in siteList of the site with site id = value,
     *         or -1 if not found.
     */
    private int listIndexOf(String value, List siteList) {
        for (int i = 0; i < siteList.size(); i++) {
            Site site = (Site) siteList.get(i);
            if (site.equals(value)) {
                return i;
            }
        }

        return -1;
    }

}

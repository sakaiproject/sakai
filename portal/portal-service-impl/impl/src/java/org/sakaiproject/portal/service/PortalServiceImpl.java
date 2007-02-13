package org.sakaiproject.portal.service;

import java.util.Iterator;
import java.util.List;

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


}

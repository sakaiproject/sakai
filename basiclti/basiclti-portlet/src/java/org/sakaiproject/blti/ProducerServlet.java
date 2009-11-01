package org.sakaiproject.blti;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.OAuthSignatureMethod;
import net.oauth.server.HttpRequestMessage;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.cover.ServerConfigurationService;

public class ProducerServlet extends HttpServlet {

	private static ResourceLoader rb = new ResourceLoader("basiclti");

	private static final String BASICLTI_RESOURCE_LINK = "blti:resource_link_id";

	/**
         * Setup a security advisor.
         */
        public void pushAdvisor()
        {
                // setup a security advisor
                SecurityService.pushAdvisor(new SecurityAdvisor()
                {
                        public SecurityAdvice isAllowed(String userId, String function, String reference)
                        {
                                  return SecurityAdvice.ALLOWED;
                        }
                });
        }

        /**
         * Remove our security advisor.
         */
        public void popAdvisor()
        {
                SecurityService.popAdvisor();
        }

	public void doError(HttpServletResponse response, String s)
		throws java.io.IOException
	{
		PrintWriter out = response.getWriter();
  		out.println(rb.getString(s));
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		Session sess = SessionManager.getCurrentSession();
		if ( sess == null ) {
		PrintWriter out = response.getWriter();
			doError(response,"launch.no.session");
			return;
		}

		if ( sess != null ) System.out.println("ID="+sess.getId());

		// response.setContentType("text/html");

		String oauth_consumer_key = request.getParameter("oauth_consumer_key");
		String user_id = request.getParameter("user_id");
		String context_id = request.getParameter("context_id");
		String fname = request.getParameter("lis_person_name_given");
		String lname = request.getParameter("lis_person_name_family");
		String email = request.getParameter("lis_person_contact_email_primary");
		String resource_link_id = request.getParameter("resource_link_id");
		if ( ! "basic-lti-launch-request".equals(request.getParameter("lti_message_type")) ||
		    ! "LTI-1p0".equals(request.getParameter("lti_version")) ||
		    oauth_consumer_key == null || resource_link_id == null ) {
			doError(response, "launch.missing");
			return;
		}

		// Check the Tool ID
		String tool_id = request.getParameter("tool_id");
		Tool toolCheck = null;
		if ( tool_id != null ) {
			toolCheck = ToolManager.getTool(tool_id);
		}
		if ( toolCheck == null ) {
			doError(response,"launch.bad.tool_id");
			return;
		}

		// Construct the eid
		String eid = null;
		if ( user_id != null ) {
			eid = oauth_consumer_key + ":" + user_id;
		}

		String fullname = request.getParameter("lis_person_name_full");
		if ( fname == null && lname == null && fullname != null ) {
			int ipos = fullname.trim().lastIndexOf(' ');
			if ( ipos == -1 ) {
				fname = fullname;
			} else {
				fname = fullname.substring(0,ipos);
				lname = fullname.substring(ipos+1);
			}
		}

		PrintWriter out = response.getWriter();

		OAuthMessage oam = OAuthServlet.getMessage(request, null);
		OAuthValidator oav = new SimpleOAuthValidator();
		OAuthConsumer cons = null;
		if ( "lmsng.school.edu".equals(oauth_consumer_key) ) {
			cons = new OAuthConsumer("http://call.back.url.com/", "lmsng.school.edu", "secret", null);
		} else if ( "12345".equals(oauth_consumer_key) ) {
			cons = new OAuthConsumer("http://call.back.url.com/", "12345", "secret", null);
		} else {
			out.println("<b>oauth_consumer_key="+oauth_consumer_key+" not found.</b>\n");
			return;
		}

		OAuthAccessor acc = new OAuthAccessor(cons);

		try {
			out.println("\n<b>Base Message</b>\n</pre><p>\n");
			out.println(OAuthSignatureMethod.getBaseString(oam));
			out.println("<pre>\n");
			oav.validateMessage(oam,acc);
			out.println("Message validated");
		} catch(Exception e) {
			out.println("<b>Error while valdating message:</b>\n");
			out.println(e);
		}

		// Create the User's account if it does not exist
		if ( eid != null ) {

                	User user = null;

                	try {
                        	user = UserDirectoryService.getUserByEid(eid);
                	}
                	catch(Exception e) {
                        	user = null;
                	}

                	if (user == null ) {
                        	try {
                                	String hiddenPW = IdManager.createUuid();
                                	UserDirectoryService.addUser(null,eid,fname,lname,email,hiddenPW,"registered", null);
                                	user = UserDirectoryService.getUserByEid(eid);
                        	}
                        	catch(Exception e) {
                                	out.println("Unable to create user.");
                        	}

                	}

                       	String ipAddress = request.getRemoteAddr();
                       	UsageSessionService.login(user.getId(), eid, ipAddress, null, UsageSessionService.EVENT_LOGIN_WS);
                       	sess.setUserId(user.getId());
                       	sess.setUserEid(user.getEid());

                       	// update the user's externally provided realm definitions
                       	// authzGroupService().refreshUser(user.getUid());

                       	// post the login event
                       	// eventTrackingService().post(eventTrackingService().newEvent(EVENT_LOGIN, null, true));

                       	out.println("YAYAYAY");
		}

		// Contextualize the context_id with the OAuth consumer key
		// Also use the resource_link_id for the context_id if we did not
		// get a context_id
        	if ( context_id == null ) context_id = "res:" + resource_link_id;
		context_id = oauth_consumer_key + ":" + context_id;

		Site thesite = null;
		try {
			thesite = SiteService.getSite(context_id);
		}
		catch (Exception e) {  
			System.out.println("CRAP");
		}

		// Create the site if it does not exist
  		if ( thesite == null ) {
			String context_type = request.getParameter("context_type");
			String sakai_type = "project";
			if ( context_type != null && context_type.toLowerCase().indexOf("course") > -1 ) {
				sakai_type = "course";
			} 
			String context_title = request.getParameter("context_title");
			String context_label = request.getParameter("context_label");
			try {

				Site siteEdit = null;
				siteEdit = SiteService.addSite(context_id, sakai_type);
				if ( context_title != null ) siteEdit.setTitle(context_title);
				// siteEdit.setDescription(description);
				if ( context_label != null ) siteEdit.setShortDescription(context_label);
				//siteEdit.setIconUrl(iconurl);
				//siteEdit.setInfoUrl(infourl);
				siteEdit.setJoinable(false);
				//siteEdit.setJoinerRole(joinerrole);
				siteEdit.setPublished(true);
				siteEdit.setPubView(false);
				// siteEdit.setSkin(skin);
				siteEdit.setType(sakai_type);
	System.out.println("Creating a new site...");
				SiteService.save(siteEdit);
			}
			catch (Exception e) {  
				System.out.println("CRAP");
			}
  		}

		// Add the current user to the site with the proper role
		try {
			thesite = SiteService.getSite(context_id);
			Set<Role> roles = thesite.getRoles();

			User theuser = UserDirectoryService.getUserByEid(eid);
			String userrole = request.getParameter("roles");
			boolean CHECK_JOINED = false;
			if ("instructor".equalsIgnoreCase(userrole) ) {
				thesite.addMember(theuser.getId(), "maintain", true, true);
				CHECK_JOINED = true;
			}
			else {
				for (Role r : roles) {
					//scan available roles and join with requested role if possible
					if (r.getId().equalsIgnoreCase(userrole)) {
						try {
							thesite.addMember(theuser.getId(), userrole, true, true);
							CHECK_JOINED = true;
						}catch(Exception e) {
							CHECK_JOINED = false;
						}
					}
				}
			}

			//last ditch effort to join the site
			if (!CHECK_JOINED) 
				try {
					SiteService.join(context_id);
					CHECK_JOINED = true;
				} catch (Exception e) {
					CHECK_JOINED = false;
				}
		} catch(Exception e) {
			System.out.println("Could not join site " + context_id);
		}

		// See if we already have created the tool
		String placement_id = null;
        	try {

                	List pageEdits = thesite.getPages();
                	for (Iterator i = pageEdits.iterator(); i.hasNext();)
                	{
                        	SitePage pageEdit = (SitePage) i.next();
                        	List toolEdits = pageEdit.getTools();
                        	for (Iterator j = toolEdits.iterator(); j.hasNext();)
                        	{
                                	ToolConfiguration tool = (ToolConfiguration) j.next();
                                	Tool t = tool.getTool();
                                	Properties propsedit = tool.getPlacementConfig();
					String rli = propsedit.getProperty(BASICLTI_RESOURCE_LINK, null);
					if ( resource_link_id.equals(rli) ) {
						placement_id = tool.getId();
						System.out.println("Found the placement="+placement_id);
						break;
					}
                        	}
				if ( placement_id != null ) break;
                	}

        	}
        	catch (Exception e) {
			System.out.println("SLKASLKJSALKSAJLK");
        	}

		// If the tool is not in the site, add the tool
		if ( placement_id == null ) {
			try {
				System.out.println("Adding a page...");
                		SitePage sitePageEdit = null;
                		sitePageEdit = thesite.addPage();
                		sitePageEdit.setTitle(tool_id);
				ToolConfiguration tool = sitePageEdit.addTool();
				Tool t = tool.getTool();

				tool.setTool(tool_id, ToolManager.getTool(tool_id));
				tool.setTitle(tool_id);
				Properties propsedit = tool.getPlacementConfig();
				propsedit.setProperty(BASICLTI_RESOURCE_LINK, resource_link_id);
				pushAdvisor();
				try {
                			SiteService.save(thesite);
				}
        			catch (Exception e) {
					System.out.println("Site Save Failed "+e);
        			}
            			finally
                		{
                        		popAdvisor();
                		}
				placement_id = tool.getId();
        		}
        		catch (Exception e) {
				System.out.println("Page add Failed "+e);
        		}
		}

		String toolLink = ServerConfigurationService.getPortalUrl() + "/tool-reset/" + placement_id + "?panel=Main";
		System.out.println("DAMN="+toolLink);
		out.println("<a href=\""+toolLink+"\" target=\"_new\">"+toolLink+"</a>");
		out.close();

	}

	public void destroy() {

	}

}

package uk.ac.cam.caret.sakai.rwiki.tool.entityproviders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * Provides the /direct/wiki REST endpoint.
 * 
 * GET a URL like this /direct/wiki/site/SITEID.json and you'll get a graph of wiki
 * pages for the site specified. The graph contains page urls as well as names.
 * 
 * GET a URL like this /direct/wiki/site/SITEID/page/PAGENAME.json and you'll get the
 * specified page as a JSON object.
 *  
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Slf4j
public class RWikiEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, ActionsExecutable, Outputable,Describeable {

	public final static String ENTITY_PREFIX = "wiki";
	
	@Setter
	private RWikiObjectService objectService;
	
	@Setter
	private RenderService renderService;
	
	@Setter
	private UserDirectoryService userDirectoryService;

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.XML };
	}
	
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public Object handleSite(EntityView view, Map<String, Object> params) {
		
		String userId = developerHelperService.getCurrentUserId();
		
		if(userId == null) {
			throw new EntityException("You must be logged in to retrieve pages","",HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		String format = view.getFormat();
		
		if(view.getPathSegments().length == 3) {
			
			// This is a request for the site's page graph
		
			String siteId = view.getPathSegment(2);
        
			SparserPage homePage = new SparserPage("home",siteId, format);
        
			getSubPages(homePage, "/site/" + siteId, siteId, userId, format);
        
			return homePage;
			
		} else if(view.getPathSegments().length == 5) {
			
			// This is a request for a page
			
			String siteId = view.getPathSegment(2);
			
			String pageName = view.getPathSegment(4);
			
			// Construct the page name and get the content.
			String defaultRealm = "/site/" + siteId;
			if(objectService.exists(pageName,defaultRealm)) {
				
				RWikiObject page = objectService.getRWikiObject(pageName, defaultRealm);
				if(!objectService.checkRead(page)) {
					log.warn("User '" + userId + "' does not have read permissions for page '" + page.getName() + "'. This page will not be returned in the JSON.");
					throw new EntityException("Forbidden: You do not have permission to read this page","",HttpServletResponse.SC_FORBIDDEN);
				}
				SparsePage sparsePage = new SparsePage(pageName,siteId, format);
				String localSpace = NameHelper.localizeSpace(page.getName(),defaultRealm);
				DirectServletPageLinkRenderer plr = new DirectServletPageLinkRenderer(localSpace, defaultRealm, format);
				String rendered = renderService.renderPage(page, localSpace, plr);
				sparsePage.setHtml(rendered);
				addComments(page,sparsePage);
				return sparsePage;
			} else {
				log.warn("Bad request '" + view.getOriginalEntityUrl() + "'");
				throw new EntityException("Bad request: You must supply a valid page name"
        									,"",HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			log.warn("Bad request '" + view.getOriginalEntityUrl() + "'");
			throw new EntityException("Bad request: To get the pages in a site you need a url like " +
									"'/direct/wiki/site/SITEID.json' or '/direct/wiki/site/SITEID/page/PAGENAME.json'"
       								,"",HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	private void getSubPages(SparserPage parent, String realm,String siteId, String userId, String format) {
		
        RWikiObject page = objectService.getRWikiObject(parent.getName(), realm);
        
        // If the current user cannot read this page it will NOT be added to the returned graph
        if(!objectService.checkRead(page)) {
        	log.warn("User '" + userId + "' does not have read permissions for page '"
        				+ parent.getName() + "'. This page will not be returned in the JSON.");
        	return;
        }
        
        String referenced = page.getReferenced();
        
		if(referenced != null) {
			String[] parts = referenced.split("::");
			List<String> children = Arrays.asList(parts);
        
			for(String childName : children) {
				if(!"".equals(childName)) {
					childName = childName.substring(childName.lastIndexOf("/") + 1);
					if(objectService.exists(childName,realm)) {
						SparserPage child = new SparserPage(childName,siteId,format);
						parent.addChildPage(child);
						getSubPages(child,realm,siteId,userId,format);
					}
				}
			}
		}
		
		// Now get the comment count for the parent page
		int commentCount = objectService.findRWikiSubPages(page.getName() + ".").size();
        parent.setNumberOfComments(commentCount);
	}
	
	/**
	 * Be warned: this method only works if the DAO code continues to order by page name ascending.
	 * 
	 * @param page The page to get the comments for.
	 * @param sparsePage The sparse page which will be returned as JSON. The comments get added to this.
	 */
	private void addComments(RWikiObject page, SparsePage sparsePage) {
		
        List<RWikiObject> comments = objectService.findRWikiSubPages(page.getName() + ".");
        	
        String realm = page.getRealm();
        
        Map<String,SparseComment> commentMap = new HashMap<String,SparseComment>();
        
        for(RWikiObject comment : comments) {
        	
        	String userDisplayName = comment.getUser();
        		
        	try {
        		userDisplayName = userDirectoryService.getUser(userDisplayName).getDisplayName();
        	} catch (UserNotDefinedException unde) {
        		log.warn("No user for id '" + userDisplayName + "'. The user id will be returned instead.");
        	}
        
        	// Each comment object name is suffixed by a series of three digit strings, separated
        	// by full stops. These represent the nesting of the comments.
        	
        	String name = comment.getName();
        	
        	String localName = NameHelper.localizeName(name, NameHelper.localizeSpace(name, realm));
        	String dottyBit = localName.substring(localName.indexOf(".", 0));
        
        	String[] commentIds = dottyBit.split("\\.");
        	
        	if(commentIds.length == 2) {
        		
        		// This is a top level comment.
        		SparseComment topLevelComment = new SparseComment(userDisplayName,comment.getVersion().getTime(),comment.getContent());
        		commentMap.put(commentIds[1], topLevelComment);
        		sparsePage.addComment(topLevelComment);
        	} else if(commentIds.length > 2) {
        		
        		// This is a descendant comment. The parent should already be in the
        		// comment map. Get the parent and add the child to it. THIS DEPENDS
        		// ON THE ORDERING FROM DAO BEING ASCENDING BY NAME.
        	
        		SparseComment parent = commentMap.get(commentIds[commentIds.length - 2]);
        		
        		if(parent == null) {
        			// This is terribly bad news. It means the ordering was messed up
        			// as the parent should be in the map at this point.
        			log.warn("No comment in the map for " + commentIds[commentIds.length - 2] + ". Skipping ...");
        			continue;
        		}
        		
        		SparseComment child = new SparseComment(userDisplayName,comment.getVersion().getTime(),comment.getContent());
        		parent.addChildComment(child);
        		commentMap.put(commentIds[commentIds.length - 1], child);
        	}
        }
	}
}

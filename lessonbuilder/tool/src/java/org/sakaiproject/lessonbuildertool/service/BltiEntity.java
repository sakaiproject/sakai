/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

/**
 * Interface to Assignment
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */

// NOTE: almost no other class should import this. We want to be able
// to support both forums and jforum. So typically there will be a 
// forumEntity, but it's injected, and it can be either forum and jforum.
// Hence it has to be declared LessonEntity. That leads to a lot of
// declarations like LessonEntity forumEntity.  In this case forumEntity
// means either a ForumEntity or a JForumEntity. We can't just call the
// variables lessonEntity because the same module will probably have an
// injected class to handle tests and quizes as well. That will eventually
// be converted to be a LessonEntity.
@Slf4j
public class BltiEntity implements LessonEntity, BltiInterface {
    private static Cache bltiCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    // 2.8 doesn't define this, so put it here
    static final String LTI_PAGETITLE =     "pagetitle";

    private SimplePageBean simplePageBean;

    protected static LTIService ltiService = null; 

    public void setSimplePageBean(SimplePageBean simplePageBean) {
	this.simplePageBean = simplePageBean;
    }

    private LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    public LessonEntity getNextEntity() {
	return nextEntity;
    }
    
    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    private static ToolManager toolManager;
    public void setToolManager(ToolManager tm) {
		if (toolManager == null ) toolManager = tm;
	}
    
    private static SiteService siteService;
	public void setSiteService(SiteService sm) {
		if (siteService == null ) siteService = sm;
	}

	static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static String returnUrl = null;
    public void setReturnUrl(String m) {
	returnUrl = m;
    }

    public void init () {
	log.info("init()");
	bltiCache = memoryService
	    .getCache("org.sakaiproject.lessonbuildertool.service.BltiEntity.cache");

        /* Hack to avoid a restart to get a new version of DBLTIService 
	if ( ltiService == null ) { 
		ltiService = (LTIService) new DBLTIService(); 
		((org.sakaiproject.lti.impl.DBLTIService) ltiService).setAutoDdl("true"); 
		((org.sakaiproject.lti.impl.DBLTIService) ltiService).init(); 
	} 
        */

	if ( ltiService == null ) {
	    Object service = ComponentManager.get("org.sakaiproject.lti.api.LTIService");
	    if (service == null) {
		log.info("can't find LTI Service -- disabling LTI support");
		return;
	    }
	    ltiService = (LTIService)service;
	    log.info("LTI initialized");
	}

    }

    public void destroy()
    {
	//	bltiCache.destroy();
	//	bltiCache = null;

	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    public boolean servicePresent() {
	return ltiService != null;
    }

    protected BltiEntity() {
    }

    protected BltiEntity(int type, String id) {
	this.type = type;
	this.id = id;
    }

    public String getToolId() {
	return "sakai.blti";
    }

    // the underlying object, something Sakaiish
    protected String id;
    protected int type;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them

    protected Map<String,Object> content;
    protected Map<String,Object> tool;

/*
    public Blti getBlti(String ref, boolean nocache) {
	Blti ret = (Blti)bltiCache.get(ref);
	if (!nocache && ret != null)
	    return ret;

	try {
	    //  ret = BltiService.getBlti(ref);
	} catch (Exception e) {
	    ret = null;
	}
	
	if (ret != null)
	    bltiCache.put(ref, ret, DEFAULT_EXPIRATION);
	return ret;
    }
*/

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return 0;
    }

    public int getTypeOfGrade() {
	return 1;
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + BLTI + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null, null);
    }

    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {    
	return getEntitiesInSite(bean, null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean, Integer bltiToolId) {    
	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	if (ltiService == null)
	    return ret;
	String search = null;
	if (bltiToolId != null)
	    search = "tool_id=" + bltiToolId;
	List<Map<String,Object>> contents = ltiService.getContents(search,null,0,0, bean.getCurrentSiteId());
	for (Map<String, Object> content : contents ) {
	    Long id = getLong(content.get(LTIService.LTI_ID));
	    if ( id == -1 ) continue;
	    BltiEntity entity = new BltiEntity(TYPE_BLTI, id.toString());
	    entity.content = content;
	    ret.add(entity);
	}
	return ret;
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {    
	return getEntity(ref);
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);

	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	String id = "";
	try {
	    id = idString;
	} catch (Exception ignore) {
	    return null;
	}

	if (typeString.equals(BLTI)) {
	    return new BltiEntity(TYPE_BLTI, id);
	} else if (nextEntity != null) {
	    // in case we chain to a different implementation. Not likely for BLTI
	    return nextEntity.getEntity(ref);
	} else
	    return null;
    }

    protected void loadContent() {
	if ( content != null ) return;
	if ( id == null ) return; // Likely a failure
	if ( ltiService == null) return;  // not basiclti or old
	Long key = getLong(id);
	content = ltiService.getContent(key, toolManager.getCurrentPlacement().getContext());
	if ( content == null ) return;
	Long toolKey = getLongNull(content.get("tool_id"));
	if (toolKey != null ) tool = ltiService.getTool(toolKey, toolManager.getCurrentPlacement().getContext());
    }	

    // properties of entities
    public String getTitle() {
	loadContent();
	if ( content == null ) return null;
	return (String) content.get(LTIService.LTI_TITLE);
    }

    private String getErrorUrl() {
	return "javascript:document.write('" + messageLocator.getMessage("simplepage.format.item_removed").replace("'", "\\'") + "')";
    }

    // TODO: Concern regarding the lack of the returnUrl when this is called
    public String getUrl() {
	loadContent();
	// If I return null here, it appears that I cause an NPE in LB
	if ( content == null ) return getErrorUrl();
	String ret = (String) content.get("launch_url");
	if ( ltiService != null && tool != null && ltiService.isMaintain(getSiteId())
	    	&& LTIService.LTI_SECRET_INCOMPLETE.equals((String) tool.get(LTIService.LTI_SECRET)) 
		&& LTIService.LTI_SECRET_INCOMPLETE.equals((String) tool.get(LTIService.LTI_CONSUMERKEY)) ) {
		String toolId = getCurrentTool("sakai.siteinfo");
		if ( toolId != null ) {
		    ret = editItemUrl(toolId);
		    return ret;
		}
	}

	ret = ServerConfigurationService.getServerUrl() + ret;
	return ret;
    }

    public Date getDueDate() {
	return null;
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	// not used for BLTI, control is done entirely within LB
	return false;
    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return false;
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return false;
    }

    public LessonSubmission getSubmission(String userId) {
	// students don't have submissions to BLTI
	return null;
    }

// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	return 0;
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	return createNewUrls(bean, null);
    }

    public List<UrlItem> createNewUrls(SimplePageBean bean, Integer bltiToolId) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	String toolId = bean.getCurrentTool("sakai.siteinfo");
	if ( ltiService == null || toolId == null || returnUrl == null ) return list;

        // Retrieve all tools
	String search = null;
	if (bltiToolId != null)
	    search = "lti_tools.id=" + bltiToolId;
	List<Map<String,Object>> tools = ltiService.getTools(search,null,0,0, bean.getCurrentSiteId());
	for ( Map<String,Object> tool : tools ) {
		String url = ServerConfigurationService.getToolUrl() + "/" + toolId + "/sakai.basiclti.admin.helper.helper?panel=ContentConfig&tool_id=" 
			+ tool.get(LTIService.LTI_ID) + "&returnUrl=" + URLEncoder.encode(returnUrl);
		String fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
		Long ls = getLong(tool.get(LTIService.LTI_PL_LINKSELECTION));
		Boolean selector = (new Long(1)).equals(ls);

		list.add(new UrlItem(url, (String) tool.get(LTIService.LTI_TITLE), fa_icon, selector));
	}

	String url = ServerConfigurationService.getToolUrl() + "/" + toolId + "/sakai.basiclti.admin.helper.helper?panel=Main" + 
		"&returnUrl=" + URLEncoder.encode(returnUrl);
	list.add(new UrlItem(url, messageLocator.getMessage("simplepage.create_blti")));
	return list;
    }

    public boolean isPopUp() {
	loadContent();
	if (content == null)
	    return false;
	Long newPage = getLong(content.get(LTIService.LTI_NEWPAGE));
        return (newPage == 1) ; 
    }

    public int frameSize() {
        loadContent();
        if ( content == null  ) return -1;
        Long newPage = getLong(content.get(LTIService.LTI_FRAMEHEIGHT));
        return newPage.intValue();
    }
    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	String toolId = bean.getCurrentTool("sakai.siteinfo");
	if ( toolId == null ) return null;
	return editItemUrl(toolId);
    }

    public String editItemUrl(String toolId) {
	if ( toolId == null ) return null;
	loadContent();
	if (content == null)
	    return null;
	String url = ServerConfigurationService.getToolUrl() + "/" + toolId + "/sakai.basiclti.admin.helper.helper?panel=ContentConfig&id=" + 
		content.get(LTIService.LTI_ID);
	if ( returnUrl != null ) {
		url = url + "&returnUrl=" + URLEncoder.encode(returnUrl);
	} else {
		url = url + "&returnUrl=about:blank";
	}
	return url;
    }

    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

    public boolean objectExists() {
	loadContent();
	return content != null;
    }

    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	return !objectExists();
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	// done entirely within LB, this item type is not group-aware
	return null;
    }
  
    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {
	// not group aware
    }

    public String doImportTool(String launchUrl, String bltiTitle, String strXml, String custom)
    {
	if ( ltiService == null ) return null;

	String toolBaseUrl = launchUrl;
	int pos = launchUrl.indexOf('?');
	if ( pos > 1 ) {
		toolBaseUrl = launchUrl.substring(0,pos);
	}

	// Look for a tool that is a perfect match, and fall back
	//
	Map<String,Object> theTool = null;
	Map<String,Object> theBaseTool = null;
	List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0, simplePageBean.getCurrentSiteId());
	for ( Map<String,Object> tool : tools ) {
		String toolLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
		if ( toolLaunch.equals(launchUrl) ) {
			theTool = tool;
			break;				
		}
		if ( theBaseTool == null && toolLaunch.equals(toolBaseUrl) ) {
			theBaseTool = tool;
		}
	}

	if ( theTool == null && theBaseTool != null ) theTool = theBaseTool;

	if ( theTool == null ) {
		Properties props = new Properties ();
		props.setProperty(LTIService.LTI_LAUNCH,toolBaseUrl);
		if ( toolBaseUrl.equals(launchUrl) ) {
			props.setProperty(LTIService.LTI_TITLE, bltiTitle);
		} else {
			props.setProperty(LTIService.LTI_TITLE, toolBaseUrl);
		}
		props.setProperty(LTI_PAGETITLE, bltiTitle);
		props.setProperty(LTIService.LTI_CONSUMERKEY, LTIService.LTI_SECRET_INCOMPLETE);
		props.setProperty(LTIService.LTI_SECRET, LTIService.LTI_SECRET_INCOMPLETE);
		props.setProperty(LTIService.LTI_ALLOWLAUNCH, "1");
		props.setProperty(LTIService.LTI_ALLOWCUSTOM, "1");
		props.setProperty(LTIService.LTI_ALLOWTITLE, "1");
		props.setProperty(LTIService.LTI_ALLOWPAGETITLE, "1");
		props.setProperty(LTIService.LTI_ALLOWOUTCOMES, "1");
		props.setProperty(LTIService.LTI_SENDNAME, "1");
		props.setProperty(LTIService.LTI_SENDEMAILADDR, "1");
		props.setProperty(LTIService.LTI_XMLIMPORT,strXml);
		if (custom != null)
		    props.setProperty(LTIService.LTI_CUSTOM, custom);
		Object result = ltiService.insertTool(props, simplePageBean.getCurrentSiteId());
		if ( result instanceof String ) {
			log.info("Could not insert tool - "+result);
		}
		if ( result instanceof Long ) theTool = ltiService.getTool((Long) result, simplePageBean.getCurrentSiteId());
	}

	Map<String,Object> theContent = null;
	Long contentKey = null;
	if ( theTool != null ) {
		Properties props = new Properties ();
		props.setProperty(LTIService.LTI_TOOL_ID,getLong(theTool.get(LTIService.LTI_ID)).toString());
		props.setProperty(LTIService.LTI_TITLE, bltiTitle);
		props.setProperty(           LTI_PAGETITLE, bltiTitle);
		props.setProperty(LTIService.LTI_LAUNCH,launchUrl);
		props.setProperty(LTIService.LTI_XMLIMPORT,strXml);
		if ( custom != null ) props.setProperty(LTIService.LTI_CUSTOM,custom);
		Object result = ltiService.insertContent(props, getSiteId());
		if ( result instanceof String ) {
			log.info("Could not insert content - "+result);
		}
		if ( result instanceof Long ) theContent = ltiService.getContent((Long) result, getSiteId());
	}

	String sakaiId = null;
	if ( theContent != null ) {
		sakaiId = "/blti/" + theContent.get(LTIService.LTI_ID);
		log.info("Adding LTI content "+sakaiId);
	}
	return sakaiId;
    }


	// TODO: Could we get simplePageBean populated here and not build out own get
        public String getCurrentTool(String commonToolId) {
		try {
			String currentSiteId = toolManager.getCurrentPlacement().getContext();
			Site site = siteService.getSite(currentSiteId);
			ToolConfiguration toolConfig = site.getToolForCommonId(commonToolId);
			if (toolConfig == null) return null;
			return toolConfig.getId();
		} catch (Exception e) {
			return null;
		}
        }

  public Long getLong(Object key) {
    Long retval = getLongNull(key);
    if (retval != null)
      return retval;
    return new Long(-1);
  }

  public Long getLongNull(Object key) {
    if (key == null)
      return null;
    if (key instanceof Number)
      return new Long(((Number) key).longValue());
    if (key instanceof String) {
      try {
        return new Long((String) key);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

    public String getObjectId(){
	return null;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (nextEntity != null)
	    return nextEntity.findObject(objectid, objectMap, siteid);
	return null;
    }
    
    public String getSiteId() {
	loadContent();
	if ( content == null ) return null;
	return (String) content.get(LTIService.LTI_SITE_ID);
    }



}

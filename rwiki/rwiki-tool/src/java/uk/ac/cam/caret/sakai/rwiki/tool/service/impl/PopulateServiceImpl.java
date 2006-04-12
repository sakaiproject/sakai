/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2005 University of Cambridge
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.framework.portal.PortalService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;


import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.tool.api.PopulateService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
* @author andrew
*
*/

//FIXME: Tool

public class PopulateServiceImpl implements PopulateService {
	
	private Logger log;
	private HashMap seenPageSpaces = new HashMap();
	private List seedPages;
	
	private RWikiCurrentObjectDao dao;
   
   private RenderService renderService = null;
   private SiteService siteService = null;
   private PortalService portalService = null;
	
   
	public void init() throws IOException {

		for ( Iterator i = seedPages.iterator(); i.hasNext(); ) {    
				RWikiCurrentObject seed = (RWikiCurrentObject) i.next();
				BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(seed.getSource()), "UTF-8"));
				char[] c = new char[2048];
				StringBuffer sb = new StringBuffer();
				for ( int ic = br.read(c); ic >= 0; ic = br.read(c) ) {
					if ( ic == 0 ) Thread.yield();
					else sb.append(c,0,ic);
				}
				br.close();
				seed.setContent(sb.toString());
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.PopulateService#populateRealm(java.lang.String, java.lang.String)
	 */
   // SAK-2514
	public void populateRealm(String user, String space, String group)
	throws PermissionException {
		synchronized (seenPageSpaces) {
			if (seenPageSpaces.get(space) == null ) {
               String owner = user;
               Site s = null;
               try {
                   s = siteService.getSite(portalService.getCurrentSiteId());
                   owner =  s.getCreatedBy().getId();
               } catch (Exception e) {
                   log.warn("Cant find who created this site, defaulting to current user for prepopulate ownership :"+owner);
               }
               if ( s == null ) {
                   log.error("Cant Locate current site, will populate only global pages with no restrictions");
               }
               if (log.isDebugEnabled()) {
                   log.debug("Populating space: " + space);
               }
				for ( Iterator i = seedPages.iterator(); i.hasNext(); ) {
                   
                   
					RWikiCurrentObject seed = (RWikiCurrentObject) i.next();
                    if ( seed instanceof RWikiCurrentObject ) {
                        RWikiCurrentObject seedimpl = (RWikiCurrentObject)seed;
                        List targetTypes = seedimpl.getTargetSiteTypes();
                        if ( ignoreSeedPage(s,targetTypes) ) {
                            log.debug("Ignoring Seed page "+seed.getName());
                            continue;
                        }
                    }
                   
					
					String name = NameHelper.globaliseName(seed.getName(), space);
					log.	debug("Populating Space with "+seed.getName());
					if (dao.findByGlobalName(name) == null) {
						if (log.isDebugEnabled()) {
							log.debug("Creating Page: " + name);
						}
						log.debug("Creating Page :"+name);
                       
						RWikiCurrentObject rwo = dao.createRWikiObject(name, space);
						seed.copyTo(rwo);
                        // SAK-2513
                   
                        log.debug("Populate with Owner "+owner);
                        rwo.setUser(owner);
                        rwo.setOwner(owner);
                        updateReferences(rwo,space);
						rwo.setName(name);
						rwo.setRealm(group);
						dao.update(rwo,null);
						log.debug("Page Created ");
					} else {
						log.debug("Page Already exists ");
					}
				}
				seenPageSpaces.put(space, space);
			}
		}
	}
   /**
    * returns true if the the page should be ignored
    * @param s the site
    * @param targetTypes a list of lines that specify which site matches
    * @return
    */
   private boolean ignoreSeedPage(Site s, List targetTypes) {
       if ( targetTypes == null || targetTypes.size() == 0 ) return false;
       if ( s == null ) {
           // if all the types are not, then dont ignore
           for ( Iterator i = targetTypes.iterator(); i.hasNext(); ) {
               String ttype = (String)i.next();
               String[] ttypeGroup = ttype.split(",");
               for ( int j = 0; j < ttypeGroup.length; j++ ) {
                   if ( !ttypeGroup[j].startsWith("!") ) return true;
               }
           }
           return false;
       } else {
           String type = s.getType();
           if ( type == null ) type = "";
           type = type.toLowerCase();
           log.debug("Checking Site "+type);
           // each line is anded together and each line is ored with other lines
           for( Iterator i = targetTypes.iterator(); i.hasNext(); ) {
               String ttype = (String) i.next();
               String[] ttypeGroup = ttype.split(",");
               boolean bline = true;
               for ( int j = 0; j < ttypeGroup.length; j++ ) {
                   if ( ttypeGroup[j].startsWith("!") ) {
                       bline = bline & ( !type.startsWith(ttype.substring(1).toLowerCase()) );
                       log.debug("Checking not "+ttypeGroup[j]+" was "+bline);
                   } else {
                       bline = bline & ( type.startsWith(ttype.toLowerCase()) );
                       log.debug("Checking "+ttypeGroup[j]+" was "+bline);
                   }
               }
               if ( bline ) return false;
           }
           return true;
      }
       
   }
	
   // SAK-2470
   private void updateReferences(RWikiCurrentObject rwo,  String space) {

           // render to get a list of links
           final HashSet referenced = new HashSet();
           final String currentRealm = rwo.getRealm();

           PageLinkRenderer plr = new PageLinkRenderer() {
               public void appendLink(StringBuffer buffer, String name,
                       String view) {
                   referenced
                           .add(NameHelper.globaliseName(name, currentRealm));
               }

               public void appendLink(StringBuffer buffer, String name,
                       String view, String anchor) {
                   referenced
                           .add(NameHelper.globaliseName(name, currentRealm));
               }

               public void appendCreateLink(StringBuffer buffer, String name,
                       String view) {
                   referenced
                           .add(NameHelper.globaliseName(name, currentRealm));
               }

               public boolean isCachable() {
                   return false; // should not cache this render op
               }

               public boolean canUseCache() {
                   return false;
               }

               public void setCachable(boolean cachable) {
               }
               public void setUseCache(boolean b) {
                   
               }

           };

           renderService.renderPage(rwo,  space, plr);

           // process the references
           StringBuffer sb = new StringBuffer();
           Iterator i = referenced.iterator();
           while (i.hasNext()) {
               sb.append("::").append(i.next());
           }
           sb.append("::");
           rwo.setReferenced(sb.toString());

       }

	
	public Logger getLog() {
		return log;
	}
	
	public void setLog(Logger log) {
		this.log = log;
	}
	
	public List getSeedPages() {
		return seedPages;
	}
	
	public void setSeedPages(List seedPages) {
		this.seedPages = seedPages;
	}
	
	public RWikiCurrentObjectDao getRWikiCurrentObjectDao() {
		return dao;
	}
	
	public void setRWikiCurrentObjectDao(RWikiCurrentObjectDao dao) {
		this.dao = dao;
	}

   /**
    * @return Returns the renderService.
    */
   public RenderService getRenderService() {
       return renderService;
   }

   /**
    * @param renderService The renderService to set.
    */
   public void setRenderService(RenderService renderService) {
       this.renderService = renderService;
   }

   /**
    * @return Returns the siteService.
    */
   public SiteService getSiteService() {
       return siteService;
   }

   /**
    * @param siteService The siteService to set.
    */
   public void setSiteService(SiteService siteService) {
       this.siteService = siteService;
   }

   /**
    * @return Returns the portalService.
    */
   public PortalService getPortalService() {
       return portalService;
   }

   /**
    * @param portalService The portalService to set.
    */
   public void setPortalService(PortalService portalService) {
       this.portalService = portalService;
   }
	
	
	
	
}

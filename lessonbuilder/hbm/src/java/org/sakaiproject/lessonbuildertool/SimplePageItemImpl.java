/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
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

package org.sakaiproject.lessonbuildertool;

import java.util.Map;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

/**
 * This is a single item on a simple page.
 * 
 * WARNING: this code should execute under oracle and mysql. Oracle treats "" as null, 
 * and apparently stores null as "". To produce predictable results for the rest of the
 * code, we normalize some of the fields to "" if they are null. The problem is if we
 * write "", Oracle will read it as null, but our code expects it to come back as "".
 * Note that this code is called both to construct new items in our code and by hibernate
 * to build an object when reading from the database.
 * 
 * NOTE: Please don't add any more fields to this class.  Instead, take a look at
 * the SimplePageItemAttribute system we have set up.
 *
 * @author jeney
 * 
 */
public class SimplePageItemImpl implements SimplePageItem  {

	public static final int RESOURCE = 1;
	public static final int PAGE = 2;
	public static final int ASSIGNMENT = 3;
	public static final int ASSESSMENT = 4;
	public static final int TEXT = 5;
	public static final int URL = 6;
	public static final int MULTIMEDIA = 7;
	public static final int FORUM = 8;
	public static final int COMMENTS = 9;
	public static final int STUDENT_CONTENT = 10;
	public static final int QUESTION = 11;
    public static final int BLTI = 12;
    public static final int PEEREVAL = 13;
    public static final int BREAK = 14;
    public static final int CHECKLIST = 15;

    // must agree with definition in hbm file
	public static final int MAXNAME = 255;

    // sakaiId used for an item copied from another site with no real content
	public static final String DUMMY = "/dummy";

	private long id;
	private long pageId; // ID of the page this item belongs to
	private int sequence; // Position in which this item appears on the page
	private int type; // Type of item this is
	private String sakaiId; // The ID given by Sakai to this item
	private String name; // Name to be displayed
	private String html = null; // If the item is a text block, we store the text here
	private String description;
	private String height; // Height and width are to be used for iFrames (multimedia)
	private String width;
	private String alt;
	private boolean nextPage; // show as next rather than subpage
	private String format; // display format, currently nothing, button or maybe li
				// for breaks, "section" or "column"
	private boolean required;
	private boolean alternate;  // student can do this or the one above
	private boolean subrequirement;
	private String requirementText;
	private boolean prerequisite; // Whether or not the item is unavailable until all previous
									// required items are fulfilled.
	private boolean sameWindow; // Display resource inline rather than new window
	
	private boolean anonymous = false; // Should comments be anonymous
	private boolean showComments = false; // Should Student Pages have comment sections?
	private boolean forcedCommentsAnonymous = false;
	
	private String groups;

	private String gradebookId; // If a gradebook entry exists, this is the external ID
	private Integer gradebookPoints; // Max number of points, if gradebook entry exists
	private String gradebookTitle;
	
	// Occasionally there may be two gradebook entries, these are used for that.
	// For example, grading comments on student pages.
	private String altGradebook;
	private Integer altPoints;
	private String altGradebookTitle;
	private boolean groupOwned = false;
	private String ownerGroups = null;
	
	private boolean showPeerEval = false;
	
	/* All future fields should be added as attributes instead of
	 * additional columns. This is actually a JSONObject, but we can't
	 * refer to that here. In practice values are normally strings but
	 * can be JSON objects.
	 */
	private Map<String, Object> attributes;
	
        private static SimplePageToolDao simplePageToolDao = null;

	public SimplePageItemImpl() {
		if (simplePageToolDao != null)
		    attributes = simplePageToolDao.newJSONObject();
	}

	public SimplePageItemImpl(long id, long pageId, int sequence, int type, String sakaiId, String name) {
		if (sakaiId == null)
			sakaiId = "";
		if (name == null)
			name = "";
		this.id = id;
		this.pageId = pageId;
		this.sequence = sequence;
		this.type = type;
		this.sakaiId = sakaiId;
		this.name = maxlength(name, MAXNAME);
		height = "";
		width = "";
		description = "";
		alt = "";
		subrequirement = false;
		alternate = false;
		prerequisite = false;
		required = false;
		requirementText = "";
		sameWindow = false;  // old entries have to default to off
		groupOwned = false;
		ownerGroups = "";
		if (simplePageToolDao != null)
		    attributes = simplePageToolDao.newJSONObject();
	}

	public SimplePageItemImpl(long pageId, int sequence, int type, String sakaiId, String name) {
		if (sakaiId == null)
			sakaiId = "";
		if (name == null)
			name = "";
		this.pageId = pageId;
		this.sequence = sequence;
		this.type = type;
		this.sakaiId = sakaiId;
		this.name = maxlength(name, MAXNAME);
		height = "";
		width = "";
		description = "";
		alt = "";
		subrequirement = false;
		alternate = false;
		prerequisite = false;
		required = false;
		requirementText = "";
		sameWindow = false;  // old entries have to default to off
		groupOwned = false;
		ownerGroups = "";
		if (simplePageToolDao != null)
		    attributes = simplePageToolDao.newJSONObject();
	}

    // this should seldom truncate anything. The UI makes sure the user can't enter namss that
    // are too long. The main case this will happen is if we generate a name, which we do for URLs
    // and uploaded files.
	private String maxlength(String s, int maxlen) {
	    if (s == null)
		s = "";  // oracle turns "" into null
	    int len = s.length();
	    if (s == null || len <= maxlen)
		return s;
	    int toremove = len - maxlen;
	    int i = s.lastIndexOf(".");

	    // 6 is sort of arbitrary. need a few characters before the .
	    // to do useful abbreviation. The issue here is that in theory
	    // so much of the name might be after the dot that we have to
	    // the abbreviation there.
	
	    if (i > 0 && toremove < (i - 6)) {
		String prefix = s.substring(0, i);
		String suffix = s.substring(i+1);
		int startcut = (i / 2) - (toremove / 2);
		int endcut = startcut + toremove;
		prefix = prefix.substring(0, startcut) + prefix.substring(endcut);
		return prefix + "." + suffix;
	    }

	    // not enough space to cut from prefix, or no prefix. 
	    // for now, just cut the whole string.
	    int startcut = (len / 2) - (toremove / 2);
	    int endcut = startcut + toremove;
	    return s.substring(0, startcut) + s.substring(endcut);
	}


	public long getId() {
		return id;
	}

	public long getPageId() {
		return pageId;
	}

	public int getSequence() {
		return sequence;
	}

	public int getType() {
		return type;
	}

	public String getSakaiId() {
		return sakaiId;
	}

	public String getName() {
		return name;
	}

	public String getHtml() {
		return html;
	}

	public String getDescription() {
		return description;
	}

	public void setId(long i) {
		id = i;
	}

	public void setPageId(long s) {
		pageId = s;
	}

	public void setSequence(int o) {
		if (o > 0) {
			sequence = o;
		}
	}

	/**
	 * Set the type of item
	 * 
	 * 1 = Resource 2 = Page 3 = Assignment 4 = Assessment / Quiz 5 = Text Block (HTML) 6 = URL /
	 * link 7 = Multimedia
	 */
	public void setType(int t) {
		type = t;
	}

	public void setSakaiId(String s) {
	        if (s == null)
		    s = "";
		sakaiId = s;
	}

	public void setName(String s) {
	        if (s == null)
		    s = "";
		name = maxlength(s, MAXNAME);
	}

	public void setHtml(String html) {
		if( html == null ) {
			html = "";
		}
		this.html = html;
	}

	public void setDescription(String desc) {
		if (desc == null)
		    desc = "";
		description = desc;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		return height;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getWidth() {
		return width;
	}

	public void setAlt(String alt) {
		if (alt == null)
		    alt = "";
		this.alt = alt;
	}

	public String getAlt() {
		return alt;
	}

	public void setNextPage(Boolean n) {
		if (n == null)
		    n = false;
		nextPage = n;
	}

	public boolean getNextPage() {
		return nextPage;
	}

	public void setFormat(String f) {
		format = f;
	}

	public String getFormat() {
		return format;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}

	public boolean isAlternate() {
		return alternate;
	}


	public void setPrerequisite(boolean prerequisite) {
		this.prerequisite = prerequisite;
	}

	/**
	 * 
	 * @return Whether or not this is unavailable until all previous requirements are fulfilled.
	 */
	public boolean isPrerequisite() {
		return prerequisite;
	}

	public void setSubrequirement(boolean requirement) {
		subrequirement = requirement;
	}

	public boolean getSubrequirement() {
		return subrequirement;
	}

	public void setRequirementText(String requirementText) {
		this.requirementText = requirementText;
	}

	public String getRequirementText() {
		return requirementText;
	}

	public String getURL() {
		// Will Update to take type into account when adding more than just resources
		if (type == RESOURCE || type == MULTIMEDIA) {
			if (getAttribute("multimediaUrl") != null)
			    return getAttribute("multimediaUrl");
			return "/access/content" + getSakaiId();
		} else if (type == URL) {
		    // not used, apparently
			return getSakaiId();
		} else {
			return "";
		}
	}

    // for resource and inline, go through our own /access/lessonbuilder.
    // two advantages: we can do some level of availability checking, although user can still fake the URL
    // we display HTML, which by default Sakai doesn't do
	public String getItemURL(String siteId, String owner) {
		// Will Update to take type into account when adding more than just resources
	        // Note that some resources are not stored in resources, e.g.
	        // Citations, but there's no way to tell that from the id.
	        // To minimize DB activity, the sakaiid is based solely on
	        // the resource ID, so even at this point we don't know
	        // what it really is. The Access handler does the final translation
	        // no one else should do anything with sakai id's other than
	        // hand them to Content
		if (type == RESOURCE || type == MULTIMEDIA) {
		    if (getAttribute("multimediaUrl") != null)
			return getAttribute("multimediaUrl");
		    return "/access/lessonbuilder/item/" + getId() + getSakaiId();
		} else if (type == URL) {
		    // not used, I'm pretty sure
			return getSakaiId();
		} else {
			return "";
		}
	}

	public void setSameWindow(Boolean b) {
		if (b == null)
		    this.sameWindow = false;
		else
		    this.sameWindow = b;
	}

	public Boolean isSameWindow() {
		return sameWindow;
	}

	public void setAnonymous(Boolean anon) {
		if(anon == null) {
			anon = false;
		}
		
		anonymous = anon;
	}
	
	public boolean isAnonymous() {
		return anonymous;
	}

	public String getGroups() {
	    return groups;
	}

	public void setGroups(String g) {
	    groups = g;
	}
	
	public Boolean getShowComments() {
		return showComments;
	}
	
	public void setShowComments(Boolean showComments) {
		if(showComments == null) {
			showComments = false;
		}
		
		this.showComments = showComments;
	}
	
	public boolean getForcedCommentsAnonymous() {
		return forcedCommentsAnonymous;
	}
	
	public void setForcedCommentsAnonymous(Boolean forcedCommentsAnonymous) {
		if(forcedCommentsAnonymous == null) {
			forcedCommentsAnonymous = false;
		}
		
		this.forcedCommentsAnonymous = forcedCommentsAnonymous;
	}

	public void setGradebookId(String gradebookId) {
		this.gradebookId = gradebookId;
	}
	
	public String getGradebookId() {
		return gradebookId;
	}
	
	public void setGradebookPoints(Integer gradebookPoints) {
		this.gradebookPoints = gradebookPoints;
	}
	
	public Integer getGradebookPoints() {
		return gradebookPoints;
	}
	
	public void setGradebookTitle(String gradebookTitle) {
		this.gradebookTitle = gradebookTitle;
	}
    
    public String getGradebookTitle() {
    	return gradebookTitle;
    }
	
	public void setAltGradebook(String gradebookId) {
		this.altGradebook = gradebookId;
	}
	
	public String getAltGradebook() {
		return altGradebook;
	}
	
	public void setAltPoints(Integer gradebookPoints) {
		this.altPoints = gradebookPoints;
	}
	
	public Integer getAltPoints() {
		return altPoints;
	}
	
	public void setAltGradebookTitle(String gradebookTitle) {
		this.altGradebookTitle = gradebookTitle;
	}
    
	public String getAltGradebookTitle() {
	    return altGradebookTitle;
	}

	public boolean isGroupOwned() {
	    return groupOwned;
	}

	public void setGroupOwned(Boolean o) {
	    if(o == null)
		o = false;
	    groupOwned = o;
	}

	public String getOwnerGroups() {
	    return ownerGroups;
	}

	public void setOwnerGroups(String s) {
	    ownerGroups = s;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, Object> map) {
		attributes = map;
	}

	/* This is the getter I want everyone else to use for attributes. */
	public String getAttribute(String attr) {
	    return (String)attributes.get(attr);
	}
	
	public void setAttribute(String attr, String value) {
	    attributes.put(attr, value);
	}

	public void removeAttribute(String attr) {
	    attributes.remove(attr);
	}

	public Object getJsonAttribute(String attr) {
		return attributes.get(attr);
	}
	
        public void setJsonAttribute(String attr, Object value) {
		attributes.put(attr, value);
	}

    // JSON operations are put through the Dao because this code is in shared, and we have no way
    // to add the JSON jar file to shared. This has to be in shared so other people can use it.
    // setting simplePageToolDao has a possible race condition, but it's very unlikely, and even if
    // it happens, both threads will be setting the same value.

        public String getAttributeString() {
	    if (attributes != null)
		return attributes.toString();
	    else
		return null;
	}

        public void setAttributeString(String s) {
	    if (simplePageToolDao == null)
		return;
	    if (s == null || s.equals(""))
		attributes = simplePageToolDao.newJSONObject();
	    else
		attributes = simplePageToolDao.JSONParse(s);
	}

	public static void setSimplePageToolDao(SimplePageToolDao dao) {
	    simplePageToolDao = dao;
	}
	
	public void setShowPeerEval(Boolean review){
		this.showPeerEval= review !=null? review:false;
	}
	public Boolean getShowPeerEval(){
		return new Boolean(showPeerEval);
	}
	

}


/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Internal class that encapsulates all information about a resource that is needed in the browse mode.
 * This is being phased out as we switch to the resources type registry.
 */
public class ResourcesBrowseItem
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("content");

	protected static Integer seqnum = Integer.valueOf(0);
	private String m_itemnum;

	// attributes of all resources
	protected String m_name;
	protected String m_id;
	protected String m_type;

	protected SortedSet m_allSiteGroups;
	protected SortedSet m_inheritedGroupRefs;
	protected SortedSet m_entityGroupRefs;
	protected SortedSet m_allowedRemoveGroupRefs;
	protected SortedSet m_allowedAddGroupRefs;
	protected Map m_allSiteGroupsMap;

	protected boolean m_canRead;
	protected boolean m_canRevise;
	protected boolean m_canDelete;
	protected boolean m_canCopy;
	protected boolean m_isCopied;
	protected boolean m_canAddItem;
	protected boolean m_canAddFolder;
	protected boolean m_canSelect;

	protected boolean m_available;

	protected boolean m_inDropbox;

	protected List m_members;
	protected boolean m_isEmpty;
	protected boolean m_isHighlighted;
	protected boolean m_inheritsHighlight;
	protected String m_createdBy;
	protected String m_createdTime;
	protected String m_modifiedBy;
	protected String m_modifiedTime;
	protected String m_size;
	protected String m_target;
	protected String m_container;
	protected String m_root;
	protected int m_depth;
	protected boolean m_hasDeletableChildren;
	protected boolean m_hasCopyableChildren;
	protected boolean m_copyrightAlert;
	protected String m_url;
	protected boolean m_isLocal;
	protected boolean m_isAttached;
	private boolean m_isMoved;
	private boolean m_canUpdate;
	private boolean m_toobig;
	protected String m_access;
	protected String m_inheritedAccess;
	protected Collection m_groups;

	protected Collection m_oldInheritedGroups;
	protected Collection m_oldPossibleGroups;
	protected BasicRightsAssignment m_rights;

	protected boolean m_pubview;
	protected boolean m_pubview_inherited;
	protected boolean m_pubview_possible;
	protected boolean m_sortable;
	protected boolean m_locked = false;

	/**
	 * @param id
	 * @param name
	 * @param type
	 */
	 public ResourcesBrowseItem(String id, String name, String type)
	 {
		 m_name = name;
		 m_id = id;
		 m_type = type;

		 Integer snum; 
		 synchronized(seqnum)
		 {
			 snum = seqnum;
			 seqnum = Integer.valueOf((seqnum.intValue() + 1) % 10000);
		 }
		 m_itemnum = "Item00000000".substring(0,10 - snum.toString().length()) + snum.toString();

		 m_allowedRemoveGroupRefs = new TreeSet();
		 m_allowedAddGroupRefs = new TreeSet();
		 m_allSiteGroups = new TreeSet(new Comparator()
		 {
			 protected final String DELIM = "::";
			 public int compare(Object arg0, Object arg1) 
			 {
				 Group group0 = (Group) arg0;
				 Group group1 = (Group) arg1;
				 String string0 = group0.getTitle() + DELIM + group0.getId();
				 String string1 = group1.getTitle() + DELIM + group1.getId();

				 return string0.compareTo(string1);
			 }
		 });
		 m_entityGroupRefs = new TreeSet();
		 m_inheritedGroupRefs = new TreeSet();
		 m_allSiteGroupsMap = new Hashtable();

		 // set defaults
		 m_rights = new BasicRightsAssignment(m_itemnum, false);
		 m_members = new LinkedList();
		 m_canRead = false;
		 m_canRevise = false;
		 m_canDelete = false;
		 m_canCopy = false;
		 m_available = true;
		 m_isEmpty = true;
		 m_toobig = false;
		 m_isCopied = false;
		 m_isMoved = false;
		 m_isAttached = false;
		 m_canSelect = true; // default is true.
		 m_hasDeletableChildren = false;
		 m_hasCopyableChildren = false;
		 m_createdBy = "";
		 m_modifiedBy = "";
		 // m_createdTime = TimeService.newTime().toStringLocalDate();
		 // m_modifiedTime = TimeService.newTime().toStringLocalDate();
		 m_size = "";
		 m_depth = 0;
		 m_copyrightAlert = false;
		 m_url = "";
		 m_target = "";
		 m_root = "";

		 m_pubview = false;
		 m_pubview_inherited = false;
		 m_pubview_possible = true;

		 m_isHighlighted = false;
		 m_inheritsHighlight = false;

		 m_canAddItem = false;
		 m_canAddFolder = false;
		 m_canUpdate = false;

		 m_access = AccessMode.INHERITED.toString();
		 m_groups = new Vector();

	 }

	 public void setLocked(boolean isLocked) 
	 {
		 m_locked  = isLocked;

	 }

	 public boolean isLocked()
	 {
		 return m_locked;
	 }

	 public String getItemNum()
	 {
		 return m_itemnum;
	 }

	 public boolean isAvailable()
	 {
		 return m_available;
	 }

	 public void setAvailable(boolean available)
	 {
		 m_available = available;
	 }

	 public boolean isInherited(Group group)
	 {
		 return this.m_inheritedGroupRefs.contains(group.getReference());
	 }

	 public boolean isLocal(Group group)
	 {
		 return this.m_entityGroupRefs.contains(group.getReference());
	 }

	 public boolean isPossible(Group group)
	 {
		 boolean rv = false;

		 if(AccessMode.GROUPED.toString().equals(this.m_inheritedAccess))
		 {
			 rv = this.m_inheritedGroupRefs.contains(group.getReference());
		 }
		 else
		 {
			 rv = this.m_allSiteGroupsMap.containsKey(group.getReference());
		 }

		 return rv;
	 }

	 public boolean allowedRemove(Group group)
	 {
		 return this.m_allowedRemoveGroupRefs.contains(group.getReference());
	 }

	 public SortedSet getAllowedRemoveGroupRefs() 
	 {
		 return m_allowedRemoveGroupRefs;
	 }

	 public void setAllowedRemoveGroupRefs(Collection allowedRemoveGroupRefs) 
	 {
		 importGroupRefs(allowedRemoveGroupRefs, this.m_allowedRemoveGroupRefs);
	 }

	 public void addAllowedRemoveGroupRef(String allowedRemoveGroupRef) 
	 {
		 addGroupRefToCollection(allowedRemoveGroupRef, m_allowedRemoveGroupRefs);
	 }

	 public boolean allowedAdd(Group group)
	 {
		 return this.m_allowedAddGroupRefs.contains(group.getReference());
	 }

	 public SortedSet getAllowedAddGroupRefs() 
	 {
		 return m_allowedAddGroupRefs;
	 }

	 public void setAllowedAddGroupRefs(Collection allowedAddGroupRefs) 
	 {
		 importGroupRefs(allowedAddGroupRefs, this.m_allowedAddGroupRefs);
	 }

	 public void addAllowedAddGroupRef(String allowedAddGroupRef) 
	 {
		 addGroupRefToCollection(allowedAddGroupRef, m_allowedAddGroupRefs);
	 }

	 public List getAllSiteGroups() 
	 {
		 return new Vector(m_allSiteGroups);
	 }

	 public void setAllSiteGroups(Collection allSiteGroups) 
	 {
		 this.m_allSiteGroups.clear();
		 this.m_allSiteGroupsMap.clear();
		 addAllSiteGroups(allSiteGroups);
	 }

	 public void addAllSiteGroups(Collection allSiteGroups) 
	 {
		 Iterator it = allSiteGroups.iterator();
		 while(it.hasNext())
		 {
			 Group group = (Group) it.next();
			 if(! m_allSiteGroupsMap.containsKey(group.getReference()))
			 {
				 this.m_allSiteGroups.add(group);
				 m_allSiteGroupsMap.put(group.getReference(), group);
				 m_allSiteGroupsMap.put(group.getId(), group);
			 }
		 }
	 }

	 public SortedSet getEntityGroupRefs() 
	 {
		 return m_entityGroupRefs;
	 }

	 public void setEntityGroupRefs(Collection entityGroupRefs) 
	 {
		 importGroupRefs(entityGroupRefs, this.m_entityGroupRefs);
	 }

	 public void addEntityGroupRef(String entityGroupRef) 
	 {
		 addGroupRefToCollection(entityGroupRef, m_entityGroupRefs);
	 }

	 public SortedSet getInheritedGroupRefs() 
	 {
		 return m_inheritedGroupRefs;
	 }

	 public void setInheritedGroupRefs(Collection inheritedGroupRefs) 
	 {
		 importGroupRefs(inheritedGroupRefs, this.m_inheritedGroupRefs);
	 }

	 public void addInheritedGroupRef(String inheritedGroupRef) 
	 {
		 addGroupRefToCollection(inheritedGroupRef, m_inheritedGroupRefs);
	 }

	 protected void importGroupRefs(Collection groupRefs, Collection collection) 
	 {
		 collection.clear();
		 Iterator it = groupRefs.iterator();
		 while(it.hasNext())
		 {
			 Object obj = it.next();
			 if(obj instanceof Group)
			 {
				 addGroupRefToCollection(((Group) obj).getReference(), collection);
			 }
			 else if(obj instanceof String)
			 {
				 addGroupRefToCollection((String) obj, collection);
			 }
		 }
	 }

	 protected void addGroupRefToCollection(String groupRef, Collection collection) 
	 {
		 Group group = (Group) m_allSiteGroupsMap.get(groupRef);
		 if(group != null)
		 {
			 if(! collection.contains(group.getReference()))
			 {
				 collection.add(group.getReference());
			 }
		 }
	 }

	 public void setIsTooBig(boolean toobig)
	 {
		 m_toobig = toobig;
	 }

	 public boolean isTooBig()
	 {
		 return m_toobig;
	 }

	 /**
	  * @param name
	  */
	 public void setName(String name)
	 {
		 m_name = name;
	 }

	 /**
	  * @param root
	  */
	 public void setRoot(String root)
	 {
		 m_root = root;
	 }

	 /**
	  * @return
	  */
	 public String getRoot()
	 {
		 return m_root;
	 }

	 /**
	  * @return
	  */
	 public List getMembers()
	 {
		 List rv = new LinkedList();
		 if(m_members != null)
		 {
			 rv.addAll(m_members);
		 }
		 return rv;
	 }

	 /**
	  * @param members
	  */
	 public void addMembers(Collection members)
	 {
		 if(m_members == null)
		 {
			 m_members = new LinkedList();
		 }
		 m_members.addAll(members);
	 }

	 /**
	  * @return
	  */
	 public boolean canAddItem()
	 {
		 return m_canAddItem;
	 }

	 /**
	  * @return
	  */
	 public boolean canDelete()
	 {
		 return m_canDelete;
	 }

	 /**
	  * @return
	  */
	 public boolean canRead()
	 {
		 return m_canRead;
	 }

	 public boolean canSelect() {
		 return m_canSelect;
	 }

	 /**
	  * @return
	  */
	 public boolean canRevise()
	 {
		 return m_canRevise;
	 }

	 /**
	  * @return
	  */
	 public String getId()
	 {
		 return m_id;
	 }

	 /**
	  * @return
	  */
	 public String getName()
	 {
		 return m_name;
	 }

	 /**
	  * @return
	  */
	 public int getDepth()
	 {
		 return m_depth;
	 }

	 /**
	  * @param depth
	  */
	 public void setDepth(int depth)
	 {
		 m_depth = depth;
	 }

	 /**
	  * @param canCreate
	  */
	 public void setCanAddItem(boolean canAddItem)
	 {
		 m_canAddItem = canAddItem;
	 }

	 /**
	  * @param canDelete
	  */
	 public void setCanDelete(boolean canDelete)
	 {
		 m_canDelete = canDelete;
	 }

	 /**
	  * @param canRead
	  */
	 public void setCanRead(boolean canRead)
	 {
		 m_canRead = canRead;
	 }

	 public void setCanSelect(boolean canSelect) {
		 m_canSelect = canSelect;
	 }

	 /**
	  * @param canRevise
	  */
	 public void setCanRevise(boolean canRevise)
	 {
		 m_canRevise = canRevise;
	 }

	 /**
	  * @return
	  */
	 public boolean isFolder()
	 {
		 return ResourceType.TYPE_FOLDER.equals(m_type);
	 }

	 /**
	  * @return
	  */
	 public String getType()
	 {
		 return m_type;
	 }

	 /**
	  * @return
	  */
	 public boolean canAddFolder()
	 {
		 return m_canAddFolder;
	 }

	 /**
	  * @param b
	  */
	 public void setCanAddFolder(boolean canAddFolder)
	 {
		 m_canAddFolder = canAddFolder;
	 }

	 /**
	  * @return
	  */
	 public boolean canCopy()
	 {
		 return m_canCopy;
	 }

	 /**
	  * @param canCopy
	  */
	 public void setCanCopy(boolean canCopy)
	 {
		 m_canCopy = canCopy;
	 }

	 /**
	  * @return
	  */
	 public boolean hasCopyrightAlert()
	 {
		 return m_copyrightAlert;
	 }

	 /**
	  * @param copyrightAlert
	  */
	 public void setCopyrightAlert(boolean copyrightAlert)
	 {
		 m_copyrightAlert = copyrightAlert;
	 }

	 /**
	  * @return
	  */
	 public String getUrl()
	 {
		 return m_url;
	 }

	 /**
	  * @param url
	  */
	 public void setUrl(String url)
	 {
		 m_url = url;
	 }

	 /**
	  * @return
	  */
	 public boolean isCopied()
	 {
		 return m_isCopied;
	 }

	 /**
	  * @param isCopied
	  */
	 public void setCopied(boolean isCopied)
	 {
		 m_isCopied = isCopied;
	 }

	 /**
	  * @return
	  */
	 public boolean isMoved()
	 {
		 return m_isMoved;
	 }

	 /**
	  * @param isCopied
	  */
	 public void setMoved(boolean isMoved)
	 {
		 m_isMoved = isMoved;
	 }

	 /**
	  * @return
	  */
	 public String getCreatedBy()
	 {
		 return m_createdBy;
	 }

	 /**
	  * @return
	  */
	 public String getCreatedTime()
	 {
		 return m_createdTime;
	 }

	 /**
	  * @return
	  */
	 public String getModifiedBy()
	 {
		 return m_modifiedBy;
	 }

	 /**
	  * @return
	  */
	 public String getModifiedTime()
	 {
		 return m_modifiedTime;
	 }

	 /**
	  * @return
	  */
	 public String getSize()
	 {
		 if(m_size == null)
		 {
			 m_size = "";
		 }
		 return m_size;
	 }

	 /**
	  * @param creator
	  */
	 public void setCreatedBy(String creator)
	 {
		 m_createdBy = creator;
	 }

	 /**
	  * @param time
	  */
	 public void setCreatedTime(String time)
	 {
		 m_createdTime = time;
	 }

	 /**
	  * @param modifier
	  */
	 public void setModifiedBy(String modifier)
	 {
		 m_modifiedBy = modifier;
	 }

	 /**
	  * @param time
	  */
	 public void setModifiedTime(String time)
	 {
		 m_modifiedTime = time;
	 }

	 /**
	  * @param size
	  */
	 public void setSize(String size)
	 {
		 m_size = size;
	 }

	 /**
	  * @return
	  */
	 public String getTarget()
	 {
		 return m_target;
	 }

	 /**
	  * @param target
	  */
	 public void setTarget(String target)
	 {
		 m_target = target;
	 }

	 /**
	  * @return
	  */
	 public boolean isEmpty()
	 {
		 return m_isEmpty;
	 }

	 /**
	  * @param isEmpty
	  */
	 public void setIsEmpty(boolean isEmpty)
	 {
		 m_isEmpty = isEmpty;
	 }

	 /**
	  * @return
	  */
	 public String getContainer()
	 {
		 return m_container;
	 }

	 /**
	  * @param container
	  */
	 public void setContainer(String container)
	 {
		 m_container = container;
	 }

	 public void setIsLocal(boolean isLocal)
	 {
		 m_isLocal = isLocal;
	 }

	 public boolean isLocal()
	 {
		 return m_isLocal;
	 }

	 /**
	  * @return Returns the isAttached.
	  */
	 public boolean isAttached()
	 {
		 return m_isAttached;
	 }
	 /**
	  * @param isAttached The isAttached to set.
	  */
	 public void setAttached(boolean isAttached)
	 {
		 this.m_isAttached = isAttached;
	 }

	 /**
	  * @return Returns the hasCopyableChildren.
	  */
	 public boolean hasCopyableChildren()
	 {
		 return m_hasCopyableChildren;
	 }

	 /**
	  * @param hasCopyableChildren The hasCopyableChildren to set.
	  */
	 public void setCopyableChildren(boolean hasCopyableChildren)
	 {
		 this.m_hasCopyableChildren = hasCopyableChildren;
	 }

	 /**
	  * @return Returns the hasDeletableChildren.
	  */
	 public boolean hasDeletableChildren()
	 {
		 return m_hasDeletableChildren;
	 }

	 /**
	  * @param hasDeletableChildren The hasDeletableChildren to set.
	  */
	 public void seDeletableChildren(boolean hasDeletableChildren)
	 {
		 this.m_hasDeletableChildren = hasDeletableChildren;
	 }

	 /**
	  * @return Returns the canUpdate.
	  */
	 public boolean canUpdate()
	 {
		 return m_canUpdate;
	 }

	 /**
	  * @param canUpdate The canUpdate to set.
	  */
	 public void setCanUpdate(boolean canUpdate)
	 {
		 m_canUpdate = canUpdate;
	 }

	 public void setHighlighted(boolean isHighlighted)
	 {
		 m_isHighlighted = isHighlighted;
	 }

	 public boolean isHighlighted()
	 {
		 return m_isHighlighted;
	 }

	 public void setInheritsHighlight(boolean inheritsHighlight)
	 {
		 m_inheritsHighlight = inheritsHighlight;
	 }

	 public boolean inheritsHighlighted()
	 {
		 return m_inheritsHighlight;
	 }

	 /**
	  * Access the access mode for this item.
	  * @return The access mode.
	  */
	 public String getAccess()
	 {
		 return m_access;
	 }

	 /**
	  * Access the access mode for this item.
	  * @return The access mode.
	  */
	 public String getInheritedAccess()
	 {
		 return m_inheritedAccess;
	 }

	 public String getEntityAccess()
	 {
		 String rv = AccessMode.INHERITED.toString();
		 boolean sameGroups = true;
		 if(AccessMode.GROUPED.toString().equals(m_access))
		 {
			 Iterator it = getGroups().iterator();
			 while(sameGroups && it.hasNext())
			 {
				 Group g = (Group) it.next();
				 sameGroups = inheritsGroup(g.getReference());
			 }
			 it = getInheritedGroups().iterator();
			 while(sameGroups && it.hasNext())
			 {
				 Group g = (Group) it.next();
				 sameGroups = hasGroup(g.getReference());
			 }
			 if(!sameGroups)
			 {
				 rv = AccessMode.GROUPED.toString();
			 }
		 }
		 return rv;
	 }

	 public String getEffectiveAccess()
	 {
		 String rv = this.m_access;
		 if(AccessMode.INHERITED.toString().equals(rv))
		 {
			 rv = this.m_inheritedAccess;
		 }
		 if(AccessMode.INHERITED.toString().equals(rv))
		 {
			 rv = AccessMode.SITE.toString();
		 }
		 return rv;
	 }

	 public String getEffectiveGroups()
	 {
		 String rv = rb.getString("access.site1");

		 if(this.isPubviewInherited())
		 {
			 rv = rb.getString("access.public1");
		 }
		 else if(this.isPubview())
		 {
			 rv = rb.getString("access.public1");
		 }
		 else if(this.isInDropbox())
		 {
			 rv = rb.getString("access.dropbox1");
		 }
		 else if(AccessMode.GROUPED.toString().equals(getEffectiveAccess()))
		 {
			 rv = (String) rb.getFormattedMessage("access.group1",  new Object[]{getGroupNames()});
		 }
		 return rv;
	 }

	 public Collection getPossibleGroups()
	 {
		 return m_oldPossibleGroups;
	 }

	 public void setPossibleGroups(Collection groups)
	 {
		 m_oldPossibleGroups = groups;
	 }

	 public String getGroupNames()
	 {
		 String rv = "";

		 Collection groupRefs = this.m_entityGroupRefs;
		 if(groupRefs == null || groupRefs.isEmpty())
		 {
			 groupRefs = this.m_inheritedGroupRefs;
		 }
		 Iterator it = groupRefs.iterator();
		 while(it.hasNext())
		 {
			 String groupRef = (String) it.next();
			 Group group = (Group) this.m_allSiteGroupsMap.get(groupRef);
			 if(group != null)
			 {
				 if(rv.length() == 0)
				 {
					 rv += group.getTitle();
				 }
				 else
				 {
					 rv += ", " + group.getTitle();
				 }
			 }
		 }

		 // TODO: After updating getBrowserItems, get rid of this part
		 if(rv.length() == 0)
		 {
			 Collection groups = getGroups();
			 if(groups == null || groups.isEmpty())
			 {
				 groups = getInheritedGroups();
			 }

			 Iterator grit = groups.iterator();
			 while(grit.hasNext())
			 {
				 Group g = (Group) grit.next();
				 rv += g.getTitle();
				 if(grit.hasNext())
				 {
					 rv += ", ";
				 }
			 }
		 }

		 return rv;
	 }

	 /**
	  * Set the access mode for this item.
	  * @param access
	  */
	 public void setAccess(String access)
	 {
		 m_access = access;
	 }

	 /**
	  * Set the access mode for this item.
	  * @param access
	  */
	 public void setInheritedAccess(String access)
	 {
		 m_inheritedAccess = access;
	 }

	 /**
	  * Access a list of Group objects that can access this item.
	  * @return Returns the groups.
	  */
	 public List getGroups()
	 {
		 if(m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 return new Vector(m_groups);
	 }

	 /**
	  * Access a list of Group objects that can access this item.
	  * @return Returns the groups.
	  */
	 public List getInheritedGroups()
	 {
		 if(m_oldInheritedGroups == null)
		 {
			 m_oldInheritedGroups = new Vector();
		 }
		 return new Vector(m_oldInheritedGroups);
	 }

	 /**
	  * Determine whether a group has access to this item. 
	  * @param groupRef The internal reference string that uniquely identifies the group.
	  * @return true if the group has access, false otherwise.
	  */
	 public boolean hasGroup(String groupRef)
	 {
		 if(m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 boolean found = false;
		 Iterator it = m_groups.iterator();
		 while(it.hasNext() && !found)
		 {
			 Group gr = (Group) it.next();
			 found = gr.getReference().equals(groupRef);
		 }

		 return found;
	 }

	 /**
	  * Determine whether a group has access to this item. 
	  * @param groupRef The internal reference string that uniquely identifies the group.
	  * @return true if the group has access, false otherwise.
	  */
	 public boolean inheritsGroup(String groupRef)
	 {
		 if(m_oldInheritedGroups == null)
		 {
			 m_oldInheritedGroups = new Vector();
		 }
		 boolean found = false;
		 Iterator it = m_oldInheritedGroups.iterator();
		 while(it.hasNext() && !found)
		 {
			 Group gr = (Group) it.next();
			 found = gr.getReference().equals(groupRef);
		 }

		 return found;
	 }

	 /**
	  * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
	  * @param groups The groups to set.
	  */
	 public void setGroups(Collection groups)
	 {
		 if(groups == null)
		 {
			 return;
		 }
		 if(m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 m_groups.clear();
		 Iterator it = groups.iterator();
		 while(it.hasNext())
		 {
			 Object obj = it.next();
			 if(obj instanceof Group && ! hasGroup(((Group) obj).getReference()))
			 {
				 m_groups.add(obj);
			 }
			 else if(obj instanceof String && ! hasGroup((String) obj))
			 {
				 addGroup((String) obj);
			 }
		 }
	 }

	 /**
	  * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
	  * @param groups The groups to set.
	  */
	 public void setInheritedGroups(Collection groups)
	 {
		 if(groups == null)
		 {
			 return;
		 }
		 if(m_oldInheritedGroups == null)
		 {
			 m_oldInheritedGroups = new Vector();
		 }
		 m_oldInheritedGroups.clear();
		 Iterator it = groups.iterator();
		 while(it.hasNext())
		 {
			 Object obj = it.next();
			 if(obj instanceof Group && ! inheritsGroup(((Group) obj).getReference()))
			 {
				 m_oldInheritedGroups.add(obj);
			 }
			 else if(obj instanceof String && ! hasGroup((String) obj))
			 {
				 addInheritedGroup((String) obj);
			 }
		 }
	 }

	 /**
	  * Add a string reference identifying a Group to the list of groups that have access to this item.
	  * @param groupRef
	  */
	 public void addGroup(String groupId)
	 {
		 if(m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 if(m_container == null)
		 {
			 if(m_id == null)
			 {
				 m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			 }
			 else
			 {
				 m_container = ContentHostingService.getContainingCollectionId(m_id);
			 }
			 if(m_container == null || m_container.trim().length() == 0 )
			 {
				 m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			 }

		 }
		 boolean found = false;
		 Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
		 Iterator it = groups.iterator();
		 while( it.hasNext() && !found )
		 {
			 Group group = (Group) it.next();
			 if(group.getId().equals(groupId))
			 {
				 if(! hasGroup(group.getReference()))
				 {
					 m_groups.add(group);
				 }
				 found = true;
			 }
		 }

	 }

	 /**
	  * Add a Group to the list of groups that have access to this item.
	  * @param group The Group object to be added
	  */
	 public void addGroup(Group group) 
	 {
		 if(m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 if(! hasGroup(group.getReference()))
		 {
			 m_groups.add(group);
		 }
	 }



	 /**
	  * Add a string reference identifying a Group to the list of groups that have access to this item.
	  * @param groupRef
	  */
	 public void addInheritedGroup(String groupId)
	 {
		 if(m_oldInheritedGroups == null)
		 {
			 m_oldInheritedGroups = new Vector();
		 }
		 if(m_container == null)
		 {
			 if(m_id == null)
			 {
				 m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			 }
			 else
			 {
				 m_container = ContentHostingService.getContainingCollectionId(m_id);
			 }
			 if(m_container == null || m_container.trim().length() == 0 )
			 {
				 m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			 }

		 }
		 boolean found = false;
		 Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
		 Iterator it = groups.iterator();
		 while( it.hasNext() && !found )
		 {
			 Group group = (Group) it.next();
			 String gid = group.getId();
			 String gref = group.getReference();
			 if(gid.equals(groupId) || gref.equals(groupId))
			 {
				 if(! inheritsGroup(group.getReference()))
				 {
					 m_oldInheritedGroups.add(group);
				 }
				 found = true;
			 }
		 }

	 }

	 /**
	  * Remove all groups from the item.
	  */
	 public void clearGroups()
	 {
		 if(this.m_groups == null)
		 {
			 m_groups = new Vector();
		 }
		 m_groups.clear();
	 }

	 /**
	  * Remove all inherited groups from the item.
	  */
	 public void clearInheritedGroups()
	 {
		 if(m_oldInheritedGroups == null)
		 {
			 m_oldInheritedGroups = new Vector();
		 }
		 m_oldInheritedGroups.clear();
	 }

	 /**
	  * @return Returns the pubview.
	  */
	 public boolean isPubview() 
	 {
		 return m_pubview;
	 }
	 /**
	  * @param pubview The pubview to set.
	  */
	 public void setPubview(boolean pubview) 
	 {
		 m_pubview = pubview;
	 }

	 /**
	  * @param pubview The pubview to set.
	  */
	 public void setPubviewPossible(boolean possible) 
	 {
		 m_pubview_possible = possible;
	 }

	 /**
	  * @return Returns the pubviewset.
	  */
	 public boolean isPubviewInherited() 
	 {
		 return m_pubview_inherited;
	 }

	 /**
	  * 
	  *
	  */
	 public boolean isPubviewPossible()
	 {
		 return m_pubview_possible;
	 }

	 /**
	  * @param pubviewset The pubviewset to set.
	  */
	 public void setPubviewInherited(boolean pubviewset) 
	 {
		 m_pubview_inherited = pubviewset;
	 }

	 /**
	  * @return Returns the rights.
	  */
	 public BasicRightsAssignment getRights()
	 {
		 return m_rights;
	 }

	 /**
	  * @param rights The rights to set.
	  */
	 public void setRights(BasicRightsAssignment rights)
	 {
		 this.m_rights = rights;
	 }

	 /**
	  * @return Returns true if the item is in a dropbox (assuming it's been initialized correctly).
	  */
	 public boolean isInDropbox() 
	 {
		 return m_inDropbox;
	 }

	 /**
	  * @param inDropbox The value for inDropbox to set.
	  */
	 public void setInDropbox(boolean inDropbox) 
	 {
		 this.m_inDropbox = inDropbox;
	 }

	 public boolean isSortable()
	 {
		 return m_sortable;
	 }

	 public void setSortable(boolean sortable)
	 {
		 m_sortable = sortable;
	 }

}	//	 inner class ResourcesBrowseItem


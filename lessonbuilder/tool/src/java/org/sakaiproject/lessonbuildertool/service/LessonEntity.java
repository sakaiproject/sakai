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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Date;

import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;

/**
 * Interface to all assignments, tests and other external assignment-like things
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
public interface LessonEntity {

    public void setNextEntity(LessonEntity e);
    public LessonEntity getNextEntity();

    // can't put it here, but we need a zero-arg constuctor
    // that produces an instance with no usable information in it
    // it is used only to call getEntitiesInSite, getEntity, etc

    // type of the underlying object
    public int getType();
    // something like /sam/12345, used as SakaiId.
    // note that the original types are numbers without a type
    // so dispatch code will need to know the default. However
    // getReference will all always return something with a prefix
    public String getReference();

    // the constants are defined here because not all modules are necessarily
    // available but dispatch code may still need to understand
    // 0x is assignment type tools
    public final static int TYPE_ASSIGNMENT=1;
    public final static int TYPE_ASSIGNMENT2=2;
    public final static int TYPE_SCORM=3;
    // 1x is test type tools
    public final static int TYPE_SAMIGO=11;
    public final static int TYPE_MNEME=12;
    // 2x is forum type tools
    public final static int TYPE_FORUM_FORUM=21;   // msg center forum. shouldn't be in database
    public final static int TYPE_FORUM_TOPIC=22;   // only topic should be in database as items
    public final static int TYPE_JFORUM_CATEGORY=23;   // jforum category, shouldn't be in database
    public final static int TYPE_JFORUM_FORUM=24;   // jforum forum, shouldn't be in database
    public final static int TYPE_JFORUM_TOPIC=25;   // only topic should be in database as items
    public final static int TYPE_YAFT_TOPIC=26;   // only topic should be in database as items

    // 3x is blti, etc
    public final static int TYPE_BLTI=31;

    // prefixes. Use the /direct prefixes where  possible
    public final static String ASSIGNMENT = "assignment";
    public final static String ASSIGNMENT2 = "assignment2";
    public final static String SCORM = "scorm";
    public final static String SAM_PUB="sam_pub";
    public final static String MNEME="mneme";
    // only topic should be in the database. We need forum objects
    // because the chooser is a hiearchical and lists forums. Hopefully forums
    // will only occur in the chooser.
    public final static String FORUM_TOPIC = "forum_topic";
    public final static String FORUM_FORUM = "forum_forum";
    public final static String JFORUM_TOPIC = "jforum_topic";
    public final static String JFORUM_FORUM = "jforum_forum";
    public final static String JFORUM_CATEGORY = "jforum_category";
    
    public final static String YAFT_TOPIC = "yaft_topic";

    public final static String BLTI="blti";

    // find entities
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean);
    public List<LessonEntity> getEntitiesInSite();

    public LessonEntity getEntity(String ref, SimplePageBean bean);
    public LessonEntity getEntity(String ref);

    // returns common ID of tool, e.g. sakai.samigo
    // from that we can get other info
    public String getToolId();

    // properties of entities
    public String getTitle();
    public String getUrl();
    public Date getDueDate();
    // for forums, where we have a hiearchy of topics
    public int getLevel();
    // for forums, where some levels in hierarchy are aggregate and shouldn't be chosen
    public boolean isUsable();
    // only assignments
    public int getTypeOfGrade();

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException;
    public boolean removeEntityControl(String siteId, String groupId) throws IOException;

    // submission
    // one or the other must be defined. For forums at the moment we just get count
    public boolean needSubmission();  // do we need the data from submission?
    public LessonSubmission getSubmission(String user);
    public int getSubmissionCount(String user);

    // calls to original tool. they take the bean as an argument so they can get to
    // the current site and tool, and cache information

   // URL to create a new item. Normally called from the generic entity, not a specific one
    // can't be null.
    // it's a list because we support more than one kind of tool.
    public List<SimplePageBean.UrlItem> createNewUrls(SimplePageBean bean);

    // URL to edit an existing entity. 
    // Can be null if we can't get one or it isn't needed
    public String editItemUrl(SimplePageBean bean);

    // for most entities editItem is enough, however tests allow separate editing of
    // contents and settings. This will be null except in that situation
    public String editItemSettingsUrl(SimplePageBean bean);

 
    // checks to see if the underlying assignment, etc, actually exists
    public boolean objectExists();

    // currently used only for Samigo. What's different about Samigo's
    // unpublished items is that they don't exist as published assessments
    // at all, so we can't produce a reference to it.
    public boolean notPublished(String ref);

    // for other tools, typically used for drafts, which students can't use
    // this version is called on an instance
    public boolean notPublished();

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache);

    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups);

    // for saved XML. used for objectid property. It's data
    // about the Sakai object in the old site that we need
    // to locate it again. If the tool implements transferCopyRefMigrator,
    // this should be the normal Sakai object reference, as it will
    // appear in the map. For other tools, typically something like
    // the title is the only thing that will work.
    //   This objectid needs to include a tool name, because findObject is
    // going to have to know which of several tool implementations
    // to use. So a typical one might be assignment/NNNN
    // i.e. tool ID, assignment ID. Can return null if
    // this functionality isn't implemented. WIll result in a dummy
    // reference in the new site.
    public String getObjectId();

    // return a sakaiid for an object copied from another site.
    // If we can't identify it, return null; will need to chain
    // to other implementations of the tool type if the object ID 
    // isn't ours.
    public String findObject(String objectid, Map<String,String>objectMap, String siteid);

    // siteId for the object
    public String getSiteId();

}
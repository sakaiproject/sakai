/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PrivateTopicImpl.java $
 * $Id: PrivateTopicImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;
 
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;

@Slf4j
public class PrivateTopicImpl extends TopicImpl implements PrivateTopic {

    private String userId;
    private String contextId;
    private PrivateTopic parentTopic;
    private Set childrenFoldersSet;// = new HashSet();    
    
    //private int ptindex;
    
    
    public static Comparator<Topic> TITLE_COMPARATOR;
    
    private static final List<String> lookupOrderList = 
      Arrays.asList(new String[] {"pvt_received", "pvt_sent", "pvt_deleted", "pvt_drafts"}); 
    
    static {
      TITLE_COMPARATOR = new Comparator<Topic>()
      {                
        public int compare(Topic topic, Topic otherTopic)
        {
          if (topic != null && otherTopic != null
              && topic instanceof Topic && otherTopic instanceof Topic)
          {
//            title1 = ((Topic) topic).getTitle();
//            title2 = ((Topic) otherTopic).getTitle();
//            
//            index1 = Integer.valueOf(lookupOrderList.indexOf(title1));
//            index2 = Integer.valueOf(lookupOrderList.indexOf(title2));            
//                                    
//            /** expecting elements to exist in lookupOrderedList */
//            return index1.compareTo(index2);
          	if(lookupOrderList.indexOf(((Topic) topic).getTitle()) >= 0 && lookupOrderList.indexOf(((Topic) otherTopic).getTitle()) < 0)
          	{
          		return -1;
          	}
          	if(lookupOrderList.indexOf(((Topic) topic).getTitle()) < 0 && lookupOrderList.indexOf(((Topic) otherTopic).getTitle()) >= 0)
          	{
          		return 1;
          	}          	
          	Date date1=((Topic) topic).getCreated();
            Date date2=((Topic) otherTopic).getCreated();
            return date1.compareTo(date2);
          }
          return -1;
        }
      };
    }
        
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
		public String getContextId() {
			return contextId;
		}

		public void setContextId(String contextId) {
			this.contextId = contextId;
		}

    public PrivateTopic getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(PrivateTopic parentTopic) {
        this.parentTopic = parentTopic;
    }

    public Set getChildrenFoldersSet() {
        return childrenFoldersSet;
    }

    public void setChildrenFoldersSet(Set childrenFoldersSet) {
        this.childrenFoldersSet = childrenFoldersSet;
    }

    public List getChildrenFolders() {
        return Util.setToList(childrenFoldersSet);
    }

    public void setChildrenFolders(List childrenFolders) {
        this.childrenFoldersSet = Util.listToSet(childrenFolders);
    }

//
//    public int getPtindex() {
//        try {
//            return getParentTopic().getChildrenFolders().indexOf(this);
//        } catch (Exception e) {
//            return ptindex;
//        }
//    }
//
//    public void setPtindex(int ptindex) {
//        this.ptindex = ptindex;
//    }
    

    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addChildFolder(PrivateTopic folder) {
        if (log.isDebugEnabled()) {
            log.debug("addChildFolder(folder " + folder + ")");
        }
        
        if (folder == null) {
            throw new IllegalArgumentException("folder == null");
        }
        
        folder.setParentTopic(this);
        childrenFoldersSet.add(folder);
    }

    public void removeChildFolder(PrivateTopic folder) {
        if (log.isDebugEnabled()) {
            log.debug("removeChildFolder(folder " + folder + ")");
        }
        
        if (folder == null) {
            throw new IllegalArgumentException("Illegal folder argument passed!");
        }
        
        folder.setParentTopic(null);
        childrenFoldersSet.remove(folder);
    }
}

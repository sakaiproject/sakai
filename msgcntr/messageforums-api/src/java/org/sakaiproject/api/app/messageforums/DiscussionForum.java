/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/DiscussionForum.java $
 * $Id: DiscussionForum.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;

public interface DiscussionForum extends OpenForum {

    public ActorPermissions getActorPermissions();

    public void setActorPermissions(ActorPermissions actorPermissions);

    public DateRestrictions getDateRestrictions();

    public void setDateRestrictions(DateRestrictions dateRestrictions);

    public List getLabels();

    public void setLabels(List labels);
    
    public void addLabel(Label label);

    public void removeLabel(Label label);
    
    public Boolean getAutoMarkThreadsRead();
    
    public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead);
}
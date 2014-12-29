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

import java.util.Date;

public interface SimplePageLogEntry {

    public long getId();

    public void setId(long id);

    public Date getLastViewed();

    public void setLastViewed(Date lastViewed);

    public Date getFirstViewed();

    public void setFirstViewed(Date firstViewed);

    public String getUserId();

    public void setUserId(String userId);

    public long getItemId();

    public void setItemId(long itemId);

    public boolean isComplete();

    public void setComplete(boolean c);

    public boolean getDummy();

    public void setDummy(Boolean d);

    public String getPath();

    public void setPath(String path);

    // note that toolId is the tool from which this entry was
    // actually made. Because the same page can be accessed
    // from different locations, there's no static way to be
    // sure which tool a page was displayed in. We need to know in order
    // to find the last page accessed in a specific tool

    public String getToolId();

    public void setToolId(String toolId);
    
    public Long getStudentPageId();
    
    public void setStudentPageId(Long studentPageId);

}

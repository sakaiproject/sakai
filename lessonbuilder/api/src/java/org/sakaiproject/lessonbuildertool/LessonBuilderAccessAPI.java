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

import org.sakaiproject.entity.api.HttpAccess;
import java.io.File;
import java.io.InputStream;

public interface LessonBuilderAccessAPI {

    public void setHttpAccess(HttpAccess h);
    public HttpAccess getHttpAccess();
    public void setToolApi(ToolApi t);
    public String loadCartridge(File f, String d, String siteId);
    public String deleteOrphanPages(String siteId);
    
}




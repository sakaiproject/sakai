/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
 
public class MimeTypesLocator{

  private static Log log = LogFactory.getLog(MimeTypesLocator.class);
  private static MimeTypesLocator instance = null;
  private static MimetypesFileTypeMap map = null;

  public static MimeTypesLocator getInstance(){
    if (instance != null)
      return instance;
    else
      return new MimeTypesLocator();
  }

  public void setMimetypesFileTypeMap(MimetypesFileTypeMap map){
    this.map = map;
  } 

  public MimetypesFileTypeMap getMimetypesFileTypeMap(){
    return map;
  }

  public String getContentType(File file){
    return map.getContentType(file);
  }

}





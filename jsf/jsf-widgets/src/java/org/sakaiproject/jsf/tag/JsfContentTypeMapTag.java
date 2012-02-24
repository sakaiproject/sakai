/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

public class JsfContentTypeMapTag extends UIComponentTag {
   
   public static final String MAP_TYPE_IMAGE = "image";
   public static final String MAP_TYPE_NAME = "name";
   public static final String MAP_TYPE_EXTENSION = "extension";

   private String fileType = "";
   private String mapType = MAP_TYPE_IMAGE;
   private String pathPrefix;
   private String _var = null;
   
   public String getComponentType()
   {
      return "org.sakaiproject.JsfContentTypeMap";
   }

   public String getRendererType()
   {
      return "org.sakaiproject.JsfContentTypeMap";
   }


   /**
    * 
    * @param component     places the attributes in the component
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      TagUtil.setString(component, "fileType", fileType);
      TagUtil.setString(component, "mapType", mapType);
      TagUtil.setString(component, "pathPrefix", pathPrefix);
      TagUtil.setString(component, "var", _var);
   }

   public String getFileType() {
      return fileType;
   }

   public void setFileType(String fileType) {
      this.fileType = fileType;
   }

   public String getMapType() {
      return mapType;
   }

   public void setMapType(String mapType) {
      this.mapType = mapType;
   }

   public String getVar() {
      return _var;
   }

   public void setVar(String _var) {
      this._var = _var;
   }

   public String getPathPrefix() {
      return pathPrefix;
   }

   public void setPathPrefix(String pathPrefix) {
      this.pathPrefix = pathPrefix;
   }
   
   
}

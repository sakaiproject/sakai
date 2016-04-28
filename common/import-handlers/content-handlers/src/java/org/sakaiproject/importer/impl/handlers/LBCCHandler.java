/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/branches/sakai-10.x/import-handlers/content-handlers/src/java/org/sakaiproject/importer/impl/handlers/ResourcesHandler.java $
 * $Id: ResourcesHandler.java 106351 2012-03-28 20:21:21Z matthew@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.LBCCResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.sakaiproject.importer.impl.importables.TextDocument;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;

import javax.activation.MimetypesFileTypeMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LBCCHandler implements HandlesImportable {
	
	private Logger m_log = LoggerFactory.getLogger(org.sakaiproject.importer.impl.handlers.LBCCHandler.class);

	public boolean canHandleType(String typeName) {
	    return "lessonbuilder-cc-file".equals(typeName);
	}

	public void handle(Importable thing, String siteId) {
	    if(canHandleType(thing.getTypeName())){
		LessonBuilderAccessAPI lessonBuilderApi = (LessonBuilderAccessAPI)
		    ComponentManager.get("org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI");    
		LBCCResource file = (LBCCResource)thing;
		lessonBuilderApi.loadCartridge(null, file.getFileName(), siteId);
	    }
	}

}

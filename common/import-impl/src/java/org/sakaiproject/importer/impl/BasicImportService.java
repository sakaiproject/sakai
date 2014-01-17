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

package org.sakaiproject.importer.impl;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.ImportService;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.ResetOnCloseInputStream;

import java.io.InputStream;

public class BasicImportService implements ImportService {
	
	private List<ImportFileParser> parsers;
	private List<HandlesImportable> resourceHandlers;
	private ServerConfigurationService configService = 
		org.sakaiproject.component.cover.ServerConfigurationService.getInstance();

	public void doImportItems(Collection<Importable> importables, String siteId) {
		for(Importable thing: importables) {
			for(HandlesImportable handler: resourceHandlers) {
				if (handler.canHandleType(thing.getTypeName())) {
					handler.handle(thing, siteId);
				}
			}
		}
	}

	public boolean isValidArchive(ResetOnCloseInputStream archiveFileData) {
		return findParser(archiveFileData) != null;
	}

	private ImportFileParser findParser(ResetOnCloseInputStream archiveFileData) {
		for(ImportFileParser parser : parsers) {
			if(parser.isValidArchive(archiveFileData)){
				return parser;
			}
		}
		return null;
	}

	public ImportDataSource parseFromFile(ResetOnCloseInputStream archiveFileData) {
		ImportFileParser parser = findParser(archiveFileData);
		if (parser != null) {
			return parser.newParser().parse(archiveFileData, configService.getSakaiHomePath() + "archive");
		}
		// invalid or unsupported archive file
		// TODO this should probably throw an exception
		return null;
	}
	
	public void registerParser(ImportFileParser parser) {
		this.parsers.add(parser);
	}
	
	public void setParsers(List<ImportFileParser> parsers) {
		this.parsers = parsers;
	}
	
	public void registerResourceHandler(HandlesImportable handler) {
		this.resourceHandlers.add(handler);
	}
	
	public void setResourceHandlers(List<HandlesImportable> resourceHandlers) {
		this.resourceHandlers = resourceHandlers;
	}

}

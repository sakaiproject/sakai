package org.sakaiproject.importer.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.ImportService;
import org.sakaiproject.importer.api.Importable;

import org.sakaiproject.component.api.ServerConfigurationService;

public class BasicImportService implements ImportService {
	
	private List parsers;
	private List resourceHandlers;
	private ServerConfigurationService configService = 
		org.sakaiproject.component.cover.ServerConfigurationService.getInstance();

	public void doImportItems(Collection importables, String siteId) {
		HandlesImportable handler = null;
		for(Iterator i = importables.iterator();i.hasNext();) {
			Importable thing = (Importable)i.next();
			for(Iterator j = resourceHandlers.iterator();j.hasNext();) {
				handler = (HandlesImportable)j.next();
				if (handler.canHandleType(thing.getTypeName())) {
					handler.handle(thing, siteId);
					}
			}
		}

	}

	public boolean isValidArchive(byte[] archiveFileData) {
		boolean isValid = false;
		for(Iterator i = this.parsers.iterator();i.hasNext();) {
			if(((ImportFileParser)i.next()).isValidArchive(archiveFileData)){
				isValid = true;
				break;
			}
		}
		return isValid;
	}

	public ImportDataSource parseFromFile(byte[] archiveFileData) {
		for(Iterator i = this.parsers.iterator();i.hasNext();){
			ImportFileParser parser = (ImportFileParser)i.next();
			if(parser.isValidArchive(archiveFileData)){
				return parser.parse(archiveFileData, configService.getSakaiHomePath() + "archive");
			}
		}
		// invalid or unsupported archive file
		// TODO this should probably throw an exception
		return null;
	}
	
	public void registerParser(ImportFileParser parser) {
		this.parsers.add(parser);
	}
	
	public void setParsers(List parsers) {
		this.parsers = parsers;
	}
	
	public void registerResourceHandler(HandlesImportable handler) {
		this.resourceHandlers.add(handler);
	}
	
	public void setResourceHandlers(List resourceHandlers) {
		this.resourceHandlers = resourceHandlers;
	}

}

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.sakaiproject.archive.cover.ImportMetadataService;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;

@Slf4j
public class SakaiArchiveFileParser extends ZipFileParser {
	
	private static final String LEGACY_TOOL = "legacyTool";
	private static final String SAKAI_TOOL = "sakaiTool";
	private static final String ROOT = "importConfiguration";
	private static final String MAPPINGS = "mappings";
	private static final String MAP = "map";
	private static final String SERVICE_NAME = "serviceName";
	private static final String FILE_NAME = "filename";
	private static final String MANDATORY = "mandatory";
	private static final String ID = "id";
	
	protected Document importMappings;
	
	public boolean isValidArchive(InputStream fileData) {
		if (super.isValidArchive(fileData)) {
			if (!fileExistsInArchive("/import_mappings.xml", fileData)) 
				return false;
			return true;
		} else return false;
	}

	protected void awakeFromUnzip(String unArchiveLocation) {
		this.pathToData = unArchiveLocation;
		String absolutepathToManifest = pathToData + "/" + "import_mappings.xml";
	    absolutepathToManifest = absolutepathToManifest.replace('\\', '/');
        InputStream fis = null;
        try {
            fis = new FileInputStream(absolutepathToManifest);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            this.importMappings = (Document) docBuilder.parse(fis);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

	}
	
	public ImportFileParser newParser() {
		return new SakaiArchiveFileParser();
	}
	
	public ImportDataSource parse(InputStream fileData, String unArchiveLocation) {
		this.localArchiveLocation = unzipArchive(fileData, unArchiveLocation);
		this.pathToData = unArchiveLocation + "/" + localArchiveLocation;
		awakeFromUnzip(pathToData);
		List categories = new ArrayList();
		Collection items = new ArrayList();
		categories.addAll(getCategoriesFromArchive(pathToData));
		items.addAll(getImportableItemsFromArchive(pathToData));
		
		SakaiArchiveDataSource dataSource = new SakaiArchiveDataSource(fileData, localArchiveLocation, pathToData);
	    dataSource.setItemCategories(categories);
	    dataSource.setItems(items);
	    return dataSource;
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		return ImportMetadataService.getImportMetadataElements(importMappings);
	}

	protected Collection getImportableItemsFromArchive(String pathToData) {
		return new ArrayList();
	}
	
//	public List getImportMetadataElements(Document doc)
//	  {
//	    if (doc == null)
//	    {
//	      throw new IllegalArgumentException("Illegal document argument!");
//	    }
//	    else
//	    {
//	      //TODO: Validate the Doc against DTD
//	      Element root = doc.getDocumentElement();
//	      if (root.getTagName().equals(ROOT))
//	      {
//	        NodeList rootNodeList = root.getChildNodes();
//	        final int length = rootNodeList.getLength();
//	        for (int i = 0; i < length; i++)
//	        {
//	          Node mapping = rootNodeList.item(i);
//	          if (mapping.getNodeType() != Node.ELEMENT_NODE)
//	          {
//	            continue;
//	          }
//	          Element mappingElement = (Element) mapping;
//	          if (mappingElement.getTagName().equals(MAPPINGS))
//	          {
//	            List maps = new ArrayList();
//	            NodeList mapNode = mappingElement.getChildNodes();
//	            final int mapLength = mapNode.getLength();
//	            for (int j = 0; j < mapLength; j++)
//	            {
//	              Node mapNodes = mapNode.item(j);
//	              if (mapNodes.getNodeType() != Node.ELEMENT_NODE)
//	              {
//	                continue;
//	              }
//	              Element mapElement = (Element) mapNodes;
//	              if (mapElement.getTagName().equals(MAP))
//	              {
//	                ImportMetadataImpl importMetadataMap = new ImportMetadataImpl();
//	                importMetadataMap.setId(mapElement.getAttribute(ID));
//	                importMetadataMap.setFileName(mapElement
//	                    .getAttribute(FILE_NAME));
//	                importMetadataMap.setLegacyTool(mapElement
//	                    .getAttribute(LEGACY_TOOL));
//
//	                importMetadataMap.setSakaiTool(mapElement
//	                    .getAttribute(SAKAI_TOOL));
//	                importMetadataMap.setSakaiServiceName(mapElement
//	                    .getAttribute(SERVICE_NAME));
//	                if (mapElement.getAttribute(MANDATORY) != null
//	                    && mapElement.getAttribute(MANDATORY).length() > 0
//	                    && mapElement.getAttribute(MANDATORY).endsWith("true"))
//	                {
//	                  importMetadataMap.setMandatory(true);
//	                }
//	                maps.add(importMetadataMap);
//	              }
//	            }
//	            // import_mapping shall contain only one mapping element, after the
//	            // first one is done return
//	            return maps;
//	          }
//	        }
//	      }
//	    }
//
//	    return null;
//	  }
	    
//	    public class ImportMetadataImpl implements ImportMetadata
//	    {
//
//	      private String id;
//	      private String legacyTool;
//	      private String sakaiTool;
//	      private String sakaiServiceName;
//	      private String fileName;
//	      private boolean mandatory = false;
//
//	      /**
//	       * Should only be constructed by ImportMetadataService.
//	       */
//	      ImportMetadataImpl()
//	      {
//	      }
//
//	      /**
//	       * @return Returns the id.
//	       */
//	      public String getId()
//	      {
//	        return id;
//	      }
//
//	      /**
//	       * @param id
//	       *          The id to set.
//	       */
//	      public void setId(String id)
//	      {
//	        this.id = id;
//	      }
//
//	      /**
//	       * @return Returns the fileName.
//	       */
//	      public String getFileName()
//	      {
//	        return fileName;
//	      }
//
//	      /**
//	       * @param fileName
//	       *          The fileName to set.
//	       */
//	      public void setFileName(String fileName)
//	      {
//	        this.fileName = fileName;
//	      }
//
//	      /**
//	       * @return Returns the legacyTool.
//	       */
//	      public String getLegacyTool()
//	      {
//	        return legacyTool;
//	      }
//
//	      /**
//	       * @param legacyTool
//	       *          The legacyTool to set.
//	       */
//	      public void setLegacyTool(String legacyTool)
//	      {
//	        this.legacyTool = legacyTool;
//	      }
//
//	      /**
//	       * @return Returns the mandatory.
//	       */
//	      public boolean isMandatory()
//	      {
//	        return mandatory;
//	      }
//
//	      /**
//	       * @param mandatory
//	       *          The mandatory to set.
//	       */
//	      public void setMandatory(boolean mandatory)
//	      {
//	        this.mandatory = mandatory;
//	      }
//
//	      /**
//	       * @return Returns the sakaiServiceName.
//	       */
//	      public String getSakaiServiceName()
//	      {
//	        return sakaiServiceName;
//	      }
//
//	      /**
//	       * @param sakaiServiceName
//	       *          The sakaiServiceName to set.
//	       */
//	      public void setSakaiServiceName(String sakaiServiceName)
//	      {
//	        this.sakaiServiceName = sakaiServiceName;
//	      }
//
//	      /**
//	       * @return Returns the sakaiTool.
//	       */
//	      public String getSakaiTool()
//	      {
//	        return sakaiTool;
//	      }
//
//	      /**
//	       * @param sakaiTool
//	       *          The sakaiTool to set.
//	       */
//	      public void setSakaiTool(String sakaiTool)
//	      {
//	        this.sakaiTool = sakaiTool;
//	      }
//
//	    }
}

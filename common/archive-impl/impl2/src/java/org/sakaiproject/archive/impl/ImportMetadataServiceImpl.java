/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.archive.api.ImportMetadataService;

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri </a>
 * @version $Id$
 *  
 */
@Slf4j
public class ImportMetadataServiceImpl implements ImportMetadataService
{
  private static final String ROOT = "importConfiguration";
  private static final String MAPPINGS = "mappings";
  private static final String MAP = "map";
  private static final String LEGACY_TOOL = "legacyTool";
  private static final String SAKAI_TOOL = "sakaiTool";
  private static final String SERVICE_NAME = "serviceName";
  private static final String FILE_NAME = "filename";
  private static final String MANDATORY = "mandatory";

  // For Site.xml
  private static final String SITE_ROOT = "archive";
  private static final String SITE_SERVICE = "org.sakaiproject.site.api.SiteService";
  private static final String SITE_APPLICATION_ID = "sakai:site";
  private static final String SITE = "site";
  private static final String SITE_ROLES = "roles";
  private static final String SITE_ROLE_MAINTAIN = "maintain";
  private static final String SITE_ROLE_ABILITY = "ability";
  private static final String SITE_ROLE_MAINTAIN_USERID = "userId";
  private static final String SITE_ROLE_ID = "roleId";
  private static final String ID = "id";

  private Document document = null;

  /* (non-Javadoc)
   * @see org.sakaiproject.service.legacy.archive.ImportMetadataService#getImportMetadataElements(org.w3c.dom.Document)
   */
  public List getImportMetadataElements(Document doc)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getImportMetadataElements(Document" + doc + ")");
    }
    if (doc == null)
    {
      throw new IllegalArgumentException("Illegal document argument!");
    }
    else
    {
      this.document = doc;
      //TODO: Validate the Doc against DTD
      Element root = doc.getDocumentElement();
      if (root.getTagName().equals(ROOT))
      {
        NodeList rootNodeList = root.getChildNodes();
        final int length = rootNodeList.getLength();
        for (int i = 0; i < length; i++)
        {
          Node mapping = rootNodeList.item(i);
          if (mapping.getNodeType() != Node.ELEMENT_NODE)
          {
            continue;
          }
          Element mappingElement = (Element) mapping;
          if (mappingElement.getTagName().equals(MAPPINGS))
          {
            List maps = new ArrayList();
            NodeList mapNode = mappingElement.getChildNodes();
            final int mapLength = mapNode.getLength();
            for (int j = 0; j < mapLength; j++)
            {
              Node mapNodes = mapNode.item(j);
              if (mapNodes.getNodeType() != Node.ELEMENT_NODE)
              {
                continue;
              }
              Element mapElement = (Element) mapNodes;
              if (mapElement.getTagName().equals(MAP))
              {
                ImportMetadataImpl importMetadataMap = new ImportMetadataImpl();
                importMetadataMap.setId(mapElement.getAttribute(ID));
                importMetadataMap.setFileName(mapElement
                    .getAttribute(FILE_NAME));
                importMetadataMap.setLegacyTool(mapElement
                    .getAttribute(LEGACY_TOOL));

                importMetadataMap.setSakaiTool(mapElement
                    .getAttribute(SAKAI_TOOL));
                importMetadataMap.setSakaiServiceName(mapElement
                    .getAttribute(SERVICE_NAME));
                if (mapElement.getAttribute(MANDATORY) != null
                    && mapElement.getAttribute(MANDATORY).length() > 0
                    && mapElement.getAttribute(MANDATORY).endsWith("true"))
                {
                  importMetadataMap.setMandatory(true);
                }
                maps.add(importMetadataMap);
              }
            }
            // import_mapping shall contain only one mapping element, after the
            // first one is done return
            return maps;
          }
        }
      }
    }

    return null;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.service.legacy.archive.ImportMetadataService#getImportMapById(java.lang.String)
   */
  public ImportMetadata getImportMapById(String id)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getImportMapById(String" + id + ")");
    }
    if (id == null || id.length() < 1)
    {
      throw new IllegalArgumentException("Illegal id argument!");
    }
    if (this.document == null)
    {
      log.error("No valid document found");
      return null;
    }
    Element root = document.getDocumentElement();
    if (root.getTagName().equals(ROOT))
    {
      NodeList rootNodeList = root.getChildNodes();
      final int length = rootNodeList.getLength();
      for (int i = 0; i < length; i++)
      {
        Node mapping = rootNodeList.item(i);
        if (mapping.getNodeType() != Node.ELEMENT_NODE)
        {
          continue;
        }
        Element mappingElement = (Element) mapping;
        if (mappingElement.getTagName().equals(MAPPINGS))
        {
          NodeList mapNode = mappingElement.getChildNodes();
          final int mapLength = mapNode.getLength();
          for (int j = 0; j < mapLength; j++)
          {
            Node mapNodes = mapNode.item(j);
            if (mapNodes.getNodeType() != Node.ELEMENT_NODE)
            {
              continue;
            }
            Element mapElement = (Element) mapNodes;
            if (mapElement.getTagName().equals(MAP)
                && mapElement.getAttribute(ID) != null
                && mapElement.getAttribute(ID).equals(id))
            {
              ImportMetadataImpl importMetadataMap = new ImportMetadataImpl();
              importMetadataMap.setId(mapElement.getAttribute(ID));

              importMetadataMap.setFileName(mapElement.getAttribute(FILE_NAME));
              importMetadataMap.setLegacyTool(mapElement
                  .getAttribute(LEGACY_TOOL));

              importMetadataMap.setSakaiTool(mapElement
                  .getAttribute(SAKAI_TOOL));
              importMetadataMap.setSakaiServiceName(mapElement
                  .getAttribute(SERVICE_NAME));
              if (mapElement.getAttribute(MANDATORY) != null
                  && mapElement.getAttribute(MANDATORY).length() > 0
                  && mapElement.getAttribute(MANDATORY).endsWith("true"))
              {
                importMetadataMap.setMandatory(true);
              }
              return importMetadataMap;
            }
          }
        }
      }
    }

    return null;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.service.legacy.archive.ImportMetadataService#hasMaintainRole(java.lang.String, org.w3c.dom.Document)
   */
  public boolean hasMaintainRole(String username, Document siteDoc)
  {
    if (log.isDebugEnabled())
    {
      log.debug("hasMaintainRole(Document" + siteDoc + ")");
    }
    if (username == null || username.length() < 1)
    {
      throw new IllegalArgumentException("Illegal username argument!");
    }
    if (siteDoc == null)
    {
      throw new IllegalArgumentException("Illegal document argument!");
    }
    else
    {
      //TODO: Validate the Doc against Site DTD
      Element root = siteDoc.getDocumentElement();
      if (root.getTagName().equals(SITE_ROOT))
      {
        NodeList rootNodeList = root.getChildNodes();
        final int length = rootNodeList.getLength();
        for (int i = 0; i < length; i++)
        {
          Node service = rootNodeList.item(i);
          if (service.getNodeType() != Node.ELEMENT_NODE)
          {
            continue;
          }
          Element serviceElement = (Element) service;
          if (serviceElement.getTagName().equals(SITE_SERVICE) || serviceElement.getTagName().equals(SITE_APPLICATION_ID))
          {
            NodeList siteNodes = serviceElement.getChildNodes();
            final int siteNodeLength = siteNodes.getLength();
            for (int j = 0; j < siteNodeLength; j++)
            {
              Node siteNode = siteNodes.item(j);
              if (siteNode.getNodeType() != Node.ELEMENT_NODE)
              {
                continue;
              }
              Element siteElement = (Element) siteNode;
              if (siteElement.getTagName().equals(SITE))
              {

                NodeList rolesNodes = siteElement.getChildNodes();
                final int rolesNodeLength = rolesNodes.getLength();
                for (int k = 0; k < rolesNodeLength; k++)
                {
                  Node rolesNode = rolesNodes.item(k);
                  if (rolesNode.getNodeType() != Node.ELEMENT_NODE)
                  {
                    continue;
                  }
                  Element roleElement = (Element) rolesNode;
                  if (roleElement.getTagName().equals(SITE_ROLES))
                  {

                    NodeList mtNodes = roleElement.getChildNodes();
                    final int mtLength = mtNodes.getLength();
                    for (int l = 0; l < mtLength; l++)
                    {
                      Node mtNode = mtNodes.item(l);
                      if (mtNode.getNodeType() != Node.ELEMENT_NODE)
                      {
                        continue;
                      }
                      Element mtElement = (Element) mtNode;
                      if (mtElement.getTagName().equals(SITE_ROLE_MAINTAIN))
                      {
                        NodeList abNodes = mtElement.getChildNodes();
                        final int abLength = abNodes.getLength();
                        for (int m = 0; m < abLength; m++)
                        {
                          Node abNode = abNodes.item(m);
                          if (abNode.getNodeType() != Node.ELEMENT_NODE)
                          {
                            continue;
                          }
                          Element abElement = (Element) abNode;
                          if (abElement.getTagName().equals(SITE_ROLE_ABILITY))
                          {
                            String siteUserID = abElement
                                .getAttribute(SITE_ROLE_MAINTAIN_USERID);
                            String userRole = abElement
                            .getAttribute(SITE_ROLE_ID);
                            if (siteUserID != null
                                && siteUserID.trim().length() > 0
                                && siteUserID.equals(username) && userRole.equals(SITE_ROLE_MAINTAIN))
                            {
                              return true;
                            }

                          }
                        }

                      }
                    }

                  }
                }

              }
            }

          }
        }
      }
    }
    return false;
  }
}

/**
 * Copyright (c) 2009-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.basiclti.impl;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Slf4j
public class BasicLTIArchiveBean {
        private String pageTitle = null;
        private String toolTitle = null;
        private Properties siteToolProperties = new Properties();
        
        public final static String ALIAS = "basicLTI";
        public final static String PAGE_TITLE = "pageTitle";
        public final static String TOOL_TITLE = "toolTitle";
        public final static String SITE_TOOL_PROPERTIES = "siteToolProperties";

        public BasicLTIArchiveBean()
        {
        }
        
        public BasicLTIArchiveBean(Node basicLTI) throws Exception
        {
        	// Parse basicLTI Element Node and populate fields in BasicLTI bean
        	// The only fields should be pageTitle, toolTitle and siteToolProperties
        	if(basicLTI.getChildNodes().getLength() != 3)
        	{
        		throw new Exception("Invalid number of child Nodes for basicLTI Node.");
        	}
        	for(int i=0; i < basicLTI.getChildNodes().getLength(); i++)
        	{
        		// This node is a child of node basicLTI
        		Node basicLTIChildNode = basicLTI.getChildNodes().item(i);
            	if(basicLTIChildNode.getNodeName().equals(BasicLTIArchiveBean.PAGE_TITLE))
        		{
        			if(this.pageTitle != null)
        			{
        				throw new Exception("Multiple pageTitle Nodes nested within basicLTI Node.");
        			}
        			this.pageTitle = basicLTIChildNode.getTextContent();
        		}
            	else if(basicLTIChildNode.getNodeName().equals(BasicLTIArchiveBean.TOOL_TITLE))
        		{
        			if(this.toolTitle != null)
        			{
        				throw new Exception("Multiple toolTitle Nodes nested within basicLTI Node.");
        			}
        			this.toolTitle = basicLTIChildNode.getTextContent();
        		}
        		else if(basicLTIChildNode.getNodeName().equals(BasicLTIArchiveBean.SITE_TOOL_PROPERTIES))
        		{
        			// if siteToolProperties has already been populated
        			if(this.getSiteToolProperties().keySet().size() != 0)
        			{
        				throw new Exception("Multiple siteToolProperties Nodes nested within basicLTI Node.");
        			}
        			for(int j=0; j < basicLTIChildNode.getChildNodes().getLength(); j++)
        			{
        				Node propertyNode = basicLTIChildNode.getChildNodes().item(j);
        				String name  = propertyNode.getAttributes().getNamedItem("name").getTextContent();
        				String value = propertyNode.getAttributes().getNamedItem("value").getTextContent();
        				if(this.getSiteToolProperties().containsKey(name))
        				{
        					throw new Exception("Duplicate property " + name);
        				}
        				this.getSiteToolProperties().setProperty(name, value);
        			}
        		}
        		else
        		{
        			throw new Exception("Unrecognized Node " + basicLTIChildNode.getNodeName() + " in basicLTI Node");
        		}
        	}
        }

        public String getPageTitle()
        {
        	return this.pageTitle;
        }
        
        public String getToolTitle()
        {
        	return this.toolTitle;
        }

        public void setPageTitle(String title)
        {
                this.pageTitle = title;
        }
        
        public void setToolTitle(String title)
        {
                this.toolTitle = title;
        }
        
        public Properties getSiteToolProperties()
        {
        	return this.siteToolProperties;
        }
        
        public void setSiteToolProperties(Properties siteToolProperties)
        {
        	this.siteToolProperties = siteToolProperties;
        }
        
        public Node toNode(Document doc)
        {
        	Node node = null;
    		log.debug("Building node for {}", this.getPageTitle());
			// The alias is the name of the root element -- basicLTI
			// Look at the XStream documentation to see why I chose the term "alias"
			node = doc.createElement(BasicLTIArchiveBean.ALIAS);
			
			Node pageTitleNode = doc.createElement(BasicLTIArchiveBean.PAGE_TITLE);
			pageTitleNode.setTextContent(this.getPageTitle());
			node.appendChild(pageTitleNode);
			
			Node toolTitleNode = doc.createElement(BasicLTIArchiveBean.TOOL_TITLE);
			toolTitleNode.setTextContent(this.getToolTitle());
			node.appendChild(toolTitleNode);
			
			Node propertiesNode = doc.createElement(BasicLTIArchiveBean.SITE_TOOL_PROPERTIES);
			for(Object key: this.getSiteToolProperties().keySet())
			{
				Attr name = doc.createAttribute("name");
				name.setValue((String)key);
				Attr value = doc.createAttribute("value");
				value.setValue(this.getSiteToolProperties().getProperty((String)key));
				
				Node property = doc.createElement("property");
				property.getAttributes().setNamedItem(name);
				property.getAttributes().setNamedItem(value);
				propertiesNode.appendChild(property);
			}
			node.appendChild(propertiesNode);
        	return node;
        }
}

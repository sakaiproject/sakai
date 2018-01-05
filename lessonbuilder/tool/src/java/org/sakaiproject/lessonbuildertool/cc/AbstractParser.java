/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.cc;

/***********
 * This code is based on a reference implementation done for the IMS Consortium.
 * The copyright notice for that implementation is included below. 
 * All modifications are covered by the following copyright notice.
 *
 * Copyright (c) 2011 Rutgers, the State University of New Jersey
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
 */


/**********************************************************************************
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/AbstractParser.java $
 * $Id: AbstractParser.java 227 2011-01-08 18:26:55Z drchuck $
 **********************************************************************************
 *
 * Copyright (c) 2010 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 *
 **********************************************************************************/

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

@Slf4j
public abstract class AbstractParser {
  private static final String RESOURCE_QUERY="ims:resource[@identifier='xxx']";
  private static final String MD_ROOT="lom";
  private static final String METADATA="metadata";
  private static final String DEPENDENCY="dependency";
  private static final String FILE="file";
  private static final String HREF="href";
  private static final String IDREF="identifierref";
  private static final String ID="identifier";
  private static SAXBuilder builder;
  private static final String PROT_NAME ="protected";
  private static final Namespace AUTH_NS = Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccauth_v1p0");
  
    // This will disable all external entities. The ones we really care
    // about are file:. We could check the IDs and allow some forms, e.g.
    // http. For the moment we're disabling them all.

  public static class NoOpEntityResolver implements EntityResolver {
      public InputSource resolveEntity(String publicId, String systemId) {
	  return new InputSource(new StringBufferInputStream(""));
      }
  }

  static {
    builder=new SAXBuilder();
    // the normal feature approach doesn't seem to work.
    // this will disable processing of external entities. internal still work
    builder.setEntityResolver(new NoOpEntityResolver());
  }
  
  // set by Parser
  Ns ns = null;

  public void
  processDependencies(DefaultHandler the_handler,
                      Element the_resource) throws ParseException {
      for (Iterator iter=the_resource.getChildren(DEPENDENCY, the_handler.getNs().getNs()).iterator(); iter.hasNext();) {
      String target=((Element)iter.next()).getAttributeValue(IDREF);
      Element resource=findResource(the_handler.getNs(),target,the_resource.getParentElement());
      the_handler.startDependency(the_resource.getAttributeValue(ID),target);
      processResource(resource, the_handler);
      the_handler.endDependency();
    }
  }
  
  public void
  processFiles(DefaultHandler the_handler,
               Element the_resource) {
	  for (Iterator iter=the_resource.getChildren(FILE, the_handler.getNs().getNs()).iterator(); iter.hasNext();) {
		  the_handler.addFile((Element)iter.next());
	  }
  }

  public void
  processResourceMetadata(DefaultHandler the_handler,
                          Element the_resource) throws ParseException {
      Element md = the_resource.getChild(METADATA, the_handler.getNs().getNs());
      if (md != null) {
	 the_handler.checkCurriculum(md);
	 md=the_resource.getChild(METADATA, the_handler.getNs().getNs()).getChild(MD_ROOT, the_handler.getNs().lom_ns());
	 if (md!=null) {    
	     the_handler.setResourceMetadataXml(md);
	 }
      }
  }
  
  public Element
  getXML(CartridgeLoader the_cartridge,
         String the_file) throws IOException, ParseException {
    Element result=null;
    try {
	result=builder.build(the_cartridge.getFile(the_file)).getRootElement();
      XMLOutputter outputter = new XMLOutputter();
    } catch (Exception e) {
      throw new ParseException(e);
    }
    return result;
  }
  
  public void
  processResource(Element the_resource,
                  DefaultHandler handler) throws ParseException {
      try {
    handler.startResource(the_resource.getAttributeValue(ID), isProtected(the_resource));
    handler.setResourceXml(the_resource);
    processResourceMetadata(handler, the_resource);
    processFiles(handler, the_resource);
    processDependencies(handler, the_resource);
    handler.endResource();
      } catch (Exception e) {
	  log.error(e.getMessage(), e);
          if (the_resource == null)
              log.info("processresouce the item null");
          else
              log.info("processresource failed " + the_resource.getAttributeValue(ID));

      };
  }
  
  public Element
  findResource(Ns ns, String the_identifier, Element the_resources) throws ParseException {
    Element result=null;
    try {
      String query=RESOURCE_QUERY.replaceFirst("xxx", the_identifier);
      XPath path=XPath.newInstance(query);
      path.addNamespace(ns.getNs());
      result= (Element)path.selectSingleNode(the_resources);
    } catch (JDOMException e) {
      throw new ParseException(e.getMessage(),e);
    }
    return result;
  }
  
  public boolean
  isProtected(Element the_resource) {
    return the_resource.getAttribute(PROT_NAME, AUTH_NS)!=null;
  }
  
}

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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/Parser.java $
 * $Id: Parser.java 227 2011-01-08 18:26:55Z drchuck $
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * 
 * This is a simple cartridge parser library for IMS common cartridges. This parser is non validating, and has been
 * written against version 1.0 of the CC specification.
 * 
 * Users of the parser need to do the following:
 * 1) Create a Cartridge Loader, and supply it with a ZIP file.
 * 2) Create a Parser: the createCartridgeParser(CartridgeLoader) method in this class.
 * 3) Override DefaultHandler to process the events that arise during the parse process.
 * 4) Call Parser.parse(DefaultHandler) with your default Handler.
 * 
 * The parser will read the manifest file, as well as any declared xml resources (question banks, assessments, discussions,
 * and weblinks). DefaultHandler will always return xml in the form of JDOM elements, as well as (in some cases), java
 * objects (strings mostly). The parser will also return the details for authorization services, metadata and indicate if 
 * a resource is protected or not
 * 
 * @author Phil Nicholls
 * @version 1.0
 *
 */
@Slf4j
public class Parser extends AbstractParser {
  CartridgeLoader utils;

  private static final String IMS_MANIFEST="imsmanifest.xml";
  
  private static final String AUTH_QUERY="/ims:manifest/auth:authorizations";
  private static final String ORG_QUERY="/ims:manifest/ims:organizations/ims:organization";
  private static final String ITEM_QUERY="/ims:manifest/ims:organizations/ims:organization/ims:item";
  private static final String FILE_QUERY="//ims:file/@href";
  
  private static final String AUTH_IMPORT="import";
  private static final String AUTH_ACCESS="access";
  private static final String AUTH_ACCESS_CARTRIDGE="cartridge";
  private static final String AUTH_ACCESS_RESOURCE="resource";
  
  private static final String AUTH_AUTHORIZATION="authorization";
  private static final String AUTH_CCID="cartridgeId";
  private static final String AUTH_WEBSERVICE="webservice";
  
  private static final String MD="metadata";
  private static final String MD_SCHEMA="schema";
  private static final String MD_SCHEMA_VERSION="schemaversion";
  private static final String MD_ROOT="lom";
  
  private static final String CC_ITEM="item";
  private static final String CC_ITEM_ID="identifier";
  private static final String CC_ITEM_IDREF="identifierref";
  private static final String CC_ITEM_TITLE="title";
  private static final String CC_RESOURCE="resource";
  private static final String CC_FILE="file";
  private static final String QUESTION_BANK="question-bank";
  private static final String CC_RESOURCES="resources";
  private static final String CC_RES_TYPE="type";
  
  private
  Parser(CartridgeLoader the_cu) {
    super();
    utils=the_cu;
  }
  
  public void
  parse(DefaultHandler the_handler) throws FileNotFoundException, ParseException {
    try {
      Element manifest=this.getXML(utils, IMS_MANIFEST);
      processManifest(manifest, the_handler);
    } catch (Exception e) {
	the_handler.getSimplePageBean().setErrKey("simplepage.cc-error", "");
	log.info("parse error, stack trace follows " + e);
    }
  }
  
  private void
  processManifest(Element the_manifest, DefaultHandler the_handler) throws ParseException {
    ns = new Ns();
    the_handler.setNs(ns);
    // figure out which version we have, and set up ns to know about it
    int v = 0;
    for (; v < ns.getVersions(); v++) {
		ns.setVersion(v);
		// see if the namespace from the main manifest entry matches the candidate
		if (the_manifest.getNamespace().equals(ns.cc_ns())) {
			ns.setNs(ns.cc_ns());
			ns.setLom(ns.lomimscc_ns());
		    break;
	    }
	    else if (the_manifest.getNamespace().equals(ns.cp_ns())) {
	    	ns.setNs(ns.cp_ns());
	    	ns.setLom(ns.lomimscp_ns());
	    	break;
	    }
    }
    if (v >= ns.getVersions()) {
	the_handler.getSimplePageBean().setErrMessage(
	      the_handler.getSimplePageBean().getMessageLocator().getMessage("simplepage.wrong-cc-version"));
	return;
    }

    log.debug("Found version " + ns.getNs());

    the_handler.startManifest();
    the_handler.setManifestXml(the_manifest);
    if (processAuthorization(the_manifest, the_handler))
	return; // don't process CCs with authorization
    processManifestMetadata(the_manifest, the_handler);
    preProcessResources(the_manifest.getChild(CC_RESOURCES, ns.getNs()), the_handler);
    XPath path=null;
    Element org=null;
    try {
      path=XPath.newInstance(ORG_QUERY);
      path.addNamespace(ns.getNs());
      org = (Element)path.selectSingleNode(the_manifest);
      if (org!=null) {     
        for (Iterator iter=org.getChildren(CC_ITEM, ns.getNs()).iterator();iter.hasNext();) {
	    Element thisitem = (Element)iter.next();
          processItem((Element)thisitem, the_manifest.getChild(CC_RESOURCES, ns.getNs()), the_handler);
        }
      } 
      //now we need to check for the question bank and omitted dependencies
      if (the_manifest.getChild(CC_RESOURCES, ns.getNs()) != null &&
	  the_manifest.getChild(CC_RESOURCES, ns.getNs()).getChildren(CC_RESOURCE, ns.getNs()) != null)
      for (Iterator iter=the_manifest.getChild(CC_RESOURCES, ns.getNs()).getChildren(CC_RESOURCE, ns.getNs()).iterator(); iter.hasNext(); ) {
	  // this is called for question banks and other things that aren't really items, but that's OK
        Element resource=(Element)iter.next();
	// create the resource if it wasn't already on a page
	the_handler.setCCItemXml(null, resource, this, utils, true);
	processResource(resource, the_handler);
      }
      the_handler.endManifest();
    } catch (JDOMException e) {
      log.warn(e.getMessage());
      throw new ParseException(e.getMessage(),e);
    }
  }
  
  private void 
  preProcessResources(Element the_manifest,
		  DefaultHandler the_handler) {
	  XPath path;
	  List<Attribute> results;
	  try {
		  path = XPath.newInstance(FILE_QUERY);
		  path.addNamespace(ns.getNs());
		  results = path.selectNodes(the_manifest);
		  for (Attribute result : results) {
			  the_handler.preProcessFile(result.getValue()); 
		  }
	  } catch (JDOMException | ClassCastException e) {
		  log.info("Error processing xpath for files", e);
	  }
  }

  private void 
  processManifestMetadata(Element manifest,
                          DefaultHandler the_handler) {
    Element metadata=manifest.getChild(MD, ns.getNs());
    if (metadata!=null) {
      the_handler.startManifestMetadata(metadata.getChildText(MD_SCHEMA, ns.getNs()), 
                                        metadata.getChildText(MD_SCHEMA_VERSION, ns.getNs()));
      the_handler.checkCurriculum(metadata);
      Element lom=metadata.getChild(MD_ROOT, ns.getLom());
      if (lom!=null) {
        the_handler.setManifestMetadataXml(lom);
        the_handler.endManifestMetadata();
      } else
        the_handler.setManifestMetadataXml(null);
    } else
        the_handler.setManifestMetadataXml(null);
  }

  private boolean
  processAuthorization(Element the_manifest,
                       DefaultHandler the_handler) throws ParseException {
    try {
      XPath path=XPath.newInstance(AUTH_QUERY);
      path.addNamespace(ns.getNs());
      path.addNamespace(ns.auth_ns());
      Element result=(Element)path.selectSingleNode(the_manifest);
      if (result!=null) {
	  the_handler.getSimplePageBean().setErrMessage(
	     the_handler.getSimplePageBean().getMessageLocator().getMessage("simplepage.cc-uses-auth"));
	  return true;
	  //        String import_scope=result.getAttributeValue(AUTH_IMPORT);
	  //        String access_scope=result.getAttributeValue(AUTH_ACCESS);
	  //        if (access_scope.equals(AUTH_ACCESS_CARTRIDGE)) {
	  //          the_handler.startAuthorization(true, false, Boolean.parseBoolean(import_scope));
	  //        } else {
	  //          if (access_scope.equals(AUTH_ACCESS_RESOURCE)) {
	  //            the_handler.startAuthorization(false, true, Boolean.parseBoolean(import_scope));
	  //          }
	  //        }
	  //        Element authorizationElement = result.getChild(AUTH_AUTHORIZATION, AUTH_NS);
	  //        the_handler.setAuthorizationServiceXml(authorizationElement);
	  //        the_handler.setAuthorizationService(authorizationElement.getChildText(AUTH_CCID, AUTH_NS), 
	  //                                            authorizationElement.getChildText(AUTH_WEBSERVICE,  AUTH_NS));
	  //        the_handler.endAuthorization();
	  //      } 
      }
    } catch (Exception e) {
	throw new ParseException(e.getMessage(),e);
    }
    return false;
  }
  
  private void
  processItem(Element the_item, 
		  Element the_resources,
		  DefaultHandler the_handler) throws ParseException {
	  try {
		  if (the_item.getAttributeValue(CC_ITEM_IDREF)!=null) {
			  Element resource=findResource(ns,the_item.getAttributeValue(CC_ITEM_IDREF), the_resources);
			  if (resource == null) {
				  the_handler.getSimplePageBean().setErrKey("simplepage.cc-noresource", the_item.getAttributeValue(CC_ITEM_IDREF));
				  return;
			  }
			  log.debug("process item " + the_item + " resources " + the_resources + " resource " + resource);

			  the_handler.startCCItem(the_item.getAttributeValue(CC_ITEM_ID),
			  the_item.getChildText(CC_ITEM_TITLE, ns.getNs()));
			  the_handler.setCCItemXml(the_item, resource, this, utils, false);
			  processResource(resource,
                      the_handler);
			  the_handler.endCCItem();
		  } else {

			  //CP formats don't follow the hierarchy standard at
			  //http://www.imsglobal.org/cc/ccv1p0/imscc_profilev1p0.html#0_pgfId-1753534
			  //So we need to skip the ones where title isn't defined (which would indicate that it is a valid folder and not just containing sub-items)
			  String title = the_item.getChildText(CC_ITEM_TITLE, ns.getNs());
			  if (title != null) {
				  the_handler.startCCFolder(the_item);
			  }
			  for (Iterator iter=the_item.getChildren(CC_ITEM, ns.getNs()).iterator();iter.hasNext();) {
				  processItem((Element)iter.next(), the_resources, the_handler);
			  }
			  if (title != null) {
				  the_handler.endCCFolder();
			  }
		  }
	  } catch (Exception e) {
		  log.error(e.getMessage(), e);
		  if (the_item == null)
			  log.info("processitem the item null");
		  else 
			  log.info("processitem failed " + the_item.getAttributeValue(CC_ITEM_IDREF));
	  }
  } 
  
  public static Parser
  createCartridgeParser(CartridgeLoader the_cartridge) throws FileNotFoundException, IOException {
    return new Parser(the_cartridge);
  }  
}

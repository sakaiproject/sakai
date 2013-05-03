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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
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
 * a resource is protected or not.
 * 
 * @author Phil Nicholls
 * @version 1.0
 *
 */

public class Parser extends AbstractParser {
  
  CartridgeLoader utils; 
  
  private static final Map<String, ContentParser> parsers;
  
  private static final String IMS_MANIFEST="imsmanifest.xml";
  
  private static final String LAR0="associatedcontent/imscc_xmlv1p0/learning-application-resource";
  private static final String LAR1="associatedcontent/imscc_xmlv1p1/learning-application-resource";
  private static final String LAR2="associatedcontent/imscc_xmlv1p2/learning-application-resource";
  private static final String DISCUSSION0="imsdt_xmlv1p0";
  private static final String DISCUSSION1="imsdt_xmlv1p1";
  private static final String DISCUSSION2="imsdt_xmlv1p2";
  private static final String ASSESSMENT0="imsqti_xmlv1p2/imscc_xmlv1p0/assessment";
  private static final String ASSESSMENT1="imsqti_xmlv1p2/imscc_xmlv1p1/assessment";
  private static final String ASSESSMENT2="imsqti_xmlv1p2/imscc_xmlv1p2/assessment";
  private static final String WEBLINK0="imswl_xmlv1p0";
  private static final String WEBLINK1="imswl_xmlv1p1";
  private static final String WEBLINK2="imswl_xmlv1p2";
  private static final String WEBCONTENT="webcontent";
  private static final String QUESTION_BANK0="imsqti_xmlv1p2/imscc_xmlv1p0/question-bank";
  private static final String QUESTION_BANK1="imsqti_xmlv1p2/imscc_xmlv1p1/question-bank";
  private static final String QUESTION_BANK2="imsqti_xmlv1p2/imscc_xmlv1p2/question-bank";
  private static final String CC_BLTI0="imsbasiclti_xmlv1p0";
  private static final String CC_BLTI1="imsbasiclti_xmlv1p1";
  
  private static final String AUTH_QUERY="/ims:manifest/auth:authorizations";
  private static final String ITEM_QUERY="/ims:manifest/ims:organizations/ims:organization/ims:item";
  
  private static final QuestionBankParser qbp;
  
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
  private static final String CC_RESOURCES="resources";
  private static final String CC_RES_TYPE="type";
  
  static {
    qbp=new QuestionBankParser();
    parsers=new HashMap<String, ContentParser>();
    parsers.put(LAR0, new LearningApplicationResourceParser());
    parsers.put(LAR1, new LearningApplicationResourceParser());
    parsers.put(LAR2, new LearningApplicationResourceParser());
    parsers.put(DISCUSSION0, new DiscussionParser());
    parsers.put(DISCUSSION1, new DiscussionParser());
    parsers.put(DISCUSSION2, new DiscussionParser());
    parsers.put(ASSESSMENT0, new AssessmentParser());
    parsers.put(ASSESSMENT1, new AssessmentParser());
    parsers.put(ASSESSMENT2, new AssessmentParser());
    parsers.put(WEBLINK0, new WebLinkParser());
    parsers.put(WEBLINK1, new WebLinkParser());
    parsers.put(WEBLINK2, new WebLinkParser());
    parsers.put(WEBCONTENT, new WebContentParser());
    parsers.put(CC_BLTI0, new BLTIParser());
    parsers.put(CC_BLTI1, new BLTIParser());
    // there is no CC+BLTI2
  }
  
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
	System.out.println("parse error, stack trace follows " + e);
	e.printStackTrace();
	//      throw new ParseException(e.getMessage(),e);
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
	if (the_manifest.getNamespace().equals(ns.cc_ns()))
	    break;
    }
    if (v >= ns.getVersions()) {
	the_handler.getSimplePageBean().setErrMessage(
	      the_handler.getSimplePageBean().getMessageLocator().getMessage("simplepage.wrong-cc-version"));
	return;
    }

    //System.out.println("Found version " + ns.cc_ns());

    the_handler.startManifest();
    the_handler.setManifestXml(the_manifest);
    if (processAuthorization(the_manifest, the_handler))
	return; // don't process CCs with authorization
    processManifestMetadata(the_manifest, the_handler);
    try {
      XPath path=XPath.newInstance(ITEM_QUERY);
      path.addNamespace(ns.cc_ns());
      Element item = (Element)path.selectSingleNode(the_manifest);
      if (item!=null) {     
        for (Iterator iter=item.getChildren(CC_ITEM, ns.cc_ns()).iterator();iter.hasNext();) {
	    Element thisitem = (Element)iter.next();
          processItem((Element)thisitem, the_manifest.getChild(CC_RESOURCES, ns.cc_ns()), the_handler);
        }
      } 
      //now we need to check for the question bank and omitted dependencies
      if (the_manifest.getChild(CC_RESOURCES, ns.cc_ns()) != null &&
	  the_manifest.getChild(CC_RESOURCES, ns.cc_ns()).getChildren(CC_RESOURCE, ns.cc_ns()) != null)
      for (Iterator iter=the_manifest.getChild(CC_RESOURCES, ns.cc_ns()).getChildren(CC_RESOURCE, ns.cc_ns()).iterator(); iter.hasNext(); ) {
        Element resource=(Element)iter.next();
        if (resource.getAttributeValue(CC_RES_TYPE).equals(QUESTION_BANK0) ||
	    resource.getAttributeValue(CC_RES_TYPE).equals(QUESTION_BANK1) ||
	    resource.getAttributeValue(CC_RES_TYPE).equals(QUESTION_BANK2)) {
	    // I know it's not really an item, but it uses the same code as an assessment
	    the_handler.setCCItemXml(null, resource, this, utils, true);
	    processResource(resource, the_handler);
	    qbp.parseContent(the_handler, utils, resource, isProtected(resource));
        } else {
	    // create the resource if it wasn't already on a page
	    the_handler.setCCItemXml(null, resource, this, utils, true);
	    processResource(resource, the_handler);
	}
      }
      the_handler.endManifest();
    } catch (JDOMException e) {
      System.err.println(e.getMessage());
      throw new ParseException(e.getMessage(),e);
    }
  }
  
  private void 
  processManifestMetadata(Element manifest,
                          DefaultHandler the_handler) {
    Element metadata=manifest.getChild(MD, ns.cc_ns());
    if (metadata!=null) {
      the_handler.startManifestMetadata(metadata.getChildText(MD_SCHEMA, ns.cc_ns()), 
                                        metadata.getChildText(MD_SCHEMA_VERSION, ns.cc_ns()));
      the_handler.checkCurriculum(metadata);
      Element lom=metadata.getChild(MD_ROOT, ns.lomimscc_ns());
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
      path.addNamespace(ns.cc_ns());
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
	Element resource=findResource(the_handler.getNs(),the_item.getAttributeValue(CC_ITEM_IDREF), the_resources);
	if (resource == null) {
	    the_handler.getSimplePageBean().setErrKey("simplepage.cc-noresource", the_item.getAttributeValue(CC_ITEM_IDREF));
	    return;
	}
      // System.out.println("process item " + the_item + " resources " + the_resources + " resource " + resource);

      the_handler.startCCItem(the_item.getAttributeValue(CC_ITEM_ID),
                              the_item.getChildText(CC_ITEM_TITLE, ns.cc_ns()));
      the_handler.setCCItemXml(the_item, resource, this, utils, false);
      ContentParser parser=parsers.get(resource.getAttributeValue(CC_RES_TYPE));
      if (parser==null) {
	  System.out.println("content type not recognised " + resource.getAttributeValue(CC_RES_TYPE));
	  return;
      }
      processResource(resource,
                      the_handler);
      parser.parseContent(the_handler, utils, resource, isProtected(resource));
      the_handler.endCCItem();
    } else {
      the_handler.startCCFolder(the_item);
      for (Iterator iter=the_item.getChildren(CC_ITEM, ns.cc_ns()).iterator();iter.hasNext();) {
        processItem((Element)iter.next(), the_resources, the_handler);
      }
      the_handler.endCCFolder();
      }
      } catch (Exception e) {
	  e.printStackTrace();
	  if (the_item == null)
	      System.out.println("processitem the item null");
	  else 
	      System.out.println("processitem failed " + the_item.getAttributeValue(CC_ITEM_IDREF));
      }
  } 
  
  public static Parser
  createCartridgeParser(CartridgeLoader the_cartridge) throws FileNotFoundException, IOException {
    return new Parser(the_cartridge);
  }  
}

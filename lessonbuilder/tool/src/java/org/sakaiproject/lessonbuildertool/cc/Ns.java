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

import java.util.Map;
import java.util.HashMap;
import org.jdom.Namespace;

// all version dependencies should be in this file. 
// Parser sets the right index for the current version
// then everything else gets namespaces from here

public class Ns  {
    private int version = 0;
    //Default namespace
    private Namespace ns = CP_NS[0];
	private Namespace lom = LOMIMSCC_NS[0];

    private static final Namespace LOM_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/LOM"),
					       Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource"),
					       Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource"),
					       Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource")};


    private static final Namespace LOMIMSCC_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imscc/LOM"),
						    Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest"),
						    Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest"),
						    Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest")};

    private static final Namespace LOMIMSCP_NS[] = {Namespace.getNamespace("lom", "http://www.imsglobal.org/xsd/imsmd_v1p2"),
						    Namespace.getNamespace("lom", "http://www.imsglobal.org/xsd/imsmd_v1p2"),
						    Namespace.getNamespace("lom", "http://www.imsglobal.org/xsd/imsmd_v1p2")};

    	
    private static final Namespace CC_NS[] = {Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscc/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1")};

    private static final Namespace CP_NS[] = {Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscp_v1p1")};

    private static final Namespace TOPIC_NS[] = {Namespace.NO_NAMESPACE,
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1"),
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2"),
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3")};
    

    private static final Namespace LINK_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1"),
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2"),
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p3/imswl_v1p3")};

    private static final Namespace AUTH_NS[] = {Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccauth_v1p0"),
						Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccv1p1/imsccauth_v1p1"),
						Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccv1p2/imsccauth_v1p2"),
						Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccv1p3/imsccauth_v1p3")};

    private static final Namespace BLTI_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.getNamespace("blti", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0"),
						Namespace.getNamespace("blti", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0"),
						Namespace.getNamespace("blti", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0")};

    private static final Namespace LTICC_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						Namespace.getNamespace("lticc", "http://www.imsglobal.org/xsd/imslticc_v1p3")};

    private static final Namespace QTICC_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						 Namespace.getNamespace("qticc", "http://www.imsglobal.org/xsd/ims_qtiasiv1p2")};

    private static final Namespace ASSIGN_NS[] = {Namespace.NO_NAMESPACE,
						  Namespace.NO_NAMESPACE,
						  Namespace.NO_NAMESPACE,
						  Namespace.getNamespace("", "http://www.imsglobal.org/xsd/imscc_extensions/assignment")};

    private static final Namespace CSMD_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						Namespace.getNamespace("", "http://www.imsglobal.org/xsd/imscsmetadata_v1p0"),
						Namespace.getNamespace("", "http://www.imsglobal.org/xsd/imscsmetadata_v1p0")};

    private static final Namespace CPX_NS[] = {Namespace.NO_NAMESPACE,
					       Namespace.NO_NAMESPACE,
					       Namespace.NO_NAMESPACE,
					       Namespace.getNamespace("cpx", "http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2")};


    public void setVersion(int v) {
	version = v;
    }

    public int getVersions() {
	return 4;
    }

    public void setNs(Namespace ns) {
    	this.ns = ns;
    }

    public Namespace getNs() {
   	return ns;
    }

    public void setLom(Namespace ns) {
    	this.lom  = ns;
    }

    public Namespace getLom() {
   	return lom;
    }



    public Namespace lom_ns() {
	return LOM_NS[version];
    }

    public Namespace lomimscc_ns() {
	return LOMIMSCC_NS[version];
    }

    public Namespace lomimscp_ns() {
	return LOMIMSCP_NS[version];
    }

    public Namespace cc_ns() {
	return CC_NS[version];
    }

	public Namespace cp_ns() {
	return CP_NS[version];
    }


    public Namespace topic_ns() {
	return TOPIC_NS[version];
    }

    public Namespace link_ns() {
	return LINK_NS[version];
    }

    public Namespace auth_ns() {
	return AUTH_NS[version];
    }

    public Namespace blti_ns() {
	return BLTI_NS[version];
    }

    public Namespace lticc_ns() {
	return LTICC_NS[version];
    }

    public Namespace csmd_ns() {
	return CSMD_NS[version];
    }
    
    public Namespace qticc_ns() {
	return QTICC_NS[version];
    }

    public Namespace assign_ns() {
	return ASSIGN_NS[version];
    }

    public Namespace cpx_ns() {
	return CPX_NS[version];
    }

    private static final Map<String, String> resourceTypes;

    private static final String CC_WEBCONTENT="webcontent";
    private static final String LAR="learning-application-resource";
    private static final String WEBLINK="webLink";
    private static final String TOPIC="topic";
    private static final String ASSESSMENT="assessment";
    private static final String QUESTION_BANK="question-bank";
    private static final String BLTI="basiclti";
    private static final String ASSIGNMENT="assignment"; // version 1.3 extended type
    private static final String UNKNOWN="unknown";


    static {
	resourceTypes = new HashMap<String, String>();
	resourceTypes.put("webcontent", CC_WEBCONTENT);
	resourceTypes.put("associatedcontent/imscc_xmlv1p0/learning-application-resource", LAR);
	resourceTypes.put("associatedcontent/imscc_xmlv1p1/learning-application-resource", LAR);
	resourceTypes.put("associatedcontent/imscc_xmlv1p2/learning-application-resource", LAR);
	resourceTypes.put("associatedcontent/imscc_xmlv1p3/learning-application-resource", LAR);
	resourceTypes.put("imswl_xmlv1p0", WEBLINK);
	resourceTypes.put("imswl_xmlv1p1", WEBLINK);
	resourceTypes.put("imswl_xmlv1p2", WEBLINK);
	resourceTypes.put("imswl_xmlv1p3", WEBLINK);
	resourceTypes.put("imsdt_xmlv1p0", TOPIC);
	resourceTypes.put("imsdt_xmlv1p1", TOPIC);
	resourceTypes.put("imsdt_xmlv1p2", TOPIC);
	resourceTypes.put("imsdt_xmlv1p3", TOPIC);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p0/assessment", ASSESSMENT);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p1/assessment", ASSESSMENT);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p2/assessment", ASSESSMENT);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p3/assessment", ASSESSMENT);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p0/question-bank", QUESTION_BANK);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p1/question-bank", QUESTION_BANK);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p2/question-bank", QUESTION_BANK);
	resourceTypes.put("imsqti_xmlv1p2/imscc_xmlv1p3/question-bank", QUESTION_BANK);
	resourceTypes.put("imsbasiclti_xmlv1p0", BLTI);
	resourceTypes.put("imsbasiclti_xmlv1p1", BLTI);
	// 2 seems to have used v1p1
	resourceTypes.put("imsbasiclti_xmlv1p3", BLTI);
	resourceTypes.put("assignment_xmlv1p0", ASSIGNMENT);
    }

    public String normType(String type) {
	// default is web content. Any undefined type will thus be treated as a file with
	// no known semantics.
	String newtype = resourceTypes.get(type);
	if (newtype == null)
	    return UNKNOWN;
	else
	    return newtype;
    }
    
}

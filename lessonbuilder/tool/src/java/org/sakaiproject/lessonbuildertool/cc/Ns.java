package org.sakaiproject.lessonbuildertool.cc;

import org.jdom.Namespace;

// most version dependencies should be in this file. 
// Parser sets the right index for the current version
// then everything else gets namespaces from here

// in addition look for the xxx0 and xxx1 in Parser.java and PrintHandler.java

public class Ns  {
    private int version = 0;

    private static final Namespace LOM_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/LOM"),
					       Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource"),
					       Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource")};

    private static final Namespace LOMIMSCC_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imscc/LOM"),
						    Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest"),
						    Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest")};

    private static final Namespace CC_NS[] = {Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscc/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1")};

    private static final Namespace TOPIC_NS[] = {Namespace.NO_NAMESPACE,
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1"),
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2")};

    private static final Namespace LINK_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1"),
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2")};

    private static final Namespace AUTH_NS[] = {Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccauth_v1p0"),
						Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccv1p1/imsccauth_v1p1"),
						Namespace.getNamespace("auth", "http://www.imsglobal.org/xsd/imsccv1p2/imsccauth_v1p2")};

    private static final Namespace BLTI_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.getNamespace("blti", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0"),
						Namespace.getNamespace("blti", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0")};

    private static final Namespace CSMD_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.NO_NAMESPACE,
						Namespace.getNamespace("", "http://www.imsglobal.org/xsd/imscsmetadata_v1p0")};

    public void setVersion(int v) {
	version = v;
    }

    public int getVersions() {
	return 3;
    }

    public Namespace lom_ns() {
	return LOM_NS[version];
    }

    public Namespace lomimscc_ns() {
	return LOMIMSCC_NS[version];
    }

    public Namespace cc_ns() {
	return CC_NS[version];
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

    public Namespace csmd_ns() {
	return CSMD_NS[version];
    }

}

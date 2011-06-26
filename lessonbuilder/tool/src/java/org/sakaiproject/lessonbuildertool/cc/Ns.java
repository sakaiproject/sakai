package org.sakaiproject.lessonbuildertool.cc;

import org.jdom.Namespace;

public class Ns  {
    private static int version = 0;

    private static final Namespace LOM_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/LOM"),
					      Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource")};

    private static final Namespace LOMIMSCC_NS[] = {Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imscc/LOM"),
					      Namespace.getNamespace("lom", "http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest")};

    private static final Namespace CC_NS[] = {Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imscc/imscp_v1p1"),
					      Namespace.getNamespace("ims", "http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1")};

    private static final Namespace TOPIC_NS[] = {Namespace.NO_NAMESPACE,
						 Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1")};

    private static final Namespace LINK_NS[] = {Namespace.NO_NAMESPACE,
						Namespace.getNamespace("wl", "http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1")};

    public static void setVersion(int v) {
	version = v;
    }

    public static int getVersions() {
	return 2;
    }

    public static Namespace lom_ns() {
	return LOM_NS[version];
    }

    public static Namespace lomimscc_ns() {
	return LOMIMSCC_NS[version];
    }

    public static Namespace cc_ns() {
	return CC_NS[version];
    }

    public static Namespace topic_ns() {
	return TOPIC_NS[version];
    }

    public static Namespace link_ns() {
	return LINK_NS[version];
    }



}

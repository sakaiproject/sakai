package org.tsugi.lti2;

import java.util.Properties;
import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.lti2.LTI2Vars;

public class LTI2SampleData {

	public static Properties getSubstitution() {
		Properties lti2subst = new Properties();
		lti2subst.setProperty(LTI2Vars.COURSESECTION_SOURCEDID, "context_id_999");
		lti2subst.setProperty(LTI2Vars.COURSESECTION_LABEL,"SI364");
		lti2subst.setProperty(LTI2Vars.COURSESECTION_TITLE,"Building Interactive Applications");
		lti2subst.setProperty(LTI2Vars.CONTEXT_ID, "context_id_999");
		lti2subst.setProperty(LTI2Vars.CONTEXT_LABEL,"SI364");
		lti2subst.setProperty(LTI2Vars.CONTEXT_TITLE,"Building Interactive Applications");
		lti2subst.setProperty(LTI2Vars.MEMBERSHIP_ROLE,"Instructor");
		lti2subst.setProperty(LTI2Vars.RESOURCELINK_ID,"res_link_999");
		lti2subst.setProperty(LTI2Vars.RESOURCELINK_TITLE,"My weekly blog");
		lti2subst.setProperty(LTI2Vars.USER_ID,"user_id_007");
		lti2subst.setProperty(LTI2Vars.USER_USERNAME,"bond");
		lti2subst.setProperty(LTI2Vars.PERSON_NAME_GIVEN,"James");
		lti2subst.setProperty(LTI2Vars.PERSON_NAME_FAMILY,"Bond");;
		lti2subst.setProperty(LTI2Vars.PERSON_NAME_FULL,"James Bond");
		lti2subst.setProperty(LTI2Vars.PERSON_EMAIL_PRIMARY,"bond@example.com");
		return lti2subst;
	}

	public static Properties getLaunch() {
		Properties launch = new Properties();
		launch.setProperty(BasicLTIConstants.CONTEXT_ID,"context_id_999");
		launch.setProperty(BasicLTIConstants.CONTEXT_LABEL,"SI364");
		launch.setProperty(BasicLTIConstants.CONTEXT_TITLE,"Building Interactive Applications");
		launch.setProperty(BasicLTIConstants.ROLES,"Instructor");
		launch.setProperty(BasicLTIConstants.RESOURCE_LINK_ID,"res_link_999");
		launch.setProperty(BasicLTIConstants.RESOURCE_LINK_TITLE,"My weekly blog");
		launch.setProperty(BasicLTIConstants.USER_ID,"user_id_007");
		launch.setProperty(BasicLTIConstants.LIS_PERSON_NAME_GIVEN,"James");
		launch.setProperty(BasicLTIConstants.LIS_PERSON_NAME_FAMILY,"Bond");;
		launch.setProperty(BasicLTIConstants.LIS_PERSON_NAME_FULL,"James Bond");
		launch.setProperty(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY,"bond@example.com");
		return launch;
	}

}

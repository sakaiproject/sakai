package org.imsglobal.lti2;

import java.util.Properties;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.imsglobal.basiclti.BasicLTIConstants;

public class LTI2SampleData {

	public static Properties getSubstitution() {
		Properties lti2subst = new Properties();
		lti2subst.setProperty("CourseOffering.id","context_id_999");
		lti2subst.setProperty("CourseOffering.label","SI364");
		lti2subst.setProperty("CourseOffering.title","Building Interactive Applications");
		lti2subst.setProperty("Membership.role","Instructor");
		lti2subst.setProperty("ResourceLink.id","res_link_999");
		lti2subst.setProperty("ResourceLink.title","My weekly blog");
		lti2subst.setProperty("User.id","user_id_007");
		lti2subst.setProperty("User.username","bond");
		lti2subst.setProperty("Person.name.given","James");
		lti2subst.setProperty("Person.name.family","Bond");;
		lti2subst.setProperty("Person.name.full","James Bond");
		lti2subst.setProperty("Person.email.primary","bond@example.com");
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

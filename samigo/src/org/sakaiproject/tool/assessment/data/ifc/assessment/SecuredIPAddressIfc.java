package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface SecuredIPAddressIfc
    extends java.io.Serializable
{

  Long getId();

  void setId(Long id);

  void setAssessment(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessment();

  String getHostname();

  void setHostname(String hostname);

  String getIpAddress();

  void setIpAddress(String ipAddress);

}

package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Category;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;

public class PublishedSecuredIPAddress
    implements Serializable, SecuredIPAddressIfc{
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private AssessmentBaseIfc assessment;
  private String hostname;
  private String ipAddress;

  public PublishedSecuredIPAddress() {}

  public PublishedSecuredIPAddress(AssessmentBaseIfc assessment, String hostname, String ipAddress) {
    this.assessment = assessment;
    this.hostname = hostname;
    this.ipAddress = ipAddress;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AssessmentBaseIfc getAssessment() {
    return assessment;
  }

  public void setAssessment(AssessmentBaseIfc assessment) {
    this.assessment = assessment;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

}

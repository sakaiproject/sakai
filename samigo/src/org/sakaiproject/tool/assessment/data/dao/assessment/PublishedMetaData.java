package org.sakaiproject.tool.assessment.data.dao.assessment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;

import java.io.*;

import org.apache.log4j.*;

public class PublishedMetaData
    implements Serializable, AssessmentMetaDataIfc {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private AssessmentBaseIfc assessment;
  private String label;
  private String entry;

  public PublishedMetaData() {}

  public PublishedMetaData(AssessmentBaseIfc assessment, String label, String entry) {
    this.assessment = assessment;
    this.label = label;
    this.entry = entry;
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

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

}

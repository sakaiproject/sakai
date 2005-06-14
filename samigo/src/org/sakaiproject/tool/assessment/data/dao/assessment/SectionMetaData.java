package org.sakaiproject.tool.assessment.data.dao.assessment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

import java.io.*;

import org.apache.log4j.*;

public class SectionMetaData
    implements Serializable, SectionMetaDataIfc {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  public static String AUTHORS = "ASSESSMENT_AUTHORS";
  public static String KEYWORDS = "ASSESSMENT_KEYWORDS";
  public static String OBJECTIVES = "ASSESSMENT_OBJECTIVES";
  public static String RUBRICS = "ASSESSMENT_RUBRICS";
  public static String BGCOLOR = "ASSESSMENT_BGCOLOR";
  public static String BGIMAGE = "ASSESSMENT_BGIMAGE";

  private Long id;
  private SectionDataIfc section;
  private String label;
  private String entry;

  public SectionMetaData() {}

  public SectionMetaData(SectionDataIfc section, String label, String entry) {
    this.section= section;
    this.label = label;
    this.entry = entry;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SectionDataIfc getSection() {
    return section;
  }

  public void setSection(SectionDataIfc section) {
    this.section= section;
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

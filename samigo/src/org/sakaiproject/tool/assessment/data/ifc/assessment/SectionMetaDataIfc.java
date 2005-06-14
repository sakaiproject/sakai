package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface SectionMetaDataIfc
    extends java.io.Serializable
{
  public static String KEYWORDS = "SECTION_KEYWORDS";
  public static String OBJECTIVES = "SECTION_OBJECTIVES";
  public static String RUBRICS = "SECTION_RUBRICS";

  Long getId();

  void setId(Long id);

  SectionDataIfc getSection();

  void setSection(SectionDataIfc section);

  String getLabel();

  void setLabel(String label);

  String getEntry();

  void setEntry(String entry);

}

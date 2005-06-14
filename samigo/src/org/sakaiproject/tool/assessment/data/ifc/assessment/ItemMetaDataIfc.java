package org.sakaiproject.tool.assessment.data.ifc.assessment;


public interface ItemMetaDataIfc
    extends java.io.Serializable
{
  public static final String OBJECTIVE = "OBJECTIVE";
  public static final String KEYWORD= "KEYWORD";
  public static final String RUBRIC= "RUBRIC";
  public static final String RANDOMIZE= "RANDOMIZE";
  public static final String SCALENAME= "SCALENAME";
  public static final String PARTID= "PARTID";
  public static final String POOLID= "POOLID";
  public static final String TIMEALLOWED= "TIMEALLOWED";
  public static final String NUMATTEMPTS= "NUMATTEMPTS";

  Long getId();

  void setId(Long id);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item);

  String getLabel();

  void setLabel(String label);

  String getEntry();

  void setEntry(String entry);

}

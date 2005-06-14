package org.sakaiproject.tool.assessment.ui.bean.qti;

import java.io.Serializable;

/**
 * <p>Bean for QTI XML or XML fragments and descriptive information. </p>
 * <p>Used to maintain information or to dump XML to client.</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class XMLDisplay implements Serializable
{
  private String name;
  private String description;
  private String xml;
  private String id;

  public XMLDisplay()
  {
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getXml()
  {
//    System.out.println("debug: get XML=" + xml);
    return xml;
  }

  public void setXml(String xml)
  {
//    System.out.println("debug: set XML=" + xml);
    this.xml = xml;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }



}
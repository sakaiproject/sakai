package test.org.sakaiproject.tool.assessment.jsf;

import java.io.Serializable;

/**
 * <p> </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class TestLink implements Serializable
{
  private String action;
  private String text;
  public String getAction()
  {
    return action;
  }
  public void setAction(String action)
  {
    this.action = action;
  }
  public String getText()
  {
    return text;
  }
  public void setText(String text)
  {
    this.text = text;
  }
}
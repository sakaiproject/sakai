package test.org.sakaiproject.tool.assessment.jsf;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p> </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class TestLinksBean implements Serializable
{
  public TestLinksBean()
  {
    String[] actions = { "select", "author", "template" };
    links = new ArrayList();
    for (int i = 0; i < actions.length; i++)
    {
      TestLink link = new TestLink();
      link.setAction(actions[i]);
      link.setText("Link to " + actions[i]);
      links.add(link);
    }

  }
  private ArrayList links;
  public ArrayList getLinks()
  {
    return links;
  }
  public void setLinks(ArrayList links)
  {
    this.links = links;
  }
}
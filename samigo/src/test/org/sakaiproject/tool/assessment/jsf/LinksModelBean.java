package test.org.sakaiproject.tool.assessment.jsf;
import java.io.Serializable;

/**
 * <p> test bean</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class LinksModelBean implements Serializable
{
  private java.util.ArrayList linkListList;

  public LinksModelBean()
  {
    linkListList = new java.util.ArrayList();
    for (int i = 0; i < 20; i++) {
      linkListList.add(new TestLinksBean());
    }
  }

  public java.util.ArrayList getLinkListList()
  {
    return linkListList;
  }
  public void setLinkListList(java.util.ArrayList linkListList)
  {
    this.linkListList = linkListList;
  }

  public static void main(String args[])
  {
    LinksModelBean bean = new LinksModelBean();
    java.util.ArrayList list = bean.getLinkListList();

    for (int i = 0; i < list.size(); i++) {
      System.out.println("\ndebug:" + list.get(i));
    }
  }


}
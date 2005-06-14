package test.org.sakaiproject.tool.assessment.jsf;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
/**
 * <p> </p>
 * <p>Description: A Test Baking Bean</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */

public class SubBackingBean implements Serializable
{
  private String title;
  private String name;
  private String address;
  private Date date;
  private String id;
  private HashMap map;

  public SubBackingBean()
  {
    title = "General Discord";
    name = "Name";
    address = "address";
    date = new Date();
    id = "666";
    map = new HashMap();
    map.put("author", "author");
    map.put("template", "template");
    map.put("author2", "author");
    map.put("template2", "template");
    map.put("author3", "author");
    map.put("template3", "template");
 }


 public String getTitle()
 {
   return title;
 }

 public void setTitle(String t)
 {
   title = t;
 }
 public Date getDate()
 {
   return date;
 }

 public void setDate(Date d)
 {
   date = d;
 }
 public String getName()
 {
   return name;
 }

 public void setName(String p)
 {
   name = p;
 }
 public String getId()
 {
   return id;
 }

 public void setId(String p)
 {
   id = p;
 }
  public String getAddress()
  {
    return address;
  }

  public void setAddress(String p)
  {
    address = p;
  }
  public java.util.HashMap getMap()
  {
    return map;
  }
  public void setMap(java.util.HashMap map)
  {
    this.map = map;
  }

}
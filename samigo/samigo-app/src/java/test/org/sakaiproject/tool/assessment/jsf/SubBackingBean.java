/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
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
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


package example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * <p>Title: Sakai JSF</p>
 * <p>Description: Just a few properites so yo can set them.</p>
 * <p>Copyright: Copyright (c) 2005 Sakai Project</p>
 * <p>: </p>
 * @author Ed Smiley
 * @version 2.0
 */

public class SimplePropsBean implements Serializable
{
  private static final Random r = new Random();

  private String prop1 = "JSF test 1";
  private String prop2 = "JSF test 2";
  private String prop3 = "JSF test 3";
  private String prop4 = "JSF test 4";
  private String prop5 = "JSF test 5";
  private String prop6 = "JSF test 6";
  private java.util.Date date1 = makeADate();
  private java.util.Date date2 = makeADate();
  private java.util.Date date3 = makeADate();
  private java.util.Date date4 = makeADate();
  private java.util.List list1 = new ArrayList();
  private java.util.List list2 = new ArrayList();
  private java.util.List list3 = new ArrayList();
  private java.util.List list4 = new ArrayList();

  public String getProp1()
  {
    return prop1;
  }
  public void setProp1(String prop1)
  {
    this.prop1 = prop1;
  }
  public String getProp2()
  {
    return prop2;
  }
  public void setProp2(String prop2)
  {
    this.prop2 = prop2;
  }
  public String getProp3()
  {
    return prop3;
  }
  public void setProp3(String prop3)
  {
    this.prop3 = prop3;
  }
  public String getProp4()
  {
    return prop4;
  }
  public void setProp4(String prop4)
  {
    this.prop4 = prop4;
  }
  public java.util.Date getDate1()
  {
    return date1;
  }
  public void setDate1(java.util.Date date1)
  {
    this.date1 = date1;
  }
  public java.util.Date getDate2()
  {
    return date2;
  }
  public void setDate2(java.util.Date date2)
  {
    this.date2 = date2;
  }
  public java.util.Date getDate3()
  {
    return date3;
  }
  public void setDate3(java.util.Date date3)
  {
    this.date3 = date3;
  }
  public java.util.Date getDate4()
  {
    return date4;
  }
  public void setDate4(java.util.Date date4)
  {
    this.date4 = date4;
  }
  public java.util.List getList2()
  {
    return list2;
  }
  public void setList2(java.util.List list2)
  {
    this.list2 = list2;
  }
  public java.util.List getList3()
  {
    return list3;
  }
  public void setList3(java.util.List list3)
  {
    this.list3 = list3;
  }
  public java.util.List getList4()
  {
    return list4;
  }

  /**
   * unit tests
   * @param args
   */
  public static void main(String[] args)
  {
    SimplePropsBean spb = new SimplePropsBean();
    System.out.println("spb.prop1="+spb.prop1);
    System.out.println("spb.prop2="+spb.prop2);
    System.out.println("spb.prop3="+spb.prop3);
    System.out.println("spb.prop4="+spb.prop4);
    System.out.println("spb.prop5="+spb.prop5);
    System.out.println("spb.prop6="+spb.prop6);
    System.out.println("current date=" + new Date());
    System.out.println("date1="+spb.date1);
    System.out.println("date2="+spb.date2);
    System.out.println("date3="+spb.date3);
    System.out.println("date4="+spb.date4);
    if (spb.list1 != null)
    {
      System.out.println("list 1 OK");
    }
    if (spb.list2 != null)
    {
      System.out.println("list 2 OK");
    }
    if (spb.list3 != null)
    {
      System.out.println("list 3 OK");
    }
    if (spb.list4 != null)
    {
      System.out.println("list 4 OK");
    }

  }
  public String getProp5()
  {
    return prop5;
  }
  public void setProp5(String prop5)
  {
    this.prop5 = prop5;
  }
  public String getProp6()
  {
    return prop6;
  }
  public void setProp6(String prop6)
  {
    this.prop6 = prop6;
  }

  /**
   * @return
   */
  private static Date makeADate()
  {
    Date currDate = new Date();
    byte[] b = new byte[1];
    r.nextBytes(b);
    long randTime = currDate.getTime() * b[0];
    Date randDate = new Date(randTime);

    return randDate;
  }
}




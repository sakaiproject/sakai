/**********************************************************************************
* $HeadURL$
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
package org.sakaiproject.tool.assessment.ui.bean.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;

/**
 * <p> </p>
 * <p>Description: Backing Bean with some properties</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */

public class BackingBean
  implements Serializable
{
  private String prop1;
  private String prop2;
  private String prop3;
  private String prop4;
  private String prop5;
  private String prop6;
  private List list;

  public BackingBean()
  {
    System.out.println("Creating backing bean.");
    prop1 = "prop1";
    prop2 = "prop2";
    prop3 = "prop3";
    prop4 = "prop4";
    prop5 = "prop5";
    prop6 = "prop6";
    list = getBackingBeanList();
  }

  public String getProp1()
  {
    return prop1;
  }

  public void setProp1(String p)
  {
    prop1 = p;
  }

  public String getProp2()
  {
    return prop2;
  }

  public void setProp2(String p)
  {
    prop2 = p;
  }

  public String getProp3()
  {
    return prop3;
  }

  public void setProp3(String p)
  {
    prop3 = p;
  }

  public String getProp4()
  {
    return prop4;
  }

  public void setProp4(String p)
  {
    prop4 = p;
  }

  public String getProp5()
  {
    return prop5;
  }

  public void setProp5(String p)
  {
    prop5 = p;
  }

  public String getProp6()
  {
    return prop6;
  }

  public void setProp6(String p)
  {
    prop6 = p;
  }

  public List getList()
  {
    return list;
  }

  public void setList(List p)
  {
    list = p;
  }


  public String chooseAgentAdminGo()
  {
    System.out.println("Choosing admin");
    prop1 = "admin";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentRachelGo()
  {
    System.out.println("Choosing rachel");
    prop1 = "rachel";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentMarithGo()
  {
    System.out.println("Choosing marith");
    prop1 = "marith";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentAdmin()
  {
    System.out.println("Choosing admin");
    prop1 = "admin";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentRachel()
  {
    System.out.println("Choosing rachel");
    prop1 = "rachel";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentMarith()
  {
    System.out.println("Choosing marith");
    prop1 = "marith";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentAdminInstructor()
  {
    System.out.println("Choosing admin instructor");
    prop1 = "admin";

    // Call listener before going on.
    AuthorActionListener listener = new AuthorActionListener();
    listener.processAction(null);

    return "author";
  }

  // makes a whole bunch of backing beans.
  private List getBackingBeanList()
  {
    ArrayList list = new ArrayList();
    String[] lastNames =
                         {
                         "Black",
                         "Chang",
                         "Dumbledore",
                         "Granger",
                         "McGonagle",
                         "Potter",
                         "Snape",
                         "Weasley",
    };

    String[] firstNames =
                          {
                          "Alice",
                          "Bruce",
                          "Carrie",
                          "David",
                          "Elmer",
                          "Fresia"
    };

    for (int ilast = 0; ilast < lastNames.length; ilast++)
    {
      for (int ifirst = 0; ifirst < firstNames.length; ifirst++)
      {
        for (char c = 'A'; c < 'A' + 2; c++) //6; c++)
        {
          SubBackingBean bean = new SubBackingBean();
          bean.setName(firstNames[ifirst] + " " + c + "." +
                       lastNames[ilast]);
          bean.setAddress( ("" + (ilast + ifirst + c)) +
                          " Privet Drive");
          bean.setId( ("" + Math.random()).substring(2));

          list.add(bean);
        }
      }
    }

    return list;
  }
}

/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;

/**
 * <p> </p>
 * <p>Description: Test Bean with some properties</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TestBackingBean
  implements Serializable
{
  private String prop1;
  private String prop2;
  private String prop3;
  private String prop4;
  private String prop5;
  private String prop6;
  private List list;

  public TestBackingBean()
  {
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

  public String chooseAgentAdminGo()
  {
    prop1 = "admin";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentRachelGo()
  {
    prop1 = "rachel";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentMarithGo()
  {
    prop1 = "marith";

    // Call listener before going on.
    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.processAction(null);

    return "beginAssessment";
  }

  public String chooseAgentAdmin()
  {
    prop1 = "admin";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentRachel()
  {
    prop1 = "rachel";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentMarith()
  {
    prop1 = "marith";

    // Call listener before going on.
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);

    return "select";
  }

  public String chooseAgentAdminInstructor()
  {
    prop1 = "admin";

    // Call listener before going on.
    AuthorActionListener listener = new AuthorActionListener();
    listener.processAction(null);

    return "author";
  }

  public void setProp1(String p)
  {
    prop1 = p;
  }

  public String getProp2()
  {
    return prop2;
  }

  public String getProp3()
  {
    return prop3;
  }

  public String getProp5()
  {
    return prop5;
  }

  public void setProp3(String p)
  {
    prop3 = p;
  }

  public void setProp4(String p)
  {
    prop4 = p;
  }

  public void setProp2(String p)
  {
    prop2 = p;
  }

  public String getProp4()
  {
    return prop4;
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

/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package example;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Test Person backing bean.</p>
 * <p> </p>
 * <p>Copyright: Copyright  Sakai (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class Wizard implements Serializable{
  private String first;
  private String last;
  private String text;
  private List grades;
  private String id;
  private String address;
  public Wizard() {
  }
  public String getFirst() {
    return first;
  }
  public void setFirst(String first) {
    this.first = first;
  }
  public String getLast() {
    return last;
  }
  public void setLast(String last) {
    this.last = last;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public List getGrades() {
    return grades;
  }
  public void setGrades(java.util.List grades) {
    this.grades = grades;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  public String handleAction()
  {
    return text;
  }
  public String getAddress()
  {
    return address;
  }
  public void setAddress(String address)
  {
    this.address = address;
  }

}




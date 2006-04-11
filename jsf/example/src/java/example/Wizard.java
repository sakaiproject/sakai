/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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




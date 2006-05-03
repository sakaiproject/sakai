/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/syllabus/trunk/syllabus-impl/src/java/org/sakaiproject/component/app/syllabus/GatewaySyllabusImpl.java $
 * $Id: GatewaySyllabusImpl.java 8122 2006-05-01 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.component.app.syllabus;


import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.GatewaySyllabus;

import java.util.ArrayList;

/**
 * @author <a href="mailto:cwen.iupui.edu">Chen Wen</a>
 * @version $Id$
 * 
 */
public class GatewaySyllabusImpl implements GatewaySyllabus
{
  SyllabusData syllabusData;
  ArrayList attachList;
  
  public GatewaySyllabusImpl(SyllabusData syllabusData, ArrayList attachList)
  {
    this.syllabusData = syllabusData;
    this.attachList = attachList;
  }
  
  public final ArrayList getAttachList()
  {
    return attachList;
  }

  public final void setAttachList(ArrayList attachList)
  {
    this.attachList = attachList;
  }

  public final SyllabusData getSyllabusData()
  {
    return syllabusData;
  }

  public final void setSyllabusData(SyllabusData syllabusData)
  {
    this.syllabusData = syllabusData;
  }
}

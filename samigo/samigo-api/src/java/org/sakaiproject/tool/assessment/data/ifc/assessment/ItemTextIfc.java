/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.ArrayList;
import java.util.Set;

public interface ItemTextIfc
    extends java.io.Serializable
{
  Long getId();

  void setId(Long id);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item);

  Long getSequence();

  void setSequence(Long sequence);

  String getText();

  void setText(String text);

  Set getAnswerSet();

  void setAnswerSet(Set answerSet);

  ArrayList getAnswerArray();

  ArrayList getAnswerArraySorted();
}

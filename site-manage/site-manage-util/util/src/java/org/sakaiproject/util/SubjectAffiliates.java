/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.util;

import java.util.Collection;
import java.util.Vector;
import java.lang.String;

/**
 * SubjectAffiliate A utility class representing Affiliates within academic subject areas.
 */
public class SubjectAffiliates
{
	String m_subject = "";

	/** get subject */
	public String getSubject()
	{
		return m_subject;
	} // getSubject

	/** set subject */
	public void setSubject(String subject)
	{
		m_subject = subject;

	} // setSubject

	String m_campus = "";

	/** get campus */
	public String getCampus()
	{
		return m_campus;

	} // getCampus

	/** set campus */
	public void setCampus(String campus)
	{
		m_campus = campus;

	} // setCampus

	// affiliates uniqnames
	Collection m_uniqnames = new Vector();

	/** get affiliates uniqnames */
	public Collection getUniqnames()
	{
		return m_uniqnames;

	} // getUniqnames

	/** set affiliates uniqnames */
	public void setUniqnames(Collection uniqnames)
	{
		m_uniqnames = uniqnames;

	} // setUniqnames

} // SubjectAffiliate

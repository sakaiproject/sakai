/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.util;

import java.util.Collection;
import java.util.Vector;

/**
* SubjectAffiliate
*
* A utility class representing Affiliates within academic subject areas.
*/
public class SubjectAffiliates
{
	String m_subject = "";

	/** get subject */
	public String getSubject() 
	{ 
		return m_subject; 
	}	// getSubject
	
	/** set subject */
	public void setSubject(String subject)
	{
		m_subject = subject;
		
	}	// setSubject
	
	String m_campus = "";
	
	/** get campus */
	public String getCampus() 
	{ 
		return m_campus;
		
	}	// getCampus
	
	/** set campus */
	public void setCampus(String campus) 
	{ 
		m_campus = campus;
		
	}	// setCampus
	

	// affiliates uniqnames
	Collection m_uniqnames = new Vector();
	
	/** get affiliates uniqnames */
	public Collection getUniqnames() 
	{ 
		return m_uniqnames;
		
	}	// getUniqnames
	
	/** set affiliates uniqnames */
	public void setUniqnames(Collection uniqnames) 
	{ 
		m_uniqnames = uniqnames;
		
	}	// setUniqnames
	
} //SubjectAffiliate
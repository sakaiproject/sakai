/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.component.osid.id;

public class IdManager implements org.osid.id.IdManager
{
	private static final org.apache.commons.logging.Log LOG =
		org.apache.commons.logging.LogFactory.getLog(
				"org.sakaiproject.component.osid.id.IdManager" );

	org.osid.OsidContext context = null;

	java.util.Properties configuration = null;

	public org.osid.OsidContext getOsidContext() throws org.osid.id.IdException
	{
		return null;
	}

	public void assignOsidContext(org.osid.OsidContext context) throws org.osid.id.IdException
	{
		// Nothing to see here folks
	}

	public void assignConfiguration(java.util.Properties configuration) throws org.osid.id.IdException
	{
		// Nothing to see here folks
	}

	private void log(String entry) throws org.osid.id.IdException
	{
		LOG.debug("IdManager.log() entry: " + entry);
	}

	public org.osid.shared.Id createId() throws org.osid.id.IdException
	{
		try
		{
			return new Id();
		}
		catch (org.osid.shared.SharedException sex)
		{
			throw new org.osid.id.IdException(sex.getMessage());
		}
	}

	public org.osid.shared.Id getId(String idString) throws org.osid.id.IdException
	{
		if (idString == null)
		{
			throw new org.osid.id.IdException(org.osid.id.IdException.NULL_ARGUMENT);
		}
		try
		{
			return new Id(idString);
		}
		catch (org.osid.shared.SharedException sex)
		{
			throw new org.osid.id.IdException(sex.getMessage());
		}
	}

	public void osidVersion_2_0() throws org.osid.id.IdException
	{
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Footer: $
 *************************************************************************************************************************************************************************************************************************************************************/

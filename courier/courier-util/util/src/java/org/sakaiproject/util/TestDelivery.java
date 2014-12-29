/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.util;

/**
 * <p>
 * TestDelivery is a simple Delivery class for automated testing.
 * </p>
 */
public class TestDelivery extends BaseDelivery
{
	/** The message.  */
	protected String m_message = null;
   
   	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 * @param elementId
	 *        The elementId.
	 */
	public TestDelivery(String address, String elementId, String message)
	{
		super(address, elementId);
		m_message = message;
	} 

   /**
	 * Compose a message for delivery to the client.
    *  
	 * @return The message to send to the client.
	 */
	public String compose()
	{
		return m_message;
	} // compose

	/**
	 * Display.
	 */
	public String toString()
	{
		return super.toString() + " : " + m_message;
	} // toString

	/**
	 * Display.
	 */
	public String getMessage()
	{
		return m_message;
	} // toString
	
	
	/**
	 * Are these the same?
	 * 
	 * @return true if obj is the same Delivery as this one.
	 */
	public boolean equals(Object obj)
	{
		if (!super.equals(obj)) return false;

		TestDelivery cob = (TestDelivery) obj;
		if (StringUtil.different(cob.getMessage(), getMessage() )) return false;

		return true;
	}}

/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet.services.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Simple implementation of the PortletStateEncoder. This implementation simply
 * serializes the portlet state and encodes the bits in a url safe maner.
 * 
 * @since 2.2.3
 * @version $Id: SimplePortletStateEncoder.java 18420 2006-11-26 03:32:14Z
 *          ddewolf@apache.org $
 */
public class SimplePortletStateEncoder implements PortletStateEncoder
{

	private WebRecoder urlSafeEncoder;

	public WebRecoder getUrlSafeEncoder()
	{
		return urlSafeEncoder;
	}

	public void setUrlSafeEncoder(WebRecoder urlSafeEncoder)
	{
		this.urlSafeEncoder = urlSafeEncoder;
	}

	public String encode(PortletState state)
	{
		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		ObjectOutputStream out = null;
		try
		{
			out = new ObjectOutputStream(bao);
			out.writeObject(state);
			out.flush();
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Should never happen");
		}
		finally
		{
			try
			{
				if (out != null) out.close();
			}
			catch (IOException e)
			{
			}
		}

		byte[] bits = bao.toByteArray();
		return urlSafeEncoder.encode(bits);
	}

	public PortletState decode(String encodedState)
	{
		byte[] decoded = urlSafeEncoder.decode(encodedState);
		ByteArrayInputStream bai = new ByteArrayInputStream(decoded);
		ObjectInputStream in = null;
		try
		{
			in = new ObjectInputStream(bai);
			return (PortletState) in.readObject();
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Should never happen");
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("Stale PortletState; Class not found.");
		}
		finally
		{
			if (in != null) try
			{
				in.close();
			}
			catch (IOException io)
			{
			}
		}
	}

}

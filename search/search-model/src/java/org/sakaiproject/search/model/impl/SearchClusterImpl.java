/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.search.model.impl;

/**
 * This Class is only here to allow Hibernate to build the schema
 * @author ieb
 */
public class SearchClusterImpl
{
	private String name;

	private long version;

	private long size;

	private byte[] packet;

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *        The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Returns the packet.
	 */
	public byte[] getPacket()
	{
		return packet;
	}

	/**
	 * @param packet
	 *        The packet to set.
	 */
	public void setPacket(byte[] packet)
	{
		this.packet = packet;
	}

	/**
	 * @return Returns the size.
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * @param size
	 *        The size to set.
	 */
	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * @return Returns the version.
	 */
	public long getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 *        The version to set.
	 */
	public void setVersion(long version)
	{
		this.version = version;
	}

}

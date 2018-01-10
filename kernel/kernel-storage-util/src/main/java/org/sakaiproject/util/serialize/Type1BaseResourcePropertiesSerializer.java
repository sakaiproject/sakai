/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/serialize/Type1BaseResourcePropertiesSerializer.java $
 * $Id: Type1BaseResourcePropertiesSerializer.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.serialize.DataStreamEntitySerializer;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.entity.api.serialize.SerializablePropertiesAccess;

/**
 * @author ieb
 */
@Slf4j
public class Type1BaseResourcePropertiesSerializer implements DataStreamEntitySerializer
{

	private static final int TYPE1 = 1;

	private static final int BLOCK1 = 100;

	private static final int BLOCK2 = 101;

	private static final int BLOCK3 = 102;

	/**
	 * @see org.sakaiproject.entity.api.serialize.DataStreamEntitySerializer#parse(org.sakaiproject.entity.api.serialize.SerializableEntity,
	 *      java.io.DataInputStream)
	 */
	public void parse(SerializableEntity se, DataInputStream ds)
			throws EntityParseException
	{

		if (!(se instanceof SerializablePropertiesAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableProperties ");
		}
		SerializablePropertiesAccess sp = (SerializablePropertiesAccess) se;
		Map<String, Object> properties = new HashMap<String, Object>();

		try
		{
			int type = ds.readInt();
			if (type == TYPE1)
			{
				int block = ds.readInt();
				if (block == BLOCK1)
				{
					int nprops = ds.readInt();
					for (int i = 0; i < nprops; i++)
					{
						block = ds.readInt();
						switch (block)
						{
							case BLOCK2:
							{
								String key = ds.readUTF();
								String value = ds.readUTF();
								properties.put(key, value);
							}
								break;
							case BLOCK3:
							{
								String key = ds.readUTF();
								int n = ds.readInt();
								List<String> l = new Vector<String>();
								for (int j = 0; j < n; j++)
								{
									l.add(ds.readUTF());
								}
								properties.put(key, l);

							}
								break;
							default:
								throw new EntityParseException(
										"Unrecognised block number " + block);
						}
					}
					sp.setSerializableProperties(properties);
				}
				else
				{
					throw new EntityParseException(
							"Failed to parse entity, unrecognised block " + block);
				}
			}
			else
			{
				throw new EntityParseException(
						"Cant Parse block, resource properties is not type 1 " + type);
			}
		}
		catch (EntityParseException ep)
		{
			throw ep;
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to parse entity ", ex);
		}
	}

	/**
	 * @see org.sakaiproject.entity.api.serialize.DataStreamEntitySerializer#serialize(org.sakaiproject.entity.api.serialize.SerializableEntity,
	 *      java.io.DataOutputStream)
	 */
	public void serialize(SerializableEntity se, DataOutputStream ds)
			throws EntityParseException
	{
		if (!(se instanceof SerializablePropertiesAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableProperties ");
		}
		SerializablePropertiesAccess sp = (SerializablePropertiesAccess) se;
		Map<String, Object> properties = sp.getSerializableProperties();
		try
		{
			ds.writeInt(TYPE1);
			ds.writeInt(BLOCK1);
			int ps = properties.keySet().size();
			for (Iterator<String> i = properties.keySet().iterator(); i.hasNext();)
			{
				if ( i.next() == null ) {
					ps--;
				}
			}
			ds.writeInt(ps);
			for (Entry<String, Object> entry : properties.entrySet())
			{
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value != null)
				{
					if (value instanceof String)
					{
						ds.writeInt(BLOCK2);
						ds.writeUTF(key);
						ds.writeUTF((String) value);
					}
					else if (value instanceof List)
					{
						ds.writeInt(BLOCK3);
						ds.writeUTF(key);
						List<?> l = (List<?>) value;
						int s = l.size();
						for (Iterator<?> il = l.iterator(); il.hasNext();)
						{
							if (il.next() == null)
							{
								s--;
							}
						}
						ds.writeInt(s);
						for (Iterator<?> il = l.iterator(); il.hasNext();)
						{
							Object v = il.next();
							if (v != null)
							{
								if (v instanceof String)
								{
									ds.writeUTF((String) v);
								}
								else
								{
									log.warn("Non String found in property list " + v);
								}
							}
						}
					}
					else
					{
						log.warn("Non String found in property " + value);
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to serialize properties ", ex);
		}

	}

}

/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.content.impl.serialize.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.EntitySerializer;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.serialize.Type1BaseResourcePropertiesSerializer;

/**
 * <pre>
 * Serializes ContentResources using a Type1 block serializer, outputs a string with 
 * char in the byte range 0-255 that can be saved in a CBLOB or BLOB or on file.
 * These MUST be saved as UTF8.
 * If you need to modify this class think very carefully about what data might be in production databases.
 * DO NOT add new fields to existing blocks
 * DO NOT remove fields from blocks.
 * If you need more structure add new unique blocks.
 * If you need to change the structure, create a Type2 class and change the Type number so that type 1 
 * serializations continue to work.
 * 
 * The general structure of a serialization is
 * char 1-6 : BLOB_ID identifying the blob.
 * First Int: Type Number (int)
 * Next Int: Block number identifying block
 * The contents and length of the block is determined by the serailizer writing the block
 * When the serializer has finished writing the block the next int should be the next block number.
 * 
 * BLOB_ID's MUST be unique over all BLOBS.
 * TYPE Numbers MUST be unique within BLOBS of the same type
 * BLOCK Numbers MUST be unique within Types, but SHOULD be unique within BLOBS of the same type.
 * For variable lenght data, always serialize the length before the data. (eg number of elements, size of byte[])
 * For UTF Strings that are likely to be over 64K, you MUST serialize as lenght:UTF8byte[] as writeUTF will only
 * handle 64K
 * 
 * This structure is shared with the Type1CollectionSerializer
 * BLOCK1 
 * General Attributes
 * BLOCK2 
 * release and retract dates
 * BLOCK3 (varialble)
 * Groups
 * BLOCK4 
 * Properties (managed by a seperate serializer
 * BLOCK5
 * File properties
 * BLOCK6
 * Byte[] of content (I do hope not!, should be in a seperate table or on the filesystem)
 * BLOCK_END

 * </pre>
 * @author ieb
 */
public class Type1BaseContentResourceSerializer implements EntitySerializer
{
	public static final String BLOB_ID = "CHSBRE";

	private static final int TYPE1 = 1;

	private static final int BLOCK1 = 10;

	private static final int BLOCK3 = 11;

	private static final int BLOCK2 = 12;

	private static final int BLOCK4 = 13;

	private static final int BLOCK5 = 14;

	private static final int BLOCK6 = 15;

	private static final int BLOCK_END = 2;

	private Type1BaseResourcePropertiesSerializer baseResourcePropertiesSerializer = new Type1BaseResourcePropertiesSerializer();

	private TimeService timeService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntitySerializer#parse(org.sakaiproject.entity.api.SerializableEntity,
	 *      java.lang.String)
	 */
	public void parse(SerializableEntity se, String serialized)
			throws EntityParseException
	{

		if (!(se instanceof SerializableResourceAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableResourceAccess ");
		}
		SerializableResourceAccess sc = (SerializableResourceAccess) se;

		try
		{

			if (!serialized.startsWith(BLOB_ID))
			{
				throw new EntityParseException(
						"Data Block does not belong to this serializer got ["
								+ serialized.substring(0, BLOB_ID.length())
								+ "] expected [" + BLOB_ID + "]");
			}
			char[] cbuf = serialized.toCharArray();
			int blobIdLength = BLOB_ID.length();
			byte[] sb = new byte[cbuf.length - blobIdLength];
			for (int i = blobIdLength; i < cbuf.length; i++)
			{
				sb[i - blobIdLength] = (byte) cbuf[i];
			}

			String id = null;
			AccessMode access = AccessMode.INHERITED;
			boolean hidden = false;
			String resourceType = ResourceType.TYPE_UPLOAD;
			Time releaseDate = null;
			Time retractDate = null;
			Collection<String> groups = new Vector<String>();
			String contentType = null;
			long contentLength = 0;
			String filePath = null;
			byte[] body = null;

			ByteArrayInputStream baos = new ByteArrayInputStream(sb);
			DataInputStream ds = new DataInputStream(baos);
			int type = ds.readInt();
			if (type == TYPE1)
			{
				boolean finished = false;
				while (!finished)
				{
					int block = ds.readInt();
					switch (block)
					{
						case BLOCK1:
						{
							id = ds.readUTF();
							resourceType = ds.readUTF();
							access = AccessMode.fromString(ds.readUTF());
							if (access == null || AccessMode.SITE == access)
							{
								access = AccessMode.INHERITED;
							}

							hidden = ds.readBoolean();
						}
							break;
						case BLOCK2:
						{
							long rd = ds.readLong();
							if (rd != -1)
							{
								releaseDate = timeService.newTime(rd);
							}
							else
							{
								releaseDate = null;
							}
							rd = ds.readLong();
							if (rd != -1)
							{
								retractDate = timeService.newTime(rd);
							}
							else
							{
								retractDate = null;
							}
							if (hidden)
							{
								releaseDate = null;
								retractDate = null;
							}

						}
							break;
						case BLOCK3:
							int sz = ds.readInt();
							for (int i = 0; i < sz; i++)
							{
								groups.add(ds.readUTF());
							}
							break;
						case BLOCK4:
							baseResourcePropertiesSerializer.parse(sc
									.getSerializableProperties(), ds);
							break;
						case BLOCK5:
							contentType = ds.readUTF();
							contentLength = ds.readLong();
							filePath = ds.readUTF();

							ResourceTypeRegistry registry = sc.getResourceTypeRegistry();
							if (resourceType == null)
							{
								if (registry != null)
								{
									resourceType = registry
											.mimetype2resourcetype(contentType);
								}
								if ( resourceType == null )
								{
									resourceType = ResourceType.TYPE_UPLOAD;
								}
							}

							break;
						case BLOCK6:
							body = new byte[ds.readInt()];
							ds.read(body);
							break;
						case BLOCK_END:
							finished = true;
							break;
					}
				}
			}
			else
			{
				throw new EntityParseException("Unrecognised Record Type " + type);
			}
			
			sc.setSerializableId(id);
			sc.setSerializableAccess(access);
			sc.setSerializableHidden(hidden);
			sc.setSerializableResourceType(resourceType);
			sc.setSerializableReleaseDate(releaseDate);
			sc.setSerializableRetractDate(retractDate);
			sc.setSerializableGroups(groups);
			sc.setSerializableContentType(contentType);
			sc.setSerializableContentLength(contentLength);
			sc.setSerializableFilePath(filePath);
			sc.setSerializableBody(body);

		}
		catch (EntityParseException epe)
		{
			throw epe;
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to parse entity", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntitySerializer#serialize(org.sakaiproject.entity.api.SerializableEntity)
	 */
	public String serialize(SerializableEntity se) throws EntityParseException
	{
		if (!(se instanceof SerializableResourceAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableResourceAccess ");
		}
		SerializableResourceAccess sc = (SerializableResourceAccess) se;

		try
		{
			String id = sc.getSerializableId();
			boolean hidden = sc.getSerializableHidden();
			AccessMode access = sc.getSerializableAccess();
			Time releaseDate = sc.getSerializableReleaseDate();
			Time retractDate = sc.getSerializableRetractDate();
			Collection<String> groups = sc.getSerializableGroup();
			byte[] body = sc.getSerializableBody();
			String contentType = sc.getSerializableContentType();
			String filePath = sc.getSerializableFilePath();
			String resourceType = sc.getSerializableResourceType();
			long contentLength = sc.getSerializableContentLength();
			
			if ( contentType == null ) {
				contentType = "";
			}
			if ( filePath == null ) {
				filePath = "";
			}
			if ( resourceType == null ) {
				resourceType = ResourceType.TYPE_UPLOAD;
			}

			if (body != null) contentLength = body.length;

			if (access == null || AccessMode.SITE == access)
			{
				access = AccessMode.INHERITED;
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream ds = new DataOutputStream(baos);
			ds.writeInt(TYPE1);
			ds.writeInt(BLOCK1);
			ds.writeUTF(id);
			ds.writeUTF(resourceType);
			ds.writeUTF(access.toString());
			ds.writeBoolean(hidden);
			ds.writeInt(BLOCK2);
			if (!hidden && releaseDate != null)
			{
				ds.writeLong(releaseDate.getTime());
			}
			else
			{
				ds.writeLong(-1);
			}
			if (!hidden && retractDate != null)
			{
				ds.writeLong(retractDate.getTime());
			}
			else
			{
				ds.writeLong(-1);
			}
			if (groups != null)
			{
				ds.writeInt(BLOCK3);
				ds.writeInt(groups.size());
				for (Iterator igroup = groups.iterator(); igroup.hasNext();)
				{
					String groupRef = (String) igroup.next();
					ds.writeUTF(groupRef);
				}
			}

			ds.writeInt(BLOCK4);
			baseResourcePropertiesSerializer
					.serialize(sc.getSerializableProperties(), ds);
			ds.writeInt(BLOCK5);
			ds.writeUTF(contentType);
			ds.writeLong(contentLength);
			ds.writeUTF(filePath);

			if (body != null)
			{
				ds.writeInt(BLOCK6);
				ds.writeInt(body.length);
				ds.write(body);
			}
			ds.writeInt(BLOCK_END);
			ds.flush();
			baos.flush();
			byte[] op = baos.toByteArray();
			int bid = BLOB_ID.length();
			char[] opc = new char[op.length + bid];
			for (int i = 0; i < bid; i++)
			{
				opc[i] = BLOB_ID.charAt(i);
			}
			for (int i = bid; i < opc.length; i++)
			{
				opc[i] = (char) op[i-bid];
			}
			return new String(opc);
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to serialize entity ", ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.serialize.EntitySerializer#accept(java.lang.String)
	 */
	public boolean accept(String blob)
	{
		return (blob != null && blob.startsWith(BLOB_ID));
	}

	/**
	 * @return the timeService
	 */
	public TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * @param timeService the timeService to set
	 */
	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
	}

}

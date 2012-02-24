/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.content.impl.serialize.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.EntitySerializer;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.ByteStorageConversion;
import org.sakaiproject.util.serialize.Type1BaseResourcePropertiesSerializer;

/**
 * <pre>
 *      Serializes ContentCollections using a Type1 block serializer, outputs a string with 
 *      char in the byte range 0-255 that can be saved in a CBLOB or BLOB or on file.
 *      These MUST be saved as UTF8.
 *      If you need to modify this class think very carefully about what data might be in production databases.
 *      DO NOT add new fields to existing blocks
 *      DO NOT remove fields from blocks.
 *      If you need more structure add new unique blocks.
 *      If you need to change the structure, create a Type2 class and change the Type number so that type 1 
 *      serializations continue to work.
 *      
 *      The general structure of a serialization is
 *      char 1-6 : BLOB_ID identifying the blob.
 *      First Int: Type Number (int)
 *      Next Int: Block number identifying block
 *      The contents and length of the block is determined by the serailizer writing the block
 *      When the serializer has finished writing the block the next int should be the next block number.
 *      
 *      BLOB_ID's MUST be unique over all BLOBS.
 *      TYPE Numbers MUST be unique within BLOBS of the same type
 *      BLOCK Numbers MUST be unique within Types, but SHOULD be unique within BLOBS of the same type.
 *      For variable lenght data, always serialize the length before the data. (eg number of elements, size of byte[])
 *      For UTF Strings that are likely to be over 64K, you MUST serialize as lenght:UTF8byte[] as writeUTF will only
 *      handle 64K
 *      
 *      This structure is shared with the Type1ResoruceSerializer
 *      BLOCK1 
 *      General Attributes
 *      BLOCK2 
 *      release and retract dates
 *      BLOCK3 (varialble)
 *      Groups
 *      BLOCK4 
 *      Properties (managed by a seperate serializer
 *      BLOCK_END
 * </pre>
 * 
 * @author ieb
 */
public class Type1BaseContentCollectionSerializer implements EntitySerializer
{
	public static final String BLOB_ID = "CHSBCE";

	private static final byte[] BYTE_BLOB_ID = new byte[] { 'C', 'H', 'S', 'B', 'C', 'E' };

	// These are block markers in the serialization, DO NOT reuse,
	// create a new type or new block.
	// If you re-use you will have to provide data migration
	public static final int TYPE1 = 1;

	public static final int BLOCK1 = 10;

	public static final int BLOCK2 = 11;

	public static final int BLOCK3 = 12;

	public static final int BLOCK4 = 13;

	public static final int BLOCK_END = 2;
	
	private Type1BaseResourcePropertiesSerializer baseResourcePropertiesSerializer = new Type1BaseResourcePropertiesSerializer();

	private TimeService timeService;

	/**
	 * @deprecated
	 * @param se
	 * @param serialized
	 * @throws EntityParseException
	 */
	private void parseString(SerializableEntity se, String serialized)
			throws EntityParseException
	{

		if (!(se instanceof SerializableCollectionAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableCollection ");
		}
		SerializableCollectionAccess sc = (SerializableCollectionAccess) se;
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
			byte[] sb = new byte[cbuf.length - BLOB_ID.length()];

			ByteStorageConversion.toByte(cbuf, BLOB_ID.length(), sb, 0, sb.length);
			ByteArrayInputStream baos = new ByteArrayInputStream(sb);
			DataInputStream ds = new DataInputStream(baos);
			doParse(sc, ds);
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

	public void parse(SerializableEntity se, byte[] buffer)
			throws EntityParseException
	{
		if (!(se instanceof SerializableCollectionAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableCollection ");
		}
		SerializableCollectionAccess sc = (SerializableCollectionAccess) se;
		for (int i = 0; i < BYTE_BLOB_ID.length; i++)
		{
			if (buffer[i] != BYTE_BLOB_ID[i])
			{
				throw new EntityParseException(
						"Data Block does not belong to this serializer got ["
								+ new String(buffer,0,BYTE_BLOB_ID.length) + "] expected [" + new String(BYTE_BLOB_ID) + "]");
			}
		}
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			DataInputStream ds = new DataInputStream(bais);
			byte[] signature = new byte[BYTE_BLOB_ID.length];
			ds.read(signature);
			doParse(sc, ds);
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

	/**
	 * @deprecated
	 * @param se
	 * @return
	 * @throws EntityParseException
	 */
	private String serializeString(SerializableEntity se) throws EntityParseException
	{
		if (!(se instanceof SerializableCollectionAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableCollectionAccess ");
		}
		SerializableCollectionAccess sc = (SerializableCollectionAccess) se;

		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream ds = new DataOutputStream(baos);

			doSerialize(sc, ds);

			ds.flush();
			baos.flush();
			byte[] op = baos.toByteArray();
			char[] opc = new char[op.length + BLOB_ID.length()];
			int bid = BLOB_ID.length();

			ByteStorageConversion.toChar(op, 0, opc, bid, op.length);

			for (int i = 0; i < bid; i++)
			{
				opc[i] = BLOB_ID.charAt(i);
			}
			return new String(opc);
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to serialize entity ", ex);
		}
	}

	public byte[] serialize(SerializableEntity se)
			throws EntityParseException
	{
		if (!(se instanceof SerializableCollectionAccess))
		{
			throw new EntityParseException("Cant serialize " + se
					+ " as it is not a SerializableCollectionAccess ");
		}
		SerializableCollectionAccess sc = (SerializableCollectionAccess) se;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream ds = new DataOutputStream(baos);
			ds.write(BYTE_BLOB_ID);
			doSerialize(sc, ds);
			ds.flush();
			baos.flush();
			byte[] b = baos.toByteArray();
			baos.close();
			return b;
		}
		catch (Exception ex)
		{
			throw new EntityParseException("Failed to serialize entity ", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.serialize.EntitySerializer#accept(java.lang.String)
	 */
	public boolean accept(byte[] buffer)
	{
		if ( buffer == null || buffer.length < BYTE_BLOB_ID.length ) {
			return false;
		}
		for (int i = 0; i < BYTE_BLOB_ID.length; i++)
		{
			if (buffer[i] != BYTE_BLOB_ID[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the timeService
	 */
	public TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * @param timeService
	 *        the timeService to set
	 */
	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
	}

	private void doParse(SerializableCollectionAccess sc, DataInputStream ds)
			throws EntityParseException, IOException
	{

		String id = null;
		AccessMode access = AccessMode.INHERITED;
		boolean hidden = false;
		String resourceType = ResourceType.TYPE_FOLDER;
		Time releaseDate = null;
		Time retractDate = null;
		Collection<String> groups = new Vector<String>();

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
						if (!ResourceType.TYPE_FOLDER.equals(ds.readUTF()))
						{
							throw new EntityParseException("Data block is not of tye "
									+ ResourceType.TYPE_FOLDER);
						}
						access = AccessMode.fromString(ds.readUTF());
						if (access == null || AccessMode.SITE == access)
						{
							access = AccessMode.INHERITED;
						}
						hidden = ds.readBoolean();

						resourceType = ResourceType.TYPE_FOLDER;

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
					}
						break;
					case BLOCK3:
						int sz = ds.readInt();
						for (int i = 0; i < sz; i++)
						{
							groups.add(ds.readUTF());
						}
						if ( sz > 0 ) 
						{
							access = AccessMode.GROUPED;
						}
						break;
					case BLOCK4:
						baseResourcePropertiesSerializer.parse(sc
								.getSerializableProperties(), ds);
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

	}

	private void doSerialize(SerializableCollectionAccess sc, DataOutputStream ds)
			throws EntityParseException, IOException
	{
		String id = sc.getSerializableId();
		boolean hidden = sc.getSerializableHidden();
		AccessMode access = sc.getSerializableAccess();
		Time releaseDate = sc.getSerializableReleaseDate();
		Time retractDate = sc.getSerializableRetractDate();
		Collection<String> groups = sc.getSerializableGroup();

		ds.writeInt(TYPE1);
		ds.writeInt(BLOCK1);
		ds.writeUTF(id);
		ds.writeUTF(ResourceType.TYPE_FOLDER);

		if (access == null || AccessMode.SITE == access)
		{
			access = AccessMode.INHERITED;
		}

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
		baseResourcePropertiesSerializer.serialize(sc.getSerializableProperties(), ds);
		ds.writeInt(BLOCK_END);

	}

}

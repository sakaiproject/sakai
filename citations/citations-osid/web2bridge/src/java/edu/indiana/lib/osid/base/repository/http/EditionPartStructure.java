package edu.indiana.lib.osid.base.repository.http;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EditionPartStructure implements org.osid.repository.PartStructure
{
	private org.osid.shared.Id EDITION_PART_STRUCTURE_ID = null;
	private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
			"edition", "Edition of resource container (i.e. book, 2nd edition)" );
	private String displayName = "Edition";
	private String description = "Edition of resource container (i.e. book " +
			"title, 2nd edition)";
	private boolean mandatory = false;
	private boolean populatedByRepository = false;
	private boolean repeatable = false;

	private static EditionPartStructure editionPartStructure =
		new EditionPartStructure();

	protected static EditionPartStructure getInstance()
	{
		return editionPartStructure;
	}

	/**
	 * Public method to fetch the PartStructure ID
	 */
	public static org.osid.shared.Id getPartStructureId()
	{
		org.osid.shared.Id id = null;

		try
		{
			id = getInstance().getId();
		}
		catch (org.osid.repository.RepositoryException ignore) { }

		return id;
	}

	public String getDisplayName()
	throws org.osid.repository.RepositoryException
	{
		return this.displayName;
	}

	public String getDescription()
	throws org.osid.repository.RepositoryException
	{
		return this.description;
	}

	public boolean isMandatory()
	throws org.osid.repository.RepositoryException
	{
		return this.mandatory;
	}

	public boolean isPopulatedByRepository()
	throws org.osid.repository.RepositoryException
	{
		return this.populatedByRepository;
	}

	public boolean isRepeatable()
	throws org.osid.repository.RepositoryException
	{
		return this.repeatable;
	}

	protected EditionPartStructure()
	{
		try
		{
			this.EDITION_PART_STRUCTURE_ID =
				Managers.getIdManager().getId("09dfgljk2398dfknj98ewh34268000100");
		}
		catch (Throwable t)
		{
		}
	}

	public void updateDisplayName(String displayName)
	throws org.osid.repository.RepositoryException
	{
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.Id getId()
	throws org.osid.repository.RepositoryException
	{
		return this.EDITION_PART_STRUCTURE_ID;
	}

	public org.osid.shared.Type getType()
	throws org.osid.repository.RepositoryException
	{
		return this.type;
	}

	public org.osid.repository.RecordStructure getRecordStructure()
	throws org.osid.repository.RepositoryException
	{
		return RecordStructure.getInstance();
	}

	public boolean validatePart(org.osid.repository.Part part)
	throws org.osid.repository.RepositoryException
	{
		return true;
	}

	public org.osid.repository.PartStructureIterator getPartStructures()
	throws org.osid.repository.RepositoryException
	{
		return new PartStructureIterator(new java.util.Vector());
	}
}
